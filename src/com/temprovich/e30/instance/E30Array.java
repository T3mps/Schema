package com.temprovich.e30.instance;

import com.temprovich.e30.error.E30RuntimeError;
import com.temprovich.e30.lexer.Token;

public class E30Array extends E30Instance {

    private final Object[] elements;
    private final int length;

    public E30Array(int length) {
        super(null);
        this.elements = new Object[length];
        this.length = length;
    }

    public E30Array(Object[] elements) {
        super(null);
        this.elements = elements;
        this.length = elements.length;
    }

    @Override
    public Object get(Token name) {
        if (name.lexeme().equals("length")) {
            return (double) length;
        }
        
        throw new E30RuntimeError(name, "Undefined property '" + name.lexeme() + "'.");
    }

    @Override
    public void set(Token name, Object value) {
        throw new E30RuntimeError(name, "Cannot add attributes to an array.");
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
