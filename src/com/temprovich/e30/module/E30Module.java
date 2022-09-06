package com.temprovich.e30.module;

import java.util.Map;

public interface E30Module {
    
    public abstract void inject(Map<String, Object> environment);

    public record ConstantDefinition(String name, Object value) implements E30Module {
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
