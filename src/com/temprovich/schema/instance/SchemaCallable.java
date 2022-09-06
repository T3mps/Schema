package com.temprovich.schema.instance;

import java.util.List;

import com.temprovich.schema.Interpreter;

public interface SchemaCallable {
    
    public abstract int arity();

    public default boolean isVariadic() { return arity() == - 1; }

    public abstract Object call(Interpreter interpreter, List<Object> arguments);

    @Override
    public abstract String toString();
}
