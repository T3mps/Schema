package com.temprovich.e30.preinclude;

import java.util.List;
import java.util.Map;

import com.temprovich.e30.E30;
import com.temprovich.e30.E30Callable;
import com.temprovich.e30.Environment;
import com.temprovich.e30.Interpreter;

public final class E30NativeBase implements Preinclude {
    
    /*
     * abort(): Stops a program abnormally.
     */
    private static final Definition ABORT_0 = new Definition("abort", new E30Callable() {

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
     * abort(x): Stops a program abnormally with a specified error code.
     */
    private static final Definition ABORT_1 = new Definition("abort", new E30Callable() {

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
     * exit() exits the program normally
     */
    private static final Definition EXIT = new Definition("exit", new E30Callable() {

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
     * print(x): Prints a value to the console.
     */
    private static final Definition PRINT = new Definition("print", new E30Callable() {

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

    /*
     * now(): Returns the current time in milliseconds.
     */
    private static final Definition NOW = new Definition("now", new E30Callable() {

        @Override
        public int arity() { return 0; }
        
        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return (double) System.currentTimeMillis();
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * wait(x): Waits for x milliseconds.
     */
    private static final Definition WAIT = new Definition("wait", new E30Callable() {

        @Override
        public int arity() { return 1; }
        
        @Override
        public Void call(Interpreter interpreter, List<Object> arguments) {
            try {
                double millis = (Double) arguments.get(0);
                Thread.sleep((long) millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    /*
     * type(x): Returns the type of a value.
     */
    private static final Definition TYPE = new Definition("type", new E30Callable() {

        @Override
        public int arity() { return 1; }
        
        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return interpreter.typeOf(arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    public void inject(Map<String, Object> environment) {
        ABORT_0.inject(environment);
        ABORT_1.inject(environment);
        EXIT.inject(environment);
        PRINT.inject(environment);
        SCAN.inject(environment);
        NOW.inject(environment);
        WAIT.inject(environment);
        TYPE.inject(environment);
    }
}
