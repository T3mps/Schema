package com.temprovich.e30.module;

import java.util.Map;

public class E30ModuleMath implements E30Module {

    public E30ModuleMath() {
    }

    private static final Definition PI = new Definition("PI", Math.PI);

    @Override
    public void inject(Map<String, Object> environment) {
        environment.put(PI.name(), PI.value());
    }
}
