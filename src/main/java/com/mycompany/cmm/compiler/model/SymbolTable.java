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

    public void add(String name, TokenType type, int line) {
        if (symbols.containsKey(name)) {
            System.err.println("Erro Semântico: Variável '" + name + "' já declarada na linha " + line);
            return;
        }
        symbols.put(name, new SymbolInfo(name, type, line));
    }

    public SymbolInfo get(String name) {
        SymbolInfo info = symbols.get(name);
        if (info == null && parent != null) return parent.get(name);
        return info;
    }

    public boolean exists(String name) {
        return get(name) != null;
    }
}