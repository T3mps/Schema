package com.temprovich.e30.preinclude;

import java.util.List;

import com.temprovich.e30.E30Callable;
import com.temprovich.e30.Environment;
import com.temprovich.e30.Interpreter;

public class E30NativeInternal implements Preinclude {

    /*
     * clear(): Sends a clear screen command to the console.
     */
    private static final Definition CLEAR = new Definition("clear", new E30Callable() {
        
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
    public void inject(Environment environment) {
        CLEAR.inject(environment);
    }   
}
