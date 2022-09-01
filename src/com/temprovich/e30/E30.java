package com.temprovich.e30;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/*
 * https://timothya.com/pdfs/crafting-interpreters.pdf
 * https://drive.google.com/file/d/0B1MogsyNAsj9elVzQWR5NWVTSVE/view?resourcekey=0-zoBDMpzTafr6toxDuQLNUg
 */
public class E30 {

    private static final Interpreter interpreter = new Interpreter();
    
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    private E30 () {
    }

    public static void main(String[] args) throws IOException {
        args = new String[] { "scripts/test.e30" };
        if (args.length > 1) {
            System.out.println("Usage: e30 [script]");
            System.exit(64);
        }
        if (args.length == 1) {
            runScript(args[0]);
        } else {
            runInteractive();
        }
    }

    private static void runScript(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) {
            System.exit(65);
        }
        if (hadRuntimeError) {
            System.exit(70);
        }

        if (hadError) {
            System.exit(65);
        }
    }

    private static void runInteractive() throws IOException {
        InputStreamReader in = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(in);

        for (;;) {
            System.out.print("> ");
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

    public static void runtimeError(E30RuntimeException error) {
        System.err.println(error.getMessage() + "\n[line " + error.token().line() + "]");
        hadRuntimeError = true;
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
