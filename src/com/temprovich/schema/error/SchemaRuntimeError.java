package com.temprovich.schema.error;

import com.temprovich.schema.lexer.Token;

public sealed class SchemaRuntimeError extends RuntimeException permits SchemaParseError {

    private final Token token;

    public SchemaRuntimeError(String message) {
        super(message);
        this.token = null;
    }

    public SchemaRuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }

    public Token token() {
        return token;
    }
}