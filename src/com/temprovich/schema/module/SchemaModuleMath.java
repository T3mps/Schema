package com.temprovich.schema.module;

import java.util.Map;

public class SchemaModuleMath implements SchemaModule {

    public SchemaModuleMath() {
    }

    private static final Definition PI = new Definition("PI", Math.PI);

    @Override
    public void inject(Map<String, Object> environment) {
        environment.put(PI.name(), PI.value());
    }
}
