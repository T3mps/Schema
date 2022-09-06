package com.temprovich.e30.instance;

import java.util.List;

import com.temprovich.e30.Interpreter;

public interface E30Callable {
    
    public abstract int arity();

    public default boolean isVariadic() { return arity() == - 1; }

    public abstract Object call(Interpreter interpreter, List<Object> arguments);

    @Override
    public abstract String toString();
}
