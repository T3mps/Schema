package com.temprovich.schema;

import java.util.Map;

import com.temprovich.schema.instance.SchemaFunction;
import com.temprovich.schema.lexer.Token;

public class SchemaTrait {
    
    private final Token name;
    private final Map<String, SchemaFunction> methods;

    public SchemaTrait(Token name, Map<String, SchemaFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    public Token name() {
        return name;
    }

    public Map<String, SchemaFunction> methods() {
        return methods;
    }

    @Override
    public String toString() {
        return name.lexeme();
    }
}
