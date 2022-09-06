package com.temprovich.e30.instance;

import java.util.ArrayList;
import java.util.List;

import com.temprovich.e30.Interpreter;
import com.temprovich.e30.error.E30RuntimeError;
import com.temprovich.e30.lexer.Token;

public class E30List extends E30Instance {

    private final List<Object> elements;

    public E30List(List<Object> elements) {
        super(null);
        this.elements = new ArrayList<Object>();
        if (elements != null) {
            for (var element : elements) {
                this.elements.add(element);
            }
        }
    }

    public E30List(E30List list) {
        super(null);
        this.elements = new ArrayList<Object>();
        for (var element : list.elements) {
            this.elements.add(element);
        }
    }

    @Override
    public Object get(Token name) {
        // get(index)
        if (name.lexeme().equals("get")) {
            return new E30Callable() {
                @Override
                public int arity() { return 1; }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    int index = (int)(double) arguments.get(0);
                    return elements.get(index);
                }
            };
        }

        // set(index, value)
        if (name.lexeme().equals("set")) {
            return new E30Callable() {
                @Override
                public int arity() { return 2; }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    int index = (int)(double) arguments.get(0);
                    Object value = arguments.get(1);
                    return elements.set(index, value);
                }
            };
        }

        // size()
        if (name.lexeme().equals("size")) {
            return new E30Callable() {
                @Override
                public int arity() { return 0; }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    return (double) elements.size();
                }
            };
        }

        // add(value)
        if (name.lexeme().equals("add")) {
            return new E30Callable() {
                @Override
                public int arity() { return 1; }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    Object value = arguments.get(0);
                    elements.add(value);
                    return null;
                }
            };
        }

        // remove(index)
        if (name.lexeme().equals("remove")) {
            return new E30Callable() {
                @Override
                public int arity() { return 1; }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    int index = (int)(double) arguments.get(0);
                    return elements.remove(index);
                }
            };
        }

        // clear()
        if (name.lexeme().equals("clear")) {
            return new E30Callable() {
                @Override
                public int arity() { return 0; }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    elements.clear();
                    return null;
                }
            };
        }

        // has(value)
        if (name.lexeme().equals("has")) {
            return new E30Callable() {
                @Override
                public int arity() { return 1; }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    Object value = arguments.get(0);
                    return elements.contains(value);
                }
            };
        }

        // slice(start, end)
        if (name.lexeme().equals("slice")) {
            return new E30Callable() {
                @Override
                public int arity() { return 2; }

                @Override
                public Object call(Interpreter interpreter, List<Object> arguments) {
                    int start = (int)(double) arguments.get(0);
                    int end = (int)(double) arguments.get(1);
                    return new E30List(elements.subList(start, end));
                }
            };
        }

        throw new E30RuntimeError(name, "Undefined attribute '" + name.lexeme() + "'.");
    }

    @Override
    public void set(Token name, Object value) {
        throw new E30RuntimeError(name, "Cannot add attributes to a list.");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("list[");
        int size = elements.size();

        for (int i = 0; i < size; i++) {
            sb.append(Interpreter.stringify(elements.get(i)));
            if (i < size - 1) {
                sb.append(", ");
            }
        }

        sb.append("]");
        return sb.toString();
    }
}
