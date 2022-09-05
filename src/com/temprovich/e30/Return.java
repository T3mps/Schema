package com.temprovich.e30;

public class Return extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Object value;

    public Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }

    public Object value() {
        return value;
    }
}
