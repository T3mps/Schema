package com.temprovich.schema.natives;

import java.util.Map;

public class SchemaNativeMath implements SchemaNative {

    public SchemaNativeMath() {
    }

    private static final Definition PI = new Definition("PI", Math.PI);

    @Override
    public void inject(Map<String, Object> environment) {
        environment.put(PI.name(), PI.value());
    }
}
