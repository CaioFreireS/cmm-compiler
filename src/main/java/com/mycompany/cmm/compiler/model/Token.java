/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cmm.compiler.model;

import java.util.Objects;

/**
 *
 * @author caiof
 */
public class Token {
    private final TokenType type;
    private final String lexeme;  // O texto exato encontrado no código
    private final Object literal; // O valor processado (ex: Double para float, String para ID)
    private final int line;       // Linha do token (Requisito F)
    private final int column;     // Coluna do token (Requisito F)

    // Construtor principal
    public Token(TokenType type, String lexeme, Object literal, int line, int column) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.column = column;
    }

    // Construtor utilitário para tokens sem valor literal (como palavras-chave)
    public Token(TokenType type, String lexeme, int line, int column) {
        this(type, lexeme, null, line, column);
    }

    // --- Getters ---
    public TokenType getType() { return type; }
    public String getLexeme() { return lexeme; }
    public Object getLiteral() { return literal; }
    public int getLine() { return line; }
    public int getColumn() { return column; }

    // --- Métodos Auxiliares ---

    @Override
    public String toString() {
        // Formato útil para debug e para o relatório final 
        if (literal != null) {
            return String.format("%-15s | %-15s | %-10s | [%d:%d]", 
                type, lexeme, literal, line, column);
        }
        return String.format("%-15s | %-15s |            | [%d:%d]", 
            type, lexeme, line, column);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return line == token.line &&
                column == token.column &&
                type == token.type &&
                Objects.equals(lexeme, token.lexeme) &&
                Objects.equals(literal, token.literal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, lexeme, literal, line, column);
    }
}

