package com.temprovich.e30.preinclude;

import java.util.List;

import com.temprovich.e30.E30;
import com.temprovich.e30.E30Callable;
import com.temprovich.e30.Environment;
import com.temprovich.e30.Interpreter;

public final class E30Native_Base {
    
    private static final Environment ENV = new Environment();
    static {
        /*
         * abort(x): Stops a program abnormally with a specified error code.
         */
        ENV.define("abort", new E30Callable() {

            @Override
            public int arity() { return 1; }
            
            @Override
            public Void call(Interpreter interpreter, List<Object> arguments) {
                System.exit((int) arguments.get(0));
                return null;
            }

            @Override
            public String toString() { return "<native function>"; }
        });

        /*
         * abort(): Stops a program abnormally.
         */
        ENV.define("abort", new E30Callable() {

            @Override
            public int arity() { return 0; }
            
            @Override
            public Void call(Interpreter interpreter, List<Object> arguments) {
                System.exit(E30.EXIT_CODE__ABORT);
                return null;
            }

            @Override
            public String toString() { return "<native function>"; }
        });

        /*
         * exit() exits the program normally
         */
        ENV.define("exit", new E30Callable() {

            @Override
            public int arity() { return 0; }
            
            @Override
            public Void call(Interpreter interpreter, List<Object> arguments) {
                System.exit(E30.EXIT_CODE__SUCCESS);
                return null;
            }

            @Override
            public String toString() { return "<native function>"; }
        });

        /*
         * print(x) prints the given value to the console with a newline
         */
        ENV.define("print", new E30Callable() {

            @Override
            public int arity() { return 1; }
            
            @Override
            public Void call(Interpreter interpreter, List<Object> arguments) {
                System.out.println(Interpreter.stringify(arguments.get(0)));
                return null;
            }

            @Override
            public String toString() { return "<native function>"; }
        });
    }

    public static Environment getEnvironment() {
        return ENV;
    }
}
