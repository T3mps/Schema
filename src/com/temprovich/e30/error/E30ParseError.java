package com.temprovich.e30.error;

import com.temprovich.e30.lexer.Token;

public final class E30ParseError extends E30RuntimeError {

    public E30ParseError(String message) {
        super(message);
    }

    public E30ParseError(Token token, String message) {
        super(token, message);
    }
}