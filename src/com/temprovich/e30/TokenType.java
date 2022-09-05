package com.temprovich.e30;

public enum TokenType {
    
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,
    COLON,

    // One or two character tokens.
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    // Literals.
    IDENTIFIER, STRING, NUMBER,

    // Keywords.
    AND, NODE, ELSE, FALSE, FUNCTION, FOR, IF, NULL, OR,
    RETURN, PARENT, SELF, TRUE, AUTO, WHILE, BREAK, CONTINUE,
    TRAIT, WITH,

    EOF
}
