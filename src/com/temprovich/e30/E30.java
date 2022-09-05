package com.temprovich.e30;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.temprovich.e30.error.E30RuntimeError;

/*
 * https://timothya.com/pdfs/crafting-interpreters.pdf
 * https://drive.google.com/file/d/0B1MogsyNAsj9elVzQWR5NWVTSVE/view?resourcekey=0-zoBDMpzTafr6toxDuQLNUg
 */
public class E30 {
    
    /*
     * Terminal exit codes
     */
    public static final int EXIT_CODE__SUCCESS = 0;
    public static final int EXIT_CODE__MALFORMED_ARGS = 64;
    public static final int EXIT_CODE__ERROR = 65;
    public static final int EXIT_CODE__RUNTIME_ERROR = 70;
    public static final int EXIT_CODE__ABORT = 75;

    private static final Interpreter interpreter = new Interpreter();
    
    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;

    private E30 () {
        throw new AssertionError("No instances of E30");
    }

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: e30 [script]");
            System.exit(EXIT_CODE__MALFORMED_ARGS);
        }
        if (args.length == 1) {
            runScript(args[0]);
            return;
        }
        
        runInteractive();
    }

    private static void runScript(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) {
            System.exit(EXIT_CODE__ERROR);
        }
        if (hadRuntimeError) {
            System.exit(EXIT_CODE__RUNTIME_ERROR);
        }

        System.exit(EXIT_CODE__SUCCESS);
    }

    private static void runInteractive() throws IOException {
        InputStreamReader in = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(in);

        for (;;) {
            System.out.print(">\s");
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            run(line);
        }
    }

    private static void run(String src) {
        Lexer lexer = new Lexer(src);
        List<Token> tokens = lexer.tokenize();
        Parser parser = new Parser(tokens);
        List<Statement> statements = parser.parse();

        if (hadError) {
            return;
        }

        SemanticResolver resolver = new SemanticResolver(interpreter);
        resolver.resolve(statements);

        if (hadError) {
            return;
        }

        interpreter.interpret(statements);
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }
    
    public static void error(Token token, String message) {
        if (token.type() == TokenType.EOF) {
            report(token.line(), " at end", message);
        } else {
            report(token.line(), " at '" + token.lexeme() + "'", message);
        }
    }

    public static void runtimeError(E30RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token().line() + "]");
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
