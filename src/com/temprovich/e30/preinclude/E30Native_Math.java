package com.temprovich.e30.preinclude;

import com.temprovich.e30.Environment;

public final class E30Native_Math {
    
    private static final Environment ENV = new Environment();
    static {
        ENV.define("PI", Math.PI);
        ENV.define("E", Math.E);
    }

    public static Environment getEnvironment() {
        return ENV;
    }
}
