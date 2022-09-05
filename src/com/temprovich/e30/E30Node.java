package com.temprovich.e30;

import java.util.List;
import java.util.Map;

public class E30Node extends E30Instance implements E30Callable {
    
    private final String name;
    private final E30Node parent;
    private final Map<String, E30Function> methods;

    public E30Node(E30Node metaNode, E30Node parent, String name, Map<String, E30Function> methods) {
        super(metaNode);
        this.name = name;
        this.parent = parent;
        this.methods = methods;
    }

    public E30Function fetchMethod(String name) {
        E30Function method = methods.get(name);
        if (method != null) {
            return method;
        }
        if (parent != null) {
            return parent.fetchMethod(name);
        }
        
        return null;
    }

    @Override
    public int arity() {
        E30Function definition = methods.get("define");
        if (definition == null) {
            return 0;
        }
        return definition.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        E30Instance instance = new E30Instance(this);
        E30Function initializer = fetchMethod("define");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }
        
        return instance;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
