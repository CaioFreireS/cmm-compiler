/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cmm.compiler.model;

/**
 *
 * @author berna
 */
public class SymbolInfo {
    public String name;
    public TokenType type;
    public int line;
    public Object value;

    public SymbolInfo(String name, TokenType type, int line) {
        this(name, type, line, null);
    }

    public SymbolInfo(String name, TokenType type, int line, Object value) {
        this.name = name;
        this.type = type;
        this.line = line;
        this.value = value;
    }
}
