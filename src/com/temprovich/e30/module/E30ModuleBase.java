package com.temprovich.e30.module;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.temprovich.e30.E30;
import com.temprovich.e30.Interpreter;
import com.temprovich.e30.error.E30RuntimeError;
import com.temprovich.e30.instance.E30Callable;
import com.temprovich.e30.instance.E30List;
import com.temprovich.e30.instance.E30Map;

public final class E30ModuleBase implements E30Module {
    
    public E30ModuleBase() {
    }

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
     * read_file(path): Reads a file from the filesystem and returns its contents as a string.
     */
    private static final Definition READ_FILE = new Definition("read_file", new E30Callable() {

        @Override
        public int arity() { return 1; }
        
        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            try {
                String path = (String) arguments.get(0);
                byte[] bytes = Files.readAllBytes(Paths.get(path));
                return new String(bytes, Charset.defaultCharset());
            } catch (IOException e) {
                throw new E30RuntimeError("Could not read file: " + e.getMessage());
            }
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

    private static final Definition LIST = new Definition("create_list", new E30Callable() {

        @Override
        public int arity() { return -1; } // variadic
        
        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            if (arguments.size() == 1) {
                // check for list
                if (arguments.get(0) instanceof E30List) {
                    return new E30List((E30List) arguments.get(0));
                }
            }

            return new E30List(arguments.size() > 0 ? arguments : null);
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    private static final Definition MAP = new Definition("create_map", new E30Callable() {

        @Override
        public int arity() { return -1; } // variadic
        
        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            if (arguments.size() == 1) {
                // check for map
                if (arguments.get(0) instanceof E30Map) {
                    return new E30Map((E30Map) arguments.get(0));
                }
            }
            
            return new E30Map(arguments.size() > 0 ? arguments : null);
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
            return Interpreter.typeOf(arguments.get(0));
        }

        @Override
        public String toString() { return "<native function>"; }
    });

    public void inject(Map<String, Object> environment) {
        ABORT_0.inject(environment);
        ABORT_1.inject(environment);
        EXIT.inject(environment);
        PRINT.inject(environment);
        READ_FILE.inject(environment);
        NOW.inject(environment);
        LIST.inject(environment);
        MAP.inject(environment);
        WAIT.inject(environment);
        TYPE.inject(environment);
    }
}
