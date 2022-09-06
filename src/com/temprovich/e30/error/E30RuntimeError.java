package com.temprovich.e30.error;

import com.temprovich.e30.lexer.Token;

public sealed class E30RuntimeError extends RuntimeException permits E30ParseError {

    private final Token token;

    public E30RuntimeError(String message) {
        super(message);
        this.token = null;
    }

    public E30RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }

    public Token token() {
        return token;
    }
}