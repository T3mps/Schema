package com.temprovich.e30;

import java.util.List;

public interface E30Callable {
    
    public abstract int arity();

    public abstract Object call(Interpreter interpreter, List<Object> arguments);

    @Override
    public abstract String toString();
}
