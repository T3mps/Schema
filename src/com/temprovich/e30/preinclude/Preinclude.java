package com.temprovich.e30.preinclude;

import java.util.Map;

public interface Preinclude {
    
    public abstract void inject(Map<String, Object> environment);

    public record ConstantDefinition(String name, Object value) implements Preinclude {
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
