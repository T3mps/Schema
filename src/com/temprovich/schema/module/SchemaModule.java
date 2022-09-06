package com.temprovich.schema.module;

import java.util.Map;

public interface SchemaModule {
    
    public abstract void inject(Map<String, Object> environment);

    public record ConstantDefinition(String name, Object value) implements SchemaModule {
        @Override
        public void inject(Map<String, Object> environment) {
            environment.put(name, value);
        }
    }

    public record Definition(String name, Object value) {

        public void inject(Map<String, Object> environment) {
            environment.put(name, value);
        }
    }
}
