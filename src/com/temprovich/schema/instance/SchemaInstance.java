package com.temprovich.schema.instance;

import java.util.HashMap;
import java.util.Map;

import com.temprovich.schema.error.SchemaRuntimeError;
import com.temprovich.schema.lexer.Token;

public class SchemaInstance {
    
    private SchemaNode node;
    private final Map<String, Object> fields;

    public SchemaInstance(SchemaNode node) {
        this.node = node;
        this.fields = new HashMap<String, Object>();
    }

    public Object get(Token name) {
        if (fields.containsKey(name.lexeme())) {
            return fields.get(name.lexeme());
        }

        SchemaFunction method = node.fetchMethod(name.lexeme());
        if (method != null) {
            return method.bind(this);
        }

        throw new SchemaRuntimeError(name, "Undefined attribute '" + name.lexeme() + "'.");
    }

    public void set(Token name, Object value) {
        fields.put(name.lexeme(), value);
    }

    SchemaNode node() {
        return node;
    }

    @Override
    public String toString() {
        return node.name() + " instance";
    }
}
