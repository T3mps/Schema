package com.temprovich.e30;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    
    private Environment enclosing;
    private final Map<String, Object> values;

    public Environment() {
        this.enclosing = null;
        this.values = new HashMap<String, Object>();
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
        this.values = new HashMap<String, Object>();
    }

    protected Object fetch(Token name) {
        if (values.containsKey(name.lexeme())) {
            return values.get(name.lexeme());
        }

        // recursively walk up the environment chain to find the variable
        if (enclosing != null) {
            return enclosing.fetch(name);
        }

        throw new E30RuntimeException(name, "Undefined variable '" + name.lexeme() + "'.");
    }

    protected void define(String name, Object value) {
        validateIdentifier(name);
        values.put(name, value);
    }

    protected void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme())) {
            values.put(name.lexeme(), value);
            return;
        }

        // recursively walk up the environment chain to find the variable
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }
        
        throw new E30RuntimeException(name, "Undefined variable '" + name.lexeme() + "'.");
    }

    /*
     * Ensures the identifier is valid.
     *      1) must start with _, $, or a letter
     *      2) may contain only _, $, letters and numbers.
     *      3) must not be a reserved word.
     */
    private void validateIdentifier(String name) {
        if (name.length() < 1) {
            throw new E30RuntimeException("The identifier '" + name + "' is too short.");
        }
        char first = name.charAt(0);
        if (!Character.isLetter(first) && first != '_' && first != '$') {
            throw new E30RuntimeException("A valid identifier must begin with either '_', '$' or a letter.");
        }
        for (int i = 1; i < name.length(); i++) {
            if (!Character.isLetterOrDigit(name.charAt(i)) && name.charAt(i) != '_' && name.charAt(i) != '$' && name.charAt(i) != '-') {
                throw new E30RuntimeException("A valid identifier may only contain letters, numbers, '_', '$' or '-'.");
            }
        }
        
        // check for reserved words
        boolean valid = true;
        switch (name) {
            case Lexer.KW_AND      -> valid = false;
            case Lexer.KW_CLASS    -> valid = false;
            case Lexer.KW_ELSE     -> valid = false;
            case Lexer.KW_FALSE    -> valid = false;
            case Lexer.KW_FOR      -> valid = false;
            case Lexer.KW_FUNCTION -> valid = false;
            case Lexer.KW_IF       -> valid = false;
            case Lexer.KW_NULL     -> valid = false;
            case Lexer.KW_OR       -> valid = false;
            case Lexer.KW_PRINT    -> valid = false;
            case Lexer.KW_RETURN   -> valid = false;
            case Lexer.KW_SUPER    -> valid = false;
            case Lexer.KW_SELF     -> valid = false;
            case Lexer.KW_TRUE     -> valid = false;
            case Lexer.KW_AUTO     -> valid = false;
            case Lexer.KW_WHILE    -> valid = false;
            default                -> valid = true;
        }
        if (!valid) {
            throw new E30RuntimeException("The identifier '" + name + "' is a reserved word.");
        }

        // all checks passed
    }
}
