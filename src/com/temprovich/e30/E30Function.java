package com.temprovich.e30;

import java.util.List;

public class E30Function implements E30Callable {

    private final Statement.Function declaration;
    private final Environment closure;
    private final boolean isDefinition;

    public E30Function(Statement.Function declaration, Environment closure, boolean isDefinition) {
        this.declaration = declaration;
        this.closure = closure;
        this.isDefinition = isDefinition;
    }

    public E30Function bind(E30Instance e30Instance) {
        Environment environment = new Environment(closure);
        environment.define(e30Instance);
        return new E30Function(declaration, environment, isDefinition);
    }
    
    @Override
    public int arity() {
        return declaration.function().parameters().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        if (declaration.function().parameters() != null) {
            int size = declaration.function().parameters().size();
            for (int i = 0; i < size; i++) {
                environment.define(arguments.get(i));
            }       
        }

        try {
            interpreter.executeBlock(declaration.function().body(), environment);
        } catch (Return returnValue) {
            if (isDefinition) {
                return closure.fetch(0, 0);
            }
            return returnValue.value();
        }

        if (isDefinition) {
            return closure.fetch(0, 0);
        }
        return null;
    }

    public boolean isGetter() {
        return declaration.function().parameters() == null;
    }

    @Override
    public String toString() {
        return "<fn + " + declaration.name().lexeme() + ">";
    }
}
