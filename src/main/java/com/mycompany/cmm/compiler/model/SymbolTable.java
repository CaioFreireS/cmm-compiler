package com.mycompany.cmm.compiler.model;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author berna
 */
public class SymbolTable {
    private final Map<String, SymbolInfo> symbols = new HashMap<>();
    private final SymbolTable parent; 

    public SymbolTable(SymbolTable parent) {
        this.parent = parent;
    }

    public SymbolTable getParent() {
        return this.parent;
    }

    public boolean containsLocal(String name) {
        return symbols.containsKey(name);
    }

    public void add(String name, TokenType type, int line) {
        if (symbols.containsKey(name)) {
            return;
        }
        symbols.put(name, new SymbolInfo(name, type, line));
    }

    public void add(String name, TokenType type, int line, Object value) {
        if (symbols.containsKey(name)) {
            return;
        }
        symbols.put(name, new SymbolInfo(name, type, line, value));
    }

    public SymbolInfo get(String name) {
        SymbolInfo info = symbols.get(name);
        if (info == null && parent != null) return parent.get(name);
        return info;
    }
}