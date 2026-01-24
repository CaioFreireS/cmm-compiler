package com.mycompany.cmm.compiler.model;

import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer {
    private SymbolTable symbolTable;
    private TokenType currentFunctionReturnType = null;
    private boolean hasReturn = false;
    private Token functionNameToken = null;
    private List<String[]> semanticErrors = new ArrayList<>();

    public SymbolTable getSymbolTable() { return this.symbolTable; }
    public List<String[]> getSemanticErrors() { return this.semanticErrors; }
    
    public void reset() {
        this.symbolTable = new SymbolTable(null);
        this.semanticErrors.clear();
        this.currentFunctionReturnType = null;
        this.hasReturn = false;
    }

    public void analyze(List<Token> tokens) {
        reset(); 

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (isType(token.getType()) && i + 2 < tokens.size() && 
                tokens.get(i + 1).getType() == TokenType.ID && 
                tokens.get(i + 2).getType() == TokenType.LPAREN) {
                
                currentFunctionReturnType = token.getType(); 
                functionNameToken = tokens.get(i + 1);
                hasReturn = false;
            }

            if (isType(token.getType()) && i + 1 < tokens.size() && 
                tokens.get(i + 1).getType() == TokenType.ID &&
                (i + 2 >= tokens.size() || tokens.get(i + 2).getType() != TokenType.LPAREN)) {
                symbolTable.add(tokens.get(i + 1).getLexeme(), token.getType(), token.getLine());
            }
            
        }
    }

    public void validateFunctionExit() {
        if (currentFunctionReturnType != null && currentFunctionReturnType != TokenType.VOID && !hasReturn) {
            reportError("A função '" + functionNameToken.getLexeme() + "' deve retornar um valor.", functionNameToken);
        }
    }

    public String checkTypeCompatibility(Token returnToken) {
        this.hasReturn = true; 
        if (currentFunctionReturnType == null) return null;

        TokenType foundType = returnToken.getType();
        
        if (foundType == TokenType.ID) {
            SymbolInfo info = symbolTable.get(returnToken.getLexeme());
            if (info == null) return "Erro: Variável '" + returnToken.getLexeme() + "' não declarada.";
            foundType = info.type;
        }

        boolean isCompatible = switch (currentFunctionReturnType) {
            case INT -> (foundType == TokenType.INT || foundType == TokenType.NUMBER_INT);
            case FLOAT -> (foundType == TokenType.FLOAT || foundType == TokenType.NUMBER_FLOAT);
            case CHAR -> (foundType == TokenType.CHAR || foundType == TokenType.LITERAL);
            case VOID -> (foundType == TokenType.VOID || foundType == TokenType.SEMICOLON);
            default -> false;
        };

        if (!isCompatible) {
            return "Erro de Tipo: " + currentFunctionReturnType + " não pode retornar " + foundType;
        }
        return null; 
    }

    private void reportError(String message, Token t) {
        semanticErrors.add(new String[]{message, String.valueOf(t.getLine()), t.getLexeme()});
    }

    private boolean isType(TokenType type) {
        return type == TokenType.INT || type == TokenType.FLOAT || type == TokenType.CHAR || type == TokenType.VOID;
    }
}