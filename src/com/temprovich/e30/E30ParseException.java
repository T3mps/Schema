package com.temprovich.e30;

class E30ParseException extends RuntimeException {
    
    public E30ParseException(Token token, String message) {
        super("[line " + token.line() + "] " + message);
    }

    public E30ParseException(Token token, String message, Throwable cause) {
        super("[line " + token.line() + "] " + message, cause);
    }
}
