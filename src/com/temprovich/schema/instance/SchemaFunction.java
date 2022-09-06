package com.temprovich.schema.instance;

import java.util.List;

import com.temprovich.schema.Environment;
import com.temprovich.schema.Interpreter;
import com.temprovich.schema.Statement;
import com.temprovich.schema.throwables.ReturnException;

public class SchemaFunction implements SchemaCallable {

    private final Statement.Function declaration;
    private final Environment closure;
    private final boolean isDefinition;

    public SchemaFunction(Statement.Function declaration, Environment closure, boolean isDefinition) {
        this.declaration = declaration;
        this.closure = closure;
        this.isDefinition = isDefinition;
    }

    public SchemaFunction bind(SchemaInstance e30Instance) {
        Environment environment = new Environment(closure);
        environment.define(e30Instance);
        return new SchemaFunction(declaration, environment, isDefinition);
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
        } catch (ReturnException returnValue) {
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
