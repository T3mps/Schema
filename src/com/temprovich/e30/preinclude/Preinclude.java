package com.temprovich.e30.preinclude;

import com.temprovich.e30.Environment;

public interface Preinclude {
    
    public abstract void inject(Environment environment);

    public record ConstantDefinition(String name, Object value) implements Preinclude {
        @Override
        public void inject(Environment environment) {
            environment.define(name, value);
        }
    }

    public record Definition(String name, Object value) {

        public void inject(Environment environment) {
            environment.define(name, value);
        }
    }
}
