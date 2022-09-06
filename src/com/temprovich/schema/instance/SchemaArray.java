package com.temprovich.schema.instance;

import com.temprovich.schema.error.SchemaRuntimeError;
import com.temprovich.schema.lexer.Token;

public class SchemaArray extends SchemaInstance {

    private final Object[] elements;
    private final int length;

    public SchemaArray(int length) {
        super(null);
        this.elements = new Object[length];
        this.length = length;
    }

    public SchemaArray(Object[] elements) {
        super(null);
        this.elements = elements;
        this.length = elements.length;
    }

    @Override
    public Object get(Token name) {
        if (name.lexeme().equals("length")) {
            return (double) length;
        }
        
        throw new SchemaRuntimeError(name, "Undefined property '" + name.lexeme() + "'.");
    }

    @Override
    public void set(Token name, Object value) {
        throw new SchemaRuntimeError(name, "Cannot add attributes to an array.");
    }

    public Object getValue(int index) {
        return elements[index];
    }

    public void setValue(int index, Object value) {
        elements[index] = value;
    }

    public Object[] getElements() {
        return elements;
    }

    public int length() {
        return length;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < length; i++) {
            sb.append(elements[i]);
            if (i < length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
