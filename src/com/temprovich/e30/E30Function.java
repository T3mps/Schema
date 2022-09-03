package com.temprovich.e30;

import java.util.List;

public class E30Function implements E30Callable {

    private final Statement.Function declaration;
    private final Environment closure;

    E30Function(Statement.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }
    

    @Override
    public int arity() {
        return declaration.parameters().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.parameters().size(); i++) {
            environment.define(declaration.parameters().get(i).lexeme(), arguments.get(i));
        }
        
        try {
            interpreter.executeBlock(declaration.body(), environment);
        } catch (Return returnValue) {
            return returnValue.value();
        }
        return null;
    }

    @Override
    public String toString() {
        return "<function " + declaration.name().lexeme() + '>';
    }
}
