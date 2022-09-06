package com.temprovich.e30.throwables;

public class ReturnException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Object value;

    public ReturnException(Object value) {
        super(null, null, false, false);
        this.value = value;
    }

    public Object value() {
        return value;
    }
}
