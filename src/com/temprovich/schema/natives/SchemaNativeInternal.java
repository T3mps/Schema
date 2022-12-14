package com.temprovich.schema.natives;

import java.util.List;
import java.util.Map;

import com.temprovich.schema.Interpreter;
import com.temprovich.schema.instance.SchemaCallable;

public class SchemaNativeInternal implements SchemaNative {

    public SchemaNativeInternal() {
    }

    /*
     * clear(): Sends a clear screen command to the console.
     */
    private static final Definition CLEAR = new Definition("clear", new SchemaCallable() {
        
        @Override
        public int arity() { return 0; }
        
        @Override
        public Void call(Interpreter interpreter, List<Object> arguments) {
            System.out.print("\033[H\033[2J");
            System.out.flush();
            return null;
        }
        
        @Override
        public String toString() { return "<native function>"; }
    });

    @Override
    public void inject(Map<String, Object> environment) {
        CLEAR.inject(environment);
    }   
}
