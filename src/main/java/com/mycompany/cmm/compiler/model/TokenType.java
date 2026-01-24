/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cmm.compiler.model;

/**
 *
 * @author caiof
 */
public enum TokenType {
    IF, ELSE, WHILE, FOR, RETURN, BREAK,
    VOID, CHAR, INT, FLOAT, DOUBLE, SHORT, LONG,
    UNSIGNED, SIGNED, CONST, STATIC, AUTO, EXTERN,
    PRINTF, SCANF,
    ID,
    NUMBER_INT,
    NUMBER_FLOAT,
    LITERAL,
    ASSIGN,
    PLUS_ASSIGN,
    MINUS_ASSIGN,
    MULT_ASSIGN,
    DIV_ASSIGN,
    EQ,
    NEQ,
    LT,
    GT,
    LE,
    GE,
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    MOD,
    AND,
    OR,
    NOT,
    LPAREN, RPAREN,
    LBRACE, RBRACE,
    LBRACKET, RBRACKET,
    SEMICOLON,
    COMMA,
    HASHTAG,
    EOF,
    ERROR
}
