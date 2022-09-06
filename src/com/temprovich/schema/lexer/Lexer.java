package com.temprovich.schema.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.temprovich.schema.Schema;

public final class Lexer {

    public static final String KW_AND = "and";
    public static final String KW_NODE = "node";
    public static final String KW_ELSE = "else";
    public static final String KW_FALSE = "false";
    public static final String KW_FOR = "for";
    public static final String KW_FUNCTION = "function";
    public static final String KW_IF = "if";
    public static final String KW_NULL = "null";
    public static final String KW_OR = "or";
    public static final String KW_RETURN = "return";
    public static final String KW_PARENT = "parent";
    public static final String KW_SELF = "self";
    public static final String KW_TRUE = "true";
    public static final String KW_AUTO = "auto";
    public static final String KW_WHILE = "while";
    public static final String KW_BREAK = "break";
    public static final String KW_CONTINUE = "continue";
    public static final String KW_TRAIT = "trait";
    public static final String KW_WITH = "with";
    public static final String KW_ARRAY = "array";
    public static final String KW_USE = "use";

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<String, TokenType>();
        keywords.put(KW_AND, TokenType.AND);
        keywords.put(KW_NODE, TokenType.NODE);
        keywords.put(KW_ELSE, TokenType.ELSE);
        keywords.put(KW_FALSE, TokenType.FALSE);
        keywords.put(KW_FOR, TokenType.FOR);
        keywords.put(KW_FUNCTION, TokenType.FUNCTION);
        keywords.put(KW_IF, TokenType.IF);
        keywords.put(KW_NULL, TokenType.NULL);
        keywords.put(KW_OR, TokenType.OR);
        keywords.put(KW_RETURN, TokenType.RETURN);
        keywords.put(KW_PARENT, TokenType.PARENT);
        keywords.put(KW_SELF, TokenType.SELF);
        keywords.put(KW_TRUE, TokenType.TRUE);
        keywords.put(KW_AUTO, TokenType.AUTO);
        keywords.put(KW_WHILE, TokenType.WHILE);
        keywords.put(KW_BREAK, TokenType.BREAK);
        keywords.put(KW_CONTINUE, TokenType.CONTINUE);
        keywords.put(KW_TRAIT, TokenType.TRAIT);
        keywords.put(KW_WITH, TokenType.WITH);
        keywords.put(KW_ARRAY, TokenType.ARRAY);
        keywords.put(KW_USE, TokenType.USE);
    }
    
    private final String source;
    private final List<Token> tokens;
    private int start = 0;
    private int current = 0;
    private int line = 1;
    
    public Lexer(String source) {
        this.source = source;
        this.tokens = new ArrayList<Token>();
    }

    public List<Token> tokenize() {
        while (!atEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();

        switch (c) {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case '[': addToken(TokenType.LEFT_SQUARE_BRACKET); break;
            case ']': addToken(TokenType.RIGHT_SQUARE_BRACKET); break;

            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case ':': addToken(match(':') ? TokenType.COLON_COLON : TokenType.COLON); break;

            case '!': addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG); break;
            case '=': addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
            case '<': addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS); break;
            case '>': addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;

            case '+': addToken(match('=') ? TokenType.PLUS_EQUAL : match('+') ? TokenType.PLUS_PLUS : TokenType.PLUS); break;
            case '-': addToken(match('=') ? TokenType.MINUS_EQUAL : match('-') ? TokenType.MINUS_MINUS : TokenType.MINUS); break;
            case '*': addToken(match('=') ? TokenType.STAR_EQUAL : TokenType.STAR); break;
            case '/':
                if (match('/')) {
                    while (peek() != '\n' && !atEnd()) {
                        advance();
                    }
                } else if (match('*')) {
                    while (peek() != '*' && peekNext() != '/' && !atEnd()) {
                        if (peek() == '\n') {
                            line++;
                        }
                        advance();
                    }
                    if (atEnd()) {
                        throw new RuntimeException("Unterminated comment");
                    }
                    advance();
                    advance();
                } else {
                    addToken(match('=') ? TokenType.SLASH_EQUAL : TokenType.SLASH); break;
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;
            case '"': string(); break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Schema.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void string() {
        while (peek() != '"' && !atEnd()) {
            if (peek() == '\n') {
                line++;
            }

            advance();
        }

        if (atEnd()) {
            Schema.error(line, "Unterminated string.");
            return;
        }
        
        advance();
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private void number() {
        while (isDigit(peek())) {
            advance();
        }
        
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            
            while (isDigit(peek())) {
                advance();
            }
        }
        
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }
        
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);

        if (type == null) {
            type = TokenType.IDENTIFIER;
        }

        addToken(type);
    }

    // Utility methods

    private boolean atEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected) {
        if (current >= source.length()) {
            return false;
        }
        if (source.charAt(current) != expected) {
            return false;
        }
        
        current++;
        return true;
    }

    private char peek() {
        if (current >= source.length()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
