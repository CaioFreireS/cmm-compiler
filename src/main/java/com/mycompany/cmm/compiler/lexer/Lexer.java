/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cmm.compiler.lexer;

import com.mycompany.cmm.compiler.model.Token;
import com.mycompany.cmm.compiler.model.TokenType;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author caiof
 */
public class Lexer {
    private final String input;
    private int pos = 0;
    private int line = 1;
    private int column = 1;
    private int startColumn = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("if", TokenType.IF);
        keywords.put("else", TokenType.ELSE);
        keywords.put("while", TokenType.WHILE);
        keywords.put("for", TokenType.FOR);
        keywords.put("return", TokenType.RETURN);
        keywords.put("break", TokenType.BREAK);
        keywords.put("void", TokenType.VOID);
        keywords.put("char", TokenType.CHAR);
        keywords.put("int", TokenType.INT);
        keywords.put("float", TokenType.FLOAT);
        keywords.put("double", TokenType.DOUBLE);
        keywords.put("short", TokenType.SHORT);
        keywords.put("long", TokenType.LONG);
        keywords.put("unsigned", TokenType.UNSIGNED);
        keywords.put("signed", TokenType.SIGNED);
        keywords.put("auto", TokenType.AUTO);
        keywords.put("static", TokenType.STATIC);
        keywords.put("extern", TokenType.EXTERN);
        keywords.put("const", TokenType.CONST);
        keywords.put("printf", TokenType.PRINTF);
        keywords.put("scanf", TokenType.SCANF);
        
        keywords.put("AND", TokenType.AND);
        keywords.put("OR", TokenType.OR);
    }

    public Lexer(String input) {
        this.input = input;
    }

    public Token scan() {
        skipWhitespace();

        if (pos >= input.length()) {
            return new Token(TokenType.EOF, "", line, column);
        }

        char c = peek();
        startColumn = column;

        if (Character.isLetter(c) || c == '_') {
            return scanIdentifier();
        }

        if (Character.isDigit(c)) {
            return scanNumber();
        }

        if (c == '"') {
            return scanStringLiteral();
        }

        advance();

        switch (c) {
            case '+': return match('=') ? token(TokenType.PLUS_ASSIGN, "+=") : token(TokenType.PLUS, "+");
            case '-': return match('=') ? token(TokenType.MINUS_ASSIGN, "-=") : token(TokenType.MINUS, "-");
            case '*': return match('=') ? token(TokenType.MULT_ASSIGN, "*=") : token(TokenType.MULTIPLY, "*");
            case '%': return match('=') ? token(TokenType.DIV_ASSIGN, "%=") : token(TokenType.MOD, "%");
            
            case '/': 
                if (match('/')) {
                    skipLineComment();
                    return scan();
                } else if (match('*')) {
                    skipBlockComment();
                    return scan();
                } else if (match('=')) {
                    return token(TokenType.DIV_ASSIGN, "/=");
                } else {
                    return token(TokenType.DIVIDE, "/");
                }

            case '=': return match('=') ? token(TokenType.EQ, "==") : token(TokenType.ASSIGN, "=");
            case '!': return match('=') ? token(TokenType.NEQ, "!=") : token(TokenType.NOT, "!");
            case '<': return match('=') ? token(TokenType.LE, "<=") : token(TokenType.LT, "<");
            case '>': return match('=') ? token(TokenType.GE, ">=") : token(TokenType.GT, ">");
            
            case '(': return token(TokenType.LPAREN, "(");
            case ')': return token(TokenType.RPAREN, ")");
            case '{': return token(TokenType.LBRACE, "{");
            case '}': return token(TokenType.RBRACE, "}");
            case '[': return token(TokenType.LBRACKET, "[");
            case ']': return token(TokenType.RBRACKET, "]");
            case ';': return token(TokenType.SEMICOLON, ";");
            case ',': return token(TokenType.COMMA, ",");
            case '#': return token(TokenType.HASHTAG, "#");

            default:
                return new Token(TokenType.ERROR, String.valueOf(c), "Caractere inválido", line, startColumn);
        }
    }

    private Token scanIdentifier() {
        StringBuilder sb = new StringBuilder();
        while (pos < input.length() && (Character.isLetterOrDigit(peek()) || peek() == '_')) {
            sb.append(advance());
        }

        String text = sb.toString();

        if (text.length() > 31) {
            return new Token(TokenType.ERROR, text, "Identificador excede 31 caracteres", line, startColumn);
        }

        TokenType type = keywords.getOrDefault(text, TokenType.ID);
        
        return new Token(type, text, null, line, startColumn);
    }

    private Token scanNumber() {
        StringBuilder sb = new StringBuilder();
        boolean isFloat = false;

        while (pos < input.length() && Character.isDigit(peek())) {
            sb.append(advance());
        }

        if (pos < input.length() && peek() == '.' && Character.isDigit(peekNext())) {
            isFloat = true;
            sb.append(advance());
            while (pos < input.length() && Character.isDigit(peek())) {
                sb.append(advance());
            }
        }

        String text = sb.toString();
        if (isFloat) {
            return new Token(TokenType.NUMBER_FLOAT, text, Double.parseDouble(text), line, startColumn);
        } else {
            return new Token(TokenType.NUMBER_INT, text, Integer.parseInt(text), line, startColumn);
        }
    }

    private Token scanStringLiteral() {
        advance();
        StringBuilder sb = new StringBuilder();
        
        while (pos < input.length() && peek() != '"') {
            if (peek() == '\n') {
                line++; column = 1;
            }
            sb.append(advance());
        }

        if (pos >= input.length()) {
            return new Token(TokenType.ERROR, sb.toString(), "String não fechada", line, startColumn);
        }

        advance();
        return new Token(TokenType.LITERAL, sb.toString(), sb.toString(), line, startColumn);
    }

    private void skipLineComment() {
        while (pos < input.length() && peek() != '\n') {
            advance();
        }
    }

    private void skipBlockComment() {
        while (pos < input.length()) {
            if (peek() == '*' && peekNext() == '/') {
                advance();
                advance();
                return;
            }
            if (peek() == '\n') {
                line++;
                column = 1;
                pos++;
            } else {
                advance();
            }
        }
    }

    private void skipWhitespace() {
        while (pos < input.length()) {
            char c = peek();
            if (c == ' ' || c == '\r' || c == '\t') {
                advance();
            } else if (c == '\n') {
                line++;
                column = 1;
                pos++;
            } else {
                break;
            }
        }
    }

    private char advance() {
        char c = input.charAt(pos++);
        column++;
        return c;
    }

    private char peek() {
        if (pos >= input.length()) return '\0';
        return input.charAt(pos);
    }

    private char peekNext() {
        if (pos + 1 >= input.length()) return '\0';
        return input.charAt(pos + 1);
    }

    private boolean match(char expected) {
        if (pos >= input.length()) return false;
        if (input.charAt(pos) != expected) return false;
        advance();
        return true;
    }

    private Token token(TokenType type, String lexeme) {
        return new Token(type, lexeme, line, startColumn);
    }
}
