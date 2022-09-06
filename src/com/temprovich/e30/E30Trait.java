package com.temprovich.e30;

import java.util.Map;

import com.temprovich.e30.instance.E30Function;
import com.temprovich.e30.lexer.Token;

public class E30Trait {
    
    private final Token name;
    private final Map<String, E30Function> methods;

    public E30Trait(Token name, Map<String, E30Function> methods) {
        this.name = name;
        this.methods = methods;
    }

    public Token name() {
        return name;
    }

    public Map<String, E30Function> methods() {
        return methods;
    }

    @Override
    public String toString() {
        return name.lexeme();
    }
}
