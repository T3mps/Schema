package com.temprovich.e30;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    
    private final List<Token> tokens;
    private int current = 0;

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
            if (match(TokenType.AUTO)) {
                return autoDeclaration();
            }

            return statement();
        } catch (E30ParseException e) {
            synchronize();
            return null;
        }
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

    private Statement statement() {
        if (match(TokenType.PRINT)) {
            return printStatement();
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

    private Statement printStatement() {
        Expression expression = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Statement.Print(expression);
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
        Expression expression = equality();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expression value = assignment();

            if (expression instanceof Expression.Variable) {
                Token name = ((Expression.Variable) expression).name();
                return new Expression.Assign(name, value);
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

        return primary();
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
        if (match(TokenType.IDENTIFIER)) {
            return new Expression.Variable(previous());
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
                case CLASS:
                case FUNCTION:
                case AUTO:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }
        
        throw error(peek(), message);
    }

    private E30ParseException error(Token token, String message) {
        E30.error(token, message);
        return new E30ParseException(token, message);
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
