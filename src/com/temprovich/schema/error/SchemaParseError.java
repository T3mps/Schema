package com.temprovich.schema.error;

import com.temprovich.schema.lexer.Token;

public final class SchemaParseError extends SchemaRuntimeError {

    public SchemaParseError(String message) {
        super(message);
    }

    public SchemaParseError(Token token, String message) {
        super(token, message);
    }
}