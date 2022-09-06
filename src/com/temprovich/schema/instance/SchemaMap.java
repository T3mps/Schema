package com.temprovich.schema.instance;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.temprovich.schema.Interpreter;
import com.temprovich.schema.error.SchemaRuntimeError;
import com.temprovich.schema.lexer.Token;

public class SchemaMap extends SchemaInstance {

    private final Map<Object, Object> elements;

    public SchemaMap(List<Object> elements) {
        super(null);
        if (elements.size() % 2 != 0) {
            throw new SchemaRuntimeError("Map initializer has " + elements.size() + " elements, but should have " + (elements.size() + 1) + " | " + (elements.size() - 1) + " elements.");
        }
        this.elements = new HashMap<Object, Object>();
        if (elements != null) {
            for (int i = 0; i < elements.size(); i += 2) {
                this.elements.put(elements.get(i), elements.get(i + 1));
            }
        }
    }
    
    public SchemaMap(SchemaMap map) {
        super(null);
        this.elements = new HashMap<Object, Object>();
        for (var entry : map.elements.entrySet()) {
            this.elements.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public Object get(Token name) {
        // get(key)
        if (name.lexeme().equals("get")) {
            return new SchemaCallable() {
                @Override
                public int arity() { return 1; }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    Object key = arguments.get(0);
                    return elements.getOrDefault(key, null);
                }
            };
        }

        // set(key, value)
        if (name.lexeme().equals("set")) {
            return new SchemaCallable() {
                @Override
                public int arity() { return 2; }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    Object key = arguments.get(0);
                    Object value = arguments.get(1);
                    return elements.put(key, value);
                }
            };
        }

        // size()
        if (name.lexeme().equals("size")) {
            return new SchemaCallable() {
                @Override
                public int arity() { return 0; }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    return (double) elements.size();
                }
            };
        }

        // put(key, value)
        if (name.lexeme().equals("put")) {
            return new SchemaCallable() {
                @Override
                public int arity() { return 2; }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    Object key = arguments.get(0);
                    Object value = arguments.get(1);
                    return elements.put(key, value);
                }
            };
        }

        // remove(key)
        if (name.lexeme().equals("remove")) {
            return new SchemaCallable() {
                @Override
                public int arity() { return 1; }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    Object key = arguments.get(0);
                    return elements.remove(key);
                }
            };
        }

        // clear()
        if (name.lexeme().equals("clear")) {
            return new SchemaCallable() {
                @Override
                public int arity() { return 0; }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    elements.clear();
                    return null;
                }
            };
        }

        // hasKey(key)
        if (name.lexeme().equals("has_key")) {
            return new SchemaCallable() {
                @Override
                public int arity() { return 1; }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    Object key = arguments.get(0);
                    return elements.containsKey(key);
                }
            };
        }

        // hasValue(value)
        if (name.lexeme().equals("has_value")) {
            return new SchemaCallable() {
                @Override
                public int arity() { return 1; }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    Object value = arguments.get(0);
                    return elements.containsValue(value);
                }
            };
        }

        // keys()
        if (name.lexeme().equals("keys")) {
            return new SchemaCallable() {
                @Override
                public int arity() { return 0; }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    return new SchemaList(Arrays.asList(elements.keySet().toArray()));
                }
            };
        }

        // values()
        if (name.lexeme().equals("values")) {
            return new SchemaCallable() {
                @Override
                public int arity() { return 0; }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    return new SchemaList(Arrays.asList(elements.values().toArray()));
                }
            };
        }

        throw new SchemaRuntimeError("Undefined attribute '" + name.lexeme() + "'.");
    }

    @Override
    public void set(Token name, Object value) {
        throw new SchemaRuntimeError(name, "Cannot add attributes to a list.");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("map[");
        for (var entry : elements.entrySet()) {
            sb.append(Interpreter.stringify(entry.getKey()));
            sb.append(": ");
            sb.append(Interpreter.stringify(entry.getValue()));
            sb.append(", ");
        }
        if (sb.length() > 4) {
            sb.delete(sb.length() - 2, sb.length());
        }
        sb.append("]");
        return sb.toString();
    }
}
