/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cmm.compiler.lexer;

/**
 *
 * @author caiof
 */
public class LexicalError extends RuntimeException {
    public LexicalError(String message) {
        super(message);
    }
}
