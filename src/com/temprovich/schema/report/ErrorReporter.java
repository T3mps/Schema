package com.temprovich.schema.report;

import java.util.concurrent.atomic.AtomicBoolean;

import com.temprovich.schema.error.SchemaRuntimeError;
import com.temprovich.schema.lexer.Token;

public final class ErrorReporter {

    private static ErrorReporter instance = null;

    private AtomicBoolean hadError = new AtomicBoolean(false);
    private AtomicBoolean hadRuntimeError = new AtomicBoolean(false);

    private ErrorReporter() {
    }

    public static ErrorReporter fetch() {
        if (instance == null) {
            instance = new ErrorReporter();
        }
        return instance;
    }

    public void error(String message, String... args) {
        System.err.println(format(message, args));
        hadError.set(true);
    }

    public void error(int line, String message) {
        report(line, "", message);
    }
    
    public void error(Token token, String message) {
        switch (token.type()) {
            case EOF -> report(token.line(), " at end", message);
            default  -> report(token.line(), "at '" + token.lexeme() + "'", message);
        }
    }

    public void runtimeError(SchemaRuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token().line() + "]");
        hadRuntimeError.set(true);
    }

    /*
     * Error message should consist of 3 parts: problem identification,
     * cause details if helpful, and a solution if possible
     */
    private void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error " + where + ": " + format(message));
        hadError.set(true);
    }

    public static String format(String message, String... args) {
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", args[i]);
        }
        return message;
    }

    public boolean hadError() {
        return hadError.get();
    }

    public boolean hadRuntimeError() {
        return hadRuntimeError.get();
    }
}
