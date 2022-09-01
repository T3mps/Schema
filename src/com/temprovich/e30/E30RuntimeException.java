package com.temprovich.e30;

public class E30RuntimeException extends RuntimeException {

    private final Token token;

    public E30RuntimeException(String message) {
        super(message);
        this.token = null;
    }

    public E30RuntimeException(Token token, String message) {
        super(message);
        this.token = token;
    }

    public Token token() {
        return token;
    }
}
