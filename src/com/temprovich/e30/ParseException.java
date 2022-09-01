package com.temprovich.e30;

class ParseException extends RuntimeException {
    
    public ParseException(Token token, String message) {
        super("[line " + token.line() + "] " + message);
    }

    public ParseException(Token token, String message, Throwable cause) {
        super("[line " + token.line() + "] " + message, cause);
    }
}
