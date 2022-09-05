package com.temprovich.e30;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.temprovich.e30.error.E30ParseError;

public class Parser {
    
    private final List<Token> tokens;
    private int current = 0;
    private int loopDepth = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Statement> parse() {
        List<Statement> statements = new ArrayList<Statement>();

        while (!atEnd()) {
            statements.add(declaration());
        }
        
        return statements;
    }

    private Statement declaration() {
        try {
            if (match(TokenType.NODE)) {
                return nodeDeclaration();
            }
            if (match(TokenType.TRAIT)) {
                return traitDeclaration();
            }
            if (match(TokenType.FUNCTION)) {
                if (check(TokenType.FUNCTION) && checkNext(TokenType.IDENTIFIER)) {
                    consume(TokenType.FUNCTION, null);
                }
                return function("function");
            }
            if (match(TokenType.AUTO)) {
                return autoDeclaration();
            }

            return statement();
        } catch (E30ParseError e) {
            synchronize();
            return null;
        }
    }

    private Statement nodeDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect node name.");

        Expression.Variable parent = null;
        if (match(TokenType.COLON)) {
            consume(TokenType.IDENTIFIER, "Expect parent node name.");
            parent = new Expression.Variable(previous());
        }

        List<Expression> traits = withClause();

        List<Statement.Function> methods = new ArrayList<Statement.Function>();
        List<Statement.Function> metaMethods = new ArrayList<Statement.Function>();
        consume(TokenType.LEFT_BRACE, "Expect '{' to lead node body.");

        while (!check(TokenType.RIGHT_BRACE) && !atEnd()) {
            boolean isMeta = match(TokenType.NODE);
            (isMeta ? metaMethods : methods).add(function("method"));
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' to close node body.");

        return new Statement.Node(name, parent, traits, methods, metaMethods);
    }

    private Statement traitDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect trait name.");
        List<Expression> traits = withClause();

        consume(TokenType.LEFT_BRACE, "Expect '{' to lead trait body.");

        List<Statement.Function> methods = new ArrayList<Statement.Function>();
        while (!check(TokenType.RIGHT_BRACE) && !atEnd()) {
            methods.add(function("method"));
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' to close trait body.");

        return new Statement.Trait(name, traits, methods);
    }

    private List<Expression> withClause() {
        List<Expression> traits = new ArrayList<Expression>();
        if (match(TokenType.WITH)) {
            do {
                consume(TokenType.IDENTIFIER, "Expect trait name.");
                traits.add(new Expression.Variable(previous()));
            } while (match(TokenType.COMMA));
        }

        return traits;
    }

    private Statement autoDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expected name after 'auto'");

        Expression initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration");
        return new Statement.Auto(name, initializer);
    }

    private Statement.Function function(String type) {
        Token name = consume(TokenType.IDENTIFIER, "Expect " + type + " name.");
        return new Statement.Function(name, functionBody(type));
    }

    private Expression.Function functionBody(String type) {
        List<Token> parameters = null;
        
        if (!type.equals("method") || check(TokenType.LEFT_PAREN)) {
            consume(TokenType.LEFT_PAREN, "Expect '(' after " + type + " name.");
            parameters = new ArrayList<Token>();
            if (!check(TokenType.RIGHT_PAREN)) {
                do {
                    if (parameters.size() >= 255) {
                        error(peek(), "Can't have more than 255 parameters.");
                    }
    
                    parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
                } while (match(TokenType.COMMA));
            }
            consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");
        }

        consume(TokenType.LEFT_BRACE, "Expect '{' before " + type + " body.");
        List<Statement> body = block();
        return new Expression.Function(parameters, body);
    }

    private Statement statement() {
        if (match(TokenType.IF)) {
            return ifStatement();
        }
        if (match(TokenType.FOR)) {
            return forStatement();
        }
        if (match(TokenType.WHILE)) {
            return whileStatement();
        }
        if (match(TokenType.RETURN)) {
            return returnStatement();
        }
        if (match(TokenType.BREAK)) {
            return breakStatement();
        }
        if (match(TokenType.CONTINUE)) {
            return continueStatement();
        }
        if (match(TokenType.LEFT_BRACE)) {
            return new Statement.Block(block());
        }

        return expressionStatement();
    }

    private Statement expressionStatement() {
        Expression expression = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Statement.Expr(expression);
    }

    private Statement forStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.");

        Statement initializer;
        if (match(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (match(TokenType.AUTO)) {
            initializer = autoDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expression condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");

        Expression increment = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression();
        }

        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.");

        try {
            loopDepth++;
            Statement body = statement();

            if (increment != null) {
                body = new Statement.Block(
                    Arrays.asList(
                        body,
                        new Statement.Expr(increment)
                    )
                );
            }
    
            if (condition == null) {
                condition = new Expression.Literal(true);
            }
            body = new Statement.While(condition, body);
    
            if (initializer != null) {
                body = new Statement.Block(
                    Arrays.asList(
                        initializer,
                        body
                    )
                );
            }
    
            return body;
        } finally {
            loopDepth--;
        }
    }

    private Statement ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
        Expression condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");

        Statement thenBranch = statement();
        Statement elseBranch = match(TokenType.ELSE) ? statement() : null;

        return new Statement.If(condition, thenBranch, elseBranch);
    }

    private Statement whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.");
        Expression condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.");
        
        try {
            loopDepth++;
            Statement body = statement();

            return new Statement.While(condition, body);
        } finally {
            loopDepth--;
        }
    }

    private Statement returnStatement() {
        Token keyword = previous();
        Expression value = null;
        if (!check(TokenType.SEMICOLON)) {
            value = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after return value.");
        return new Statement.Return(keyword, value);
    }

    private Statement breakStatement() {
        if (loopDepth == 0) {
            error(previous(), "Can not break outside of a loop.");
        }
        consume(TokenType.SEMICOLON, "Expect ';' after break.");
        return new Statement.Break();
    }

    private Statement continueStatement() {
        if (loopDepth == 0) {
            error(previous(), "Can not continue to next iteration outside of a loop.");
        }
        consume(TokenType.SEMICOLON, "Expect ';' after continue.");
        return new Statement.Continue();
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<Statement>();

        while (!check(TokenType.RIGHT_BRACE) && !atEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Expression expression() {
        return assignment();
    }

    private Expression assignment() {
        Expression expression = or();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expression value = assignment();

            if (expression instanceof Expression.Variable) {
                Token name = ((Expression.Variable) expression).name();
                return new Expression.Assign(name, value);
            } else if (expression instanceof Expression.Attribute) {
                Expression.Attribute attribute = (Expression.Attribute) expression;
                return new Expression.Set(attribute.object(), attribute.name(), value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expression;
    }

    private Expression equality() {
        Expression expression = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression or() {
        Expression expression = and();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expression right = and();
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
    }

    private Expression and() {
        Expression expression = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Expression right = equality();
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
    }
    
    private Expression comparison() {
        Expression expression = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression term() {
        Expression expression = factor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expression right = factor();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression factor() {
        Expression expression = unary();

        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expression right = unary();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expression right = unary();
            return new Expression.Unary(operator, right);
        }

        return call();
    }

    private Expression call() {
        Expression expression = primary();

        for (;;) {
            if (match(TokenType.LEFT_PAREN)) {
                expression = finalizeCall(expression);
            } else if (match(TokenType.DOT)) {
                Token name = consume(TokenType.IDENTIFIER, "Expect attribute name after '.'.");
                expression = new Expression.Attribute(expression, name);
            } else {
                break;
            }
        }

        return expression;
    }

    private Expression finalizeCall(Expression callee) {
        List<Expression> arguments = new ArrayList<Expression>();

        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Cannot have more than 255 arguments.");
                }

                arguments.add(expression());
            } while (match(TokenType.COMMA));
        }

        Token paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expression.Call(callee, paren, arguments);
    }

    private Expression primary() {
        if (match(TokenType.FALSE)) {
            return new Expression.Literal(false);
        }
        if (match(TokenType.TRUE)) {
            return new Expression.Literal(true);
        }
        if (match(TokenType.NULL)) {
            return new Expression.Literal(null);
        }
        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expression.Literal(previous().literal());
        }
        if (match(TokenType.PARENT)) {
            Token keyword = previous();
            consume(TokenType.DOT, "Expect '.' after 'parent'.");
            Token method = consume(TokenType.IDENTIFIER, "Expect parent method name.");
            return new Expression.Parent(keyword, method);
        }
        if (match(TokenType.SELF)) {
            return new Expression.Self(previous());
        }
        if (match(TokenType.IDENTIFIER)) {
            return new Expression.Variable(previous());
        }
        if (match(TokenType.FUNCTION)) {
            return functionBody("function");
        }
        if (match(TokenType.LEFT_PAREN)) {
            Expression expression = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expression.Grouping(expression);
        }
        
        throw error(peek(), "Expect expression.");
    }

    private void synchronize() {
        advance();

        while (!atEnd()) {
            if (previous().type() == TokenType.SEMICOLON) {
                return;
            }

            switch (peek().type()) {
                case NODE:
                case FUNCTION:
                case AUTO:
                case FOR:
                case IF:
                case WHILE:
                case RETURN:
                    return;
                default:
                    advance();
            }
        }
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        
        throw error(peek(), message);
    }

    private E30ParseError error(Token token, String message) {
        E30.error(token, message);
        return new E30ParseError(token, message);
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        
        return false;
    }

    private boolean check(TokenType type) {
        if (atEnd()) {
            return false;
        }
        
        return peek().type() == type;
    }

    private boolean checkNext(TokenType type) {
        if (atEnd()) {
            return false;
        }
        if (tokens.get(current + 1).type() == TokenType.EOF) {
            return false;
        }
        return tokens.get(current + 1).type() == type;
    }

    private Token advance() {
        if (!atEnd()) {
            current++;
        }
        
        return previous();
    }

    private boolean atEnd() {
        return peek().type() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
