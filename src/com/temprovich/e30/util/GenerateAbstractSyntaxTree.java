package com.temprovich.e30.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAbstractSyntaxTree {
    
    private static final String INDENT = "    ";

    public static void main(String[] args) throws IOException {
        // if (args.length != 1) {
        //     System.err.println("Usage: generate_ast <output directory>");
        //     System.exit(64);
        // }

        // String outputDir = args[0];
        String outputDir = "src/com/temprovich/e30";
        define(outputDir, "Expression", Arrays.asList(
            "Literal  : Object value",
            "Grouping  : Expression expression",
            "Variable  : Token name",
            "Assign    : Token name, Expression value",
            "Unary     : Token operator, Expression right",
            "Binary    : Expression left, Token operator, Expression right",
            "Logical   : Expression left, Token operator, Expression right",
            "Call      : Expression callee, Token paren, List<Expression> arguments",
            "Function  : List<Token> parameters, List<Statement> body",
            "Attribute : Expression object, Token name",
            "Set       : Expression object, Token name, Expression value",
            "Self      : Token keyword",
            "Parent    : Token keyword, Token method"
        ));

        define(outputDir, "Statement", Arrays.asList(
            "Block    : List<Statement> statements",
            "Expr     : Expression expression",
            "Node     : Token name, Expression.Variable parent, List<Expression> traits, List<Statement.Function> methods, List<Statement.Function> metaMethods",
            "Trait    : Token name, List<Expression> traits, List<Statement.Function> methods",
            "Function : Token name, Expression.Function function",
            "Auto     : Token name, Expression value",
            "If       : Expression condition, Statement thenBranch, Statement elseBranch",
            "Return   : Token keyword, Expression value",
            "While    : Expression condition, Statement body",
            "Break    : ",
            "Continue : "
        ));
    }
    
    private static void define(String outputDirectory, String baseName, List<String> types) throws IOException {
        String path = outputDirectory + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package com.temprovich.e30;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("public abstract class " + baseName + " {");
        writer.println();

        defineVisitor(writer, baseName, types);

        for (var type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);

        }
        
        writer.println(INDENT + "public abstract <R> R accept(Visitor<R> visitor);");

        writer.println("}");
        writer.println();

        writer.close();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fields) {
        writer.println(INDENT + "public static class " + className + " extends " + baseName + " {");
        
        // Fields
        writer.println();
        String[] fieldNames;
        if (fields.isEmpty()) {
            fieldNames = new String[0];
        } else {
            fieldNames = fields.split(", ");
        }
        for (String fieldName : fieldNames) {
            writer.println(INDENT + INDENT + "private final " + fieldName + ";");
        }
        
        // Constructor
        writer.println();
        writer.println(INDENT + INDENT + "public " + className + "(" + fields + ") {");
        for (String fieldName : fieldNames) {
            String name = fieldName.split(" ")[1];
            writer.println(INDENT + INDENT + INDENT + "this." + name + " = " + name + ";");
        }
        writer.println(INDENT + INDENT + "}");
        
        // Getters
        writer.println();
        for (int i = 0; i < fieldNames.length; i++) {
            String fieldName = fieldNames[i];
            String name = fieldName.split(" ")[1];
            writer.println(INDENT + INDENT + "public " + fieldName + "() {");
            writer.println(INDENT + INDENT + INDENT + "return " + name + ";");
            writer.println(INDENT + INDENT + "}");
            
            if (i < fieldNames.length - 1) {
                writer.println();
            }
        }

        // Visitor
        writer.println();
        writer.println(INDENT + INDENT + "@Override");
        writer.println(INDENT + INDENT + "public <R> R accept(Visitor<R> visitor) {");
        writer.println(INDENT + INDENT + INDENT + "return visitor.visit(this);");
        writer.println(INDENT + INDENT + "}");
        
        writer.println(INDENT + "}");
        writer.println();
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println(INDENT + "public interface Visitor<R> {");
        for (String type : types) {
            String className = type.split(":")[0].trim();
            writer.println();
            writer.println(INDENT + INDENT + "public abstract R visit(" + className + " " + baseName.toLowerCase() + ");");
        }
        writer.println(INDENT + "}");
        writer.println();
    }
}
