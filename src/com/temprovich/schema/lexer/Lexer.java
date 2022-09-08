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

    private static final Map<String, Token.Type> keywords;

    static {
        keywords = new HashMap<String, Token.Type>();
        keywords.put(KW_AND, Token.Type.AND);
        keywords.put(KW_NODE, Token.Type.NODE);
        keywords.put(KW_ELSE, Token.Type.ELSE);
        keywords.put(KW_FALSE, Token.Type.FALSE);
        keywords.put(KW_FOR, Token.Type.FOR);
        keywords.put(KW_FUNCTION, Token.Type.FUNCTION);
        keywords.put(KW_IF, Token.Type.IF);
        keywords.put(KW_NULL, Token.Type.NULL);
        keywords.put(KW_OR, Token.Type.OR);
        keywords.put(KW_RETURN, Token.Type.RETURN);
        keywords.put(KW_PARENT, Token.Type.PARENT);
        keywords.put(KW_SELF, Token.Type.SELF);
        keywords.put(KW_TRUE, Token.Type.TRUE);
        keywords.put(KW_AUTO, Token.Type.AUTO);
        keywords.put(KW_WHILE, Token.Type.WHILE);
        keywords.put(KW_BREAK, Token.Type.BREAK);
        keywords.put(KW_CONTINUE, Token.Type.CONTINUE);
        keywords.put(KW_TRAIT, Token.Type.TRAIT);
        keywords.put(KW_WITH, Token.Type.WITH);
        keywords.put(KW_ARRAY, Token.Type.ARRAY);
        keywords.put(KW_USE, Token.Type.USE);
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

        tokens.add(new Token(Token.Type.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();

        switch (c) {
            case '(': addToken(Token.Type.LEFT_PAREN); break;
            case ')': addToken(Token.Type.RIGHT_PAREN); break;
            case '{': addToken(Token.Type.LEFT_BRACE); break;
            case '}': addToken(Token.Type.RIGHT_BRACE); break;
            case '[': addToken(Token.Type.LEFT_SQUARE_BRACKET); break;
            case ']': addToken(Token.Type.RIGHT_SQUARE_BRACKET); break;

            case ',': addToken(Token.Type.COMMA); break;
            case '.': addToken(Token.Type.DOT); break;
            case ';': addToken(Token.Type.SEMICOLON); break;
            case ':': addToken(match(':') ? Token.Type.COLON_COLON : Token.Type.COLON); break;

            case '!': addToken(match('=') ? Token.Type.BANG_EQUAL : Token.Type.BANG); break;
            case '=': addToken(match('=') ? Token.Type.EQUAL_EQUAL : Token.Type.EQUAL); break;
            case '<': addToken(match('=') ? Token.Type.LESS_EQUAL : Token.Type.LESS); break;
            case '>': addToken(match('=') ? Token.Type.GREATER_EQUAL : Token.Type.GREATER); break;

            case '+': addToken(match('=') ? Token.Type.PLUS_EQUAL : match('+') ? Token.Type.PLUS_PLUS : Token.Type.PLUS); break;
            case '-': addToken(match('=') ? Token.Type.MINUS_EQUAL : match('-') ? Token.Type.MINUS_MINUS : Token.Type.MINUS); break;
            case '*': addToken(match('=') ? Token.Type.STAR_EQUAL : Token.Type.STAR); break;
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
                    addToken(match('=') ? Token.Type.SLASH_EQUAL : Token.Type.SLASH); break;
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
                    Schema.reporter.error(line, "Unexpected character.");
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
            Schema.reporter.error(line, "Unterminated string.");
            return;
        }
        
        advance();
        String value = source.substring(start + 1, current - 1);
        addToken(Token.Type.STRING, value);
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
        
        addToken(Token.Type.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }
        
        String text = source.substring(start, current);
        Token.Type type = keywords.get(text);

        if (type == null) {
            type = Token.Type.IDENTIFIER;
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

    private void addToken(Token.Type type) {
        addToken(type, null);
    }

    private void addToken(Token.Type type, Object literal) {
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
