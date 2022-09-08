package com.temprovich.schema;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.temprovich.schema.error.SchemaParseError;
import com.temprovich.schema.lexer.Token;

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
            if (match(Token.Type.FUNCTION)) {
                if (check(Token.Type.FUNCTION) && checkNext(Token.Type.IDENTIFIER)) {
                    consume(Token.Type.FUNCTION, null);
                }
                return function("function");
            }
            if (match(Token.Type.AUTO)) {
                return autoDeclaration();
            }
            if (match(Token.Type.NODE)) {
                return nodeDeclaration();
            }
            if (match(Token.Type.TRAIT)) {
                return traitDeclaration();
            }
            return statement();
        } catch (SchemaParseError e) {
            synchronize();
            return null;
        }
    }

    private Statement nodeDeclaration() {
        Token name = consume(Token.Type.IDENTIFIER, "Expect node name.");

        Expression.Variable parent = null;
        if (match(Token.Type.COLON)) {
            consume(Token.Type.IDENTIFIER, "Expect parent node name.");
            parent = new Expression.Variable(previous());
        }

        List<Expression> traits = withClause();

        List<Statement.Function> methods = new ArrayList<Statement.Function>();
        List<Statement.Function> metaMethods = new ArrayList<Statement.Function>();
        consume(Token.Type.LEFT_BRACE, "Expect '{' to lead node body.");

        while (!check(Token.Type.RIGHT_BRACE) && !atEnd()) {
            boolean isMeta = match(Token.Type.NODE);
            (isMeta ? metaMethods : methods).add(function("method"));
        }

        consume(Token.Type.RIGHT_BRACE, "Expect '}' to close node body.");

        return new Statement.Node(name, parent, traits, methods, metaMethods);
    }

    private Statement traitDeclaration() {
        Token name = consume(Token.Type.IDENTIFIER, "Expect trait name.");
        List<Expression> traits = withClause();

        consume(Token.Type.LEFT_BRACE, "Expect '{' to lead trait body.");

        List<Statement.Function> methods = new ArrayList<Statement.Function>();
        while (!check(Token.Type.RIGHT_BRACE) && !atEnd()) {
            methods.add(function("method"));
        }

        consume(Token.Type.RIGHT_BRACE, "Expect '}' to close trait body.");

        return new Statement.Trait(name, traits, methods);
    }

    private List<Expression> withClause() {
        List<Expression> traits = new ArrayList<Expression>();
        if (match(Token.Type.WITH)) {
            do {
                consume(Token.Type.IDENTIFIER, "Expect trait name.");
                traits.add(new Expression.Variable(previous()));
            } while (match(Token.Type.COMMA));
        }

        return traits;
    }

    private Statement.Function function(String type) {
        Token name = consume(Token.Type.IDENTIFIER, "Expect " + type + " name.");
        return new Statement.Function(name, functionBody(type));
    }

    private Expression.Function functionBody(String type) {
        List<Token> parameters = null;
        
        if (!type.equals("method") || check(Token.Type.LEFT_PAREN)) {
            consume(Token.Type.LEFT_PAREN, "Expect '(' after " + type + " name.");
            parameters = new ArrayList<Token>();
            if (!check(Token.Type.RIGHT_PAREN)) {
                do {
                    if (parameters.size() >= 255) {
                        error(peek(), "Can't have more than 255 parameters.");
                    }
    
                    parameters.add(consume(Token.Type.IDENTIFIER, "Expect parameter name."));
                } while (match(Token.Type.COMMA));
            }
            consume(Token.Type.RIGHT_PAREN, "Expect ')' after parameters.");
        }

        consume(Token.Type.LEFT_BRACE, "Expect '{' before " + type + " body.");
        List<Statement> body = block();
        return new Expression.Function(parameters, body);
    }

    private Statement autoDeclaration() {
        Token name = consume(Token.Type.IDENTIFIER, "Expected name after 'auto'");

        Expression initializer = null;
        if (match(Token.Type.EQUAL)) {
            initializer = expression();
        }

        consume(Token.Type.SEMICOLON, "Expected ';' after variable declaration");
        return new Statement.Auto(name, initializer);
    }

    private Statement statement() {
        if (match(Token.Type.IF)) {
            return ifStatement();
        }
        if (match(Token.Type.FOR)) {
            return forStatement();
        }
        if (match(Token.Type.WHILE)) {
            return whileStatement();
        }
        if (match(Token.Type.RETURN)) {
            return returnStatement();
        }
        if (match(Token.Type.BREAK)) {
            return breakStatement();
        }
        if (match(Token.Type.CONTINUE)) {
            return continueStatement();
        }
        if (match(Token.Type.LEFT_BRACE)) {
            return new Statement.Block(block());
        }

        return expressionStatement();
    }

    private Statement expressionStatement() {
        Expression expression = expression();
        consume(Token.Type.SEMICOLON, "Expect ';' after expression.");
        return new Statement.Expr(expression);
    }

    private Statement forStatement() {
        consume(Token.Type.LEFT_PAREN, "Expect '(' after 'for'.");

        Statement initializer;
        if (match(Token.Type.SEMICOLON)) {
            initializer = null;
        } else if (match(Token.Type.AUTO)) {
            initializer = autoDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expression condition = null;
        if (!check(Token.Type.SEMICOLON)) {
            condition = expression();
        }

        consume(Token.Type.SEMICOLON, "Expect ';' after loop condition.");

        Expression increment = null;
        if (!check(Token.Type.RIGHT_PAREN)) {
            increment = expression();
        }

        consume(Token.Type.RIGHT_PAREN, "Expect ')' after for clauses.");

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
        consume(Token.Type.LEFT_PAREN, "Expect '(' after 'if'.");
        Expression condition = expression();
        consume(Token.Type.RIGHT_PAREN, "Expect ')' after if condition.");

        Statement thenBranch = statement();
        Statement elseBranch = match(Token.Type.ELSE) ? statement() : null;

        return new Statement.If(condition, thenBranch, elseBranch);
    }

    private Statement whileStatement() {
        consume(Token.Type.LEFT_PAREN, "Expect '(' after 'while'.");
        Expression condition = expression();
        consume(Token.Type.RIGHT_PAREN, "Expect ')' after condition.");
        
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
        if (!check(Token.Type.SEMICOLON)) {
            value = expression();
        }

        consume(Token.Type.SEMICOLON, "Expect ';' after return value.");
        return new Statement.Return(keyword, value);
    }

    private Statement breakStatement() {
        if (loopDepth == 0) {
            error(previous(), "Can not break outside of a loop.");
        }
        consume(Token.Type.SEMICOLON, "Expect ';' after break.");
        return new Statement.Break();
    }

    private Statement continueStatement() {
        if (loopDepth == 0) {
            error(previous(), "Can not continue to next iteration outside of a loop.");
        }
        consume(Token.Type.SEMICOLON, "Expect ';' after continue.");
        return new Statement.Continue();
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<Statement>();

        while (!check(Token.Type.RIGHT_BRACE) && !atEnd()) {
            statements.add(declaration());
        }

        consume(Token.Type.RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Expression expression() {
        return assignment();
    }

    private Expression assignment() {
        Expression expression = or();

        if (match(Token.Type.EQUAL)) {
            Token equals = previous();
            Expression value = assignment();

            if (expression instanceof Expression.Variable) {
                Token name = ((Expression.Variable) expression).name();
                return new Expression.Assign(name, value);
            } else if (expression instanceof Expression.Index) {
                Expression.Index index = (Expression.Index) expression;
                Token name = index.name();
                return new Expression.IndexSet(name, index.index(), value);
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

        while (match(Token.Type.BANG_EQUAL, Token.Type.EQUAL_EQUAL, Token.Type.PLUS_EQUAL,
                                                                  Token.Type.MINUS_EQUAL,
                                                                  Token.Type.STAR_EQUAL,
                                                                  Token.Type.SLASH_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression or() {
        Expression expression = and();

        while (match(Token.Type.OR)) {
            Token operator = previous();
            Expression right = and();
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
    }

    private Expression and() {
        Expression expression = equality();

        while (match(Token.Type.AND)) {
            Token operator = previous();
            Expression right = equality();
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
    }
    
    private Expression comparison() {
        Expression expression = term();

        while (match(Token.Type.GREATER, Token.Type.GREATER_EQUAL, Token.Type.LESS, Token.Type.LESS_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression term() {
        Expression expression = factor();

        while (match(Token.Type.MINUS, Token.Type.PLUS)) {
            Token operator = previous();
            Expression right = factor();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression factor() {
        Expression expression = unary();

        while (match(Token.Type.SLASH, Token.Type.STAR)) {
            Token operator = previous();
            Expression right = unary();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private Expression unary() {
        if (match(Token.Type.BANG, Token.Type.MINUS, Token.Type.PLUS_PLUS, Token.Type.MINUS_MINUS)) {
            Token operator = previous();
            Expression right = unary();
            return new Expression.Unary(operator, right);
        }

        return call();
    }

    private Expression call() {
        Expression expression = primary();

        for (;;) {
            if (match(Token.Type.LEFT_PAREN)) {
                expression = finalizeCall(expression);
            } else if (match(Token.Type.DOT)) {
                Token name = consume(Token.Type.IDENTIFIER, "Expect attribute name after '.'.");
                expression = new Expression.Attribute(expression, name);
            } else {
                break;
            }
        }

        return expression;
    }

    private Expression finalizeCall(Expression callee) {
        List<Expression> arguments = new ArrayList<Expression>();

        if (!check(Token.Type.RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Cannot have more than 255 arguments.");
                }

                arguments.add(expression());
            } while (match(Token.Type.COMMA));
        }

        Token paren = consume(Token.Type.RIGHT_PAREN, "Expect ')' after arguments.");

        return new Expression.Call(callee, paren, arguments);
    }

    private Expression primary() {
        if (match(Token.Type.FALSE)) {
            return new Expression.Literal(false);
        }
        if (match(Token.Type.TRUE)) {
            return new Expression.Literal(true);
        }
        if (match(Token.Type.NULL)) {
            return new Expression.Literal(null);
        }
        if (match(Token.Type.NUMBER, Token.Type.STRING)) {
            return new Expression.Literal(previous().literal());
        }
        if (match(Token.Type.PARENT)) {
            Token keyword = previous();
            consume(Token.Type.DOT, "Expect '.' after 'parent'.");
            Token method = consume(Token.Type.IDENTIFIER, "Expect parent method name.");
            return new Expression.Parent(keyword, method);
        }
        if (match(Token.Type.SELF)) {
            return new Expression.Self(previous());
        }
        if (match(Token.Type.IDENTIFIER)) {
            Token next = peek();
            if (next.type() == Token.Type.LEFT_SQUARE_BRACKET) {
                Token name = previous();
                consume(Token.Type.LEFT_SQUARE_BRACKET, "Expect '[' after identifier.");
                Expression index = expression();
                consume(Token.Type.RIGHT_SQUARE_BRACKET, "Expect ']' after index.");
                return new Expression.Index(name, index);
            }

            return new Expression.Variable(previous());
        }
        if (match(Token.Type.ARRAY)) {
            consume(Token.Type.LEFT_SQUARE_BRACKET, "Expect '[' after 'array'.");
            Expression size = expression();
            consume(Token.Type.RIGHT_SQUARE_BRACKET, "Expect ']' after array size.");
            return new Expression.IndexGet(size);
        }
        if (match(Token.Type.FUNCTION)) {
            return functionBody("function");
        }
        if (match(Token.Type.LEFT_PAREN)) {
            Expression expression = expression();
            consume(Token.Type.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expression.Grouping(expression);
        }
        
        throw error(peek(), "Expect expression.");
    }

    private void synchronize() {
        advance();

        while (!atEnd()) {
            if (previous().type() == Token.Type.SEMICOLON) {
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

    private Token consume(Token.Type type, String message) {
        if (check(type)) {
            return advance();
        }
        
        throw error(peek(), message);
    }

    private SchemaParseError error(Token token, String message) {
        Schema.reporter.error(token, message);
        return new SchemaParseError(token, message);
    }

    private boolean match(Token.Type... types) {
        for (Token.Type type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        
        return false;
    }

    private boolean check(Token.Type type) {
        if (atEnd()) {
            return false;
        }
        
        return peek().type() == type;
    }

    private boolean checkNext(Token.Type type) {
        if (atEnd()) {
            return false;
        }
        if (tokens.get(current + 1).type() == Token.Type.EOF) {
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
        return peek().type() == Token.Type.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
