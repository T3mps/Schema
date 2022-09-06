package com.temprovich.schema.instance;

import java.util.List;
import java.util.Map;

import com.temprovich.schema.Interpreter;

public class SchemaNode extends SchemaInstance implements SchemaCallable {
    
    private final String name;
    private final SchemaNode parent;
    private final Map<String, SchemaFunction> methods;

    public SchemaNode(SchemaNode metaNode, SchemaNode parent, String name, Map<String, SchemaFunction> methods) {
        super(metaNode);
        this.name = name;
        this.parent = parent;
        this.methods = methods;
    }

    public SchemaFunction fetchMethod(String name) {
        SchemaFunction method = methods.get(name);
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
        SchemaFunction definition = methods.get("define");
        if (definition == null) {
            return 0;
        }
        return definition.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        SchemaInstance instance = new SchemaInstance(this);
        SchemaFunction initializer = fetchMethod("define");
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
