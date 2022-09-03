package com.temprovich.e30;

import java.util.List;

public class E30Function implements E30Callable {

    private final Statement.Function declaration;

    E30Function(Statement.Function declaration) {
        this.declaration = declaration;
    }
    

    @Override
    public int arity() {
        return declaration.parameters().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.getGlobals());
        for (int i = 0; i < declaration.parameters().size(); i++) {
            environment.define(declaration.parameters().get(i).lexeme(), arguments.get(i));
        }
        
        interpreter.executeBlock(declaration.body(), environment);
        return null;
    }

    @Override
    public String toString() {
        return "<function " + declaration.name().lexeme() + '>';
    }
}
