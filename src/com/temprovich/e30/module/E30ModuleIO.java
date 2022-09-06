package com.temprovich.e30.module;

import java.util.List;
import java.util.Map;

import com.temprovich.e30.Interpreter;
import com.temprovich.e30.instance.E30Callable;

public class E30ModuleIO implements E30Module {

    public E30ModuleIO() {
    }

    /*
     * scan(): Reads a line from the console.
     */
    private static final Definition SCAN = new Definition("scan", new E30Callable() {

        @Override
        public int arity() { return 0; }
        
        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return System.console().readLine();
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    @Override
    public void inject(Map<String, Object> environment) {
        SCAN.inject(environment);
    }
}
