package com.temprovich.e30.instance;

import java.util.HashMap;
import java.util.Map;

import com.temprovich.e30.error.E30RuntimeError;
import com.temprovich.e30.lexer.Token;

public class E30Instance {
    
    private E30Node node;
    private final Map<String, Object> fields;

    public E30Instance(E30Node node) {
        this.node = node;
        this.fields = new HashMap<String, Object>();
    }

    public Object get(Token name) {
        if (fields.containsKey(name.lexeme())) {
            return fields.get(name.lexeme());
        }

        E30Function method = node.fetchMethod(name.lexeme());
        if (method != null) {
            return method.bind(this);
        }

        throw new E30RuntimeError(name, "Undefined attribute '" + name.lexeme() + "'.");
    }

    public void set(Token name, Object value) {
        fields.put(name.lexeme(), value);
    }

    E30Node node() {
        return node;
    }

    @Override
    public String toString() {
        return node.name() + " instance";
    }
}
