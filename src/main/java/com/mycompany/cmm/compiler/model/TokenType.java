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
    // --- Palavras Reservadas (Keywords) ---
    // Estruturas de controle
    IF, ELSE, WHILE, FOR, RETURN, BREAK,
    
    // Tipos de dados e modificadores
    VOID, CHAR, INT, FLOAT, DOUBLE, SHORT, LONG,
    UNSIGNED, SIGNED, CONST, STATIC, AUTO, EXTERN,
    
    // Funções nativas (mencionadas na gramática)
    PRINTF, SCANF,

    // --- Identificadores e Literais ---
    ID,             // Ex: variavel, conta_bancaria
    NUMBER_INT,     // Ex: 123
    NUMBER_FLOAT,   // Ex: 12.34
    LITERAL,        // Strings constantes (ex: "ola mundo")

    // --- Operadores de Atribuição ---
    ASSIGN,         // =
    PLUS_ASSIGN,    // +=
    MINUS_ASSIGN,   // -=
    MULT_ASSIGN,    // *=
    DIV_ASSIGN,     // /=
    
    // --- Operadores Relacionais ---
    EQ,             // ==
    NEQ,            // !=
    LT,             // <
    GT,             // >
    LE,             // <=
    GE,             // >=

    // --- Operadores Aritméticos ---
    PLUS,           // +
    MINUS,          // -
    MULTIPLY,       // *
    DIVIDE,         // /
    MOD,            // % (Implícito em linguagens C, embora não explícito na imagem 54, é boa prática ter)

    // --- Operadores Lógicos ---
    // A gramática usa explicitamente AND e OR como terminais
    AND,            // && ou a palavra AND (dependerá da sua implementação do lexer)
    OR,             // || ou a palavra OR
    NOT,            // !

    // --- Delimitadores e Pontuação ---
    LPAREN, RPAREN,     // ( )
    LBRACE, RBRACE,     // { }
    LBRACKET, RBRACKET, // [ ]
    SEMICOLON,          // ;
    COMMA,              // ,
    
    // --- Pré-processador (Opcional, mas citado) ---
    HASHTAG,            // # (para #define ou #include)

    // --- Controle ---
    EOF,      // Fim de arquivo
    ERROR     // Token inválido (para recuperação de erros)
}
