package com.temprovich.schema.error;

public final class SchemaError extends Error {

    private static final long serialVersionUID = 1L;

    public SchemaError() {
        super();
    }

    public SchemaError(String message, Throwable cause) {
        super(message, cause);
    }

    public SchemaError(String message) {
        super(message);
    }

    public SchemaError(Throwable cause) {
        super(cause);
    }
}