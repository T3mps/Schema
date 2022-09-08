package com.temprovich.schema;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.temprovich.schema.lexer.Lexer;
import com.temprovich.schema.lexer.Token;
import com.temprovich.schema.module.ModuleProcessor;
import com.temprovich.schema.report.ErrorReporter;
import com.temprovich.schema.report.ReportLibrary;

/*
 * https://timothya.com/pdfs/crafting-interpreters.pdf
 * https://drive.google.com/file/d/0B1MogsyNAsj9elVzQWR5NWVTSVE/view?resourcekey=0-zoBDMpzTafr6toxDuQLNUg
 */
public class Schema {

    public static final String[] EXTENSIONS = { ".sch", ".schema" };
    
    /*
     * Terminal exit codes
     */
    public static final int EXIT_CODE__SUCCESS = 0;
    public static final int EXIT_CODE__MALFORMED_ARGS = 64;
    public static final int EXIT_CODE__ERROR = 65;
    public static final int EXIT_CODE__RUNTIME_ERROR = 70;
    public static final int EXIT_CODE__ABORT = 75;

    public static final ErrorReporter reporter = ErrorReporter.fetch();
    private static final Interpreter interpreter = new Interpreter();

    private Schema () {
        throw new AssertionError("No instances of Schema");
    }

    public static void main(String[] args) throws IOException {
        int mode = args.length;

        switch (mode) {
            case 01 -> runScript(validate(args[0]));
            default -> terminate(ReportLibrary.MALFORMED_RUNTIME_ARGS, EXIT_CODE__MALFORMED_ARGS);
        }
    }

    private static void terminate(String message, int code) {
        System.err.println(message);
        System.exit(code);
    }

    private static String validate(String fileName) {
        if (fileName == null) {
            throw new IllegalArgumentException(ReportLibrary.NULL_FILE_NAME);
        }

        // check for extention
        boolean valid = false;
        String ext = fileName.substring(fileName.lastIndexOf("."));
        for (String extension : EXTENSIONS) {
            if (ext.equals(extension)) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new IllegalArgumentException(ErrorReporter.format(ReportLibrary.INVALID_FILE_EXTENSION, fileName, EXTENSIONS[0], EXTENSIONS[1]));
        }
        
        // check for file
        Path path = Paths.get(fileName);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException(ErrorReporter.format(ReportLibrary.NON_EXISTENT_FILE, fileName));
        }

        // process modules
        Preprocessor processor = new ModuleProcessor(path);
        System.out.println(processor.process());
        return "";
    }

    private static void runScript(String path) throws IOException {
        // byte[] bytes = Files.readAllBytes(Paths.get(path));
        // run(new String(bytes, Charset.defaultCharset()));

        if (reporter.hadError()) {
            System.exit(EXIT_CODE__ERROR);
        }
        if (reporter.hadRuntimeError()) {
            System.exit(EXIT_CODE__RUNTIME_ERROR);
        }

        System.exit(EXIT_CODE__SUCCESS);
    }

    private static void run(String src) {
        // Lexer lexer = new Lexer(src);
        // List<Token> tokens = lexer.tokenize();
        // Parser parser = new Parser(tokens);
        // List<Statement> statements = parser.parse();

        // if (reporter.hadError()) {
        //     return;
        // }

        // SemanticResolver resolver = new SemanticResolver(interpreter);
        // resolver.resolve(statements);

        // if (reporter.hadError()) {
        //     return;
        // }

        // interpreter.interpret(statements);
    }

    // public static void error(String message) {
    //     System.err.println(message);
    //     hadError = true;
    // }
    // public static void error(int line, String message) {
    //     report(line, "", message);
    // }
    
    // public static void error(Token token, String message) {
    //     if (token.type() == Token.Type.EOF) {
    //         report(token.line(), " at end", message);
    //     } else {
    //         report(token.line(), " at '" + token.lexeme() + "'", message);
    //     }
    // }

    // public static void runtimeError(SchemaRuntimeError error) {
    //     System.err.println(error.getMessage() + "\n[line " + error.token().line() + "]");
    //     hadRuntimeError = true;
    // }

    // private static void report(int line, String where, String message) {
    //     System.err.println("[line " + line + "] Error" + where + ": " + message);
    //     hadError = true;
    // }
}
