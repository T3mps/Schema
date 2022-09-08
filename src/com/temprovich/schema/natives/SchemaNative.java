package com.temprovich.schema.natives;

import java.util.Map;

public interface SchemaNative {
    
    public abstract void inject(Map<String, Object> environment);

    public record ConstantDefinition(String name, Object value) implements SchemaNative {
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
