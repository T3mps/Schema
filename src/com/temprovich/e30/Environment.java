package com.temprovich.e30;

import java.util.ArrayList;
import java.util.List;

public class Environment {
    
    private Environment enclosing;
    private final List<Object> values;

    public Environment() {
        this(null);
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
        this.values = new ArrayList<Object>();
    }

    public Object fetch(int distance, int slot) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }

        return environment.values.get(slot);
    }

    public void define(Object value) {
        values.add(value);
    }

    public void assign(int distance, int slot, Object value) {
        Environment environment = ancestor(distance);
        environment.values.set(slot, value);
    }

    private Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }

        return environment;
    }

    public Environment enclosing() {
        return enclosing;
    }
}
