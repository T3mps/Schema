package com.temprovich.e30.error;

public final class E30Error extends Error {

    private static final long serialVersionUID = 1L;

    public E30Error() {
        super();
    }

    public E30Error(String message, Throwable cause) {
        super(message, cause);
    }

    public E30Error(String message) {
        super(message);
    }

    public E30Error(Throwable cause) {
        super(cause);
    }
}