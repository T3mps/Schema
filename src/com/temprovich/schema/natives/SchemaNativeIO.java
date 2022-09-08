package com.temprovich.schema.natives;

import java.util.List;
import java.util.Map;

import com.temprovich.schema.Interpreter;
import com.temprovich.schema.instance.SchemaCallable;

public class SchemaNativeIO implements SchemaNative {

    public SchemaNativeIO() {
    }

    /*
     * scan(): Reads a line from the console.
     */
    private static final Definition SCAN = new Definition("scan", new SchemaCallable() {

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
