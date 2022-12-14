package com.temprovich.schema.lexer;

public record Token(Token.Type type, String lexeme, Object literal, int line) {

    public static enum Type {
    
        // Single-character tokens.
        LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
        LEFT_SQUARE_BRACKET, RIGHT_SQUARE_BRACKET,
        COMMA, DOT, SEMICOLON, COLON, COLON_COLON,
    
        // One or two character tokens.
        MINUS, MINUS_EQUAL, MINUS_MINUS,
        PLUS, PLUS_EQUAL, PLUS_PLUS,
        SLASH, SLASH_EQUAL,
        STAR, STAR_EQUAL,
        BANG, BANG_EQUAL,
        EQUAL, EQUAL_EQUAL,
        GREATER, GREATER_EQUAL,
        LESS, LESS_EQUAL,
    
        // Literals.
        IDENTIFIER, STRING, NUMBER, ARRAY,
    
        // Keywords.
        AND, NODE, ELSE, FALSE, FUNCTION, FOR, IF, NULL, OR,
        RETURN, PARENT, SELF, TRUE, AUTO, WHILE, BREAK, CONTINUE,
        TRAIT, WITH, USE,
        
        EOF;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(type);
        sb.append("] '");
        sb.append(lexeme);
        sb.append("' ");
        sb.append(literal);
        return sb.toString();
    }
}
