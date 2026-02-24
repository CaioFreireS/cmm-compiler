package com.mycompany.cmm.compiler.model;

import java.util.ArrayList;
import java.util.List;

public class SemanticAnalyzer {
    private SymbolTable symbolTable;
    private TokenType currentFunctionReturnType = null;
    private boolean hasReturn = false;
    private Token functionNameToken = null;
    private boolean pendingFunction = false;
    private boolean inFunction = false;
    private int braceDepth = 0;
    private List<String[]> semanticErrors = new ArrayList<>();

    public SymbolTable getSymbolTable() { return this.symbolTable; }
    public List<String[]> getSemanticErrors() { return this.semanticErrors; }
    
    public void reset() {
        this.symbolTable = new SymbolTable(null);
        this.semanticErrors.clear();
        this.currentFunctionReturnType = null;
        this.functionNameToken = null;
        this.hasReturn = false;
        this.pendingFunction = false;
        this.inFunction = false;
        this.braceDepth = 0;
    }

    public void analyze(List<Token> tokens) {
        this.symbolTable = new SymbolTable(null); 
        this.currentFunctionReturnType = null;
        this.functionNameToken = null;
        this.hasReturn = false;
        this.pendingFunction = false;
        this.inFunction = false;
        this.braceDepth = 0;
        this.semanticErrors.clear();

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (isType(token.getType()) && i + 2 < tokens.size() && 
                tokens.get(i + 1).getType() == TokenType.ID && 
                tokens.get(i + 2).getType() == TokenType.LPAREN) {
                this.currentFunctionReturnType = token.getType();
                this.functionNameToken = tokens.get(i + 1);
                this.hasReturn = false; 
                this.pendingFunction = true;
            }

            if (isType(token.getType()) && i + 1 < tokens.size() && 
                tokens.get(i + 1).getType() == TokenType.ID &&
                (i + 2 >= tokens.size() || tokens.get(i + 2).getType() != TokenType.LPAREN)) {

                Token varToken = tokens.get(i + 1);
                
                if (symbolTable.exists(varToken.getLexeme())) {
                    reportError("Variável redeclarada", varToken);
                } else {
                    symbolTable.add(varToken.getLexeme(), token.getType(), token.getLine());
                }

                if (i + 3 < tokens.size() && tokens.get(i + 2).getType() == TokenType.ASSIGN) {
                    Token valueToken = tokens.get(i + 3);
                    String error;
                    error = checkAssignmentCompatibility(token.getType(), valueToken);
                    if (error != null) {
                        reportError(error, valueToken);
                    }
                }
            }

            if (token.getType() == TokenType.ID && i + 3 < tokens.size() &&
                tokens.get(i + 1).getType() == TokenType.LBRACKET &&
                tokens.get(i + 3).getType() == TokenType.RBRACKET) {

                Token indexToken = tokens.get(i + 2);
                TokenType idxType = indexToken.getType();
                if (idxType == TokenType.NUMBER_INT || idxType == TokenType.NUMBER_FLOAT || idxType == TokenType.LITERAL) {
                    reportError("Erro de Contexto: índice de vetor não pode ser literal.", indexToken);
                }
            }

            if ((token.getType() == TokenType.SIGNED || token.getType() == TokenType.UNSIGNED) &&
                i + 2 < tokens.size() &&
                (tokens.get(i + 1).getType() == TokenType.SHORT || tokens.get(i + 1).getType() == TokenType.INT || tokens.get(i + 1).getType() == TokenType.LONG) &&
                tokens.get(i + 2).getType() == TokenType.ID &&
                (i + 3 >= tokens.size() || tokens.get(i + 3).getType() != TokenType.LPAREN)) {

                TokenType baseType = tokens.get(i + 1).getType();
                Token varToken = tokens.get(i + 2);

                if (symbolTable.exists(varToken.getLexeme())) {
                    reportError("Variável redeclarada", varToken);
                } else {
                    symbolTable.add(varToken.getLexeme(), baseType, varToken.getLine());
                }

                if (i + 4 < tokens.size() && tokens.get(i + 3).getType() == TokenType.ASSIGN) {
                    Token valueToken = tokens.get(i + 4);
                    String error = checkAssignmentCompatibility(baseType, valueToken);
                    if (error != null) {
                        reportError(error, valueToken);
                    }
                }
            }

            if ((token.getType() == TokenType.SIGNED || token.getType() == TokenType.UNSIGNED) &&
                i + 3 < tokens.size() &&
                (tokens.get(i + 1).getType() == TokenType.SHORT || tokens.get(i + 1).getType() == TokenType.INT || tokens.get(i + 1).getType() == TokenType.LONG) &&
                tokens.get(i + 2).getType() == TokenType.ID &&
                tokens.get(i + 3).getType() == TokenType.LPAREN) {

                this.currentFunctionReturnType = tokens.get(i + 1).getType();
                this.functionNameToken = tokens.get(i + 2);
                this.hasReturn = false;
                this.pendingFunction = true;
            }
        }

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            if (token.getType() == TokenType.LBRACE) {
                if (pendingFunction) {
                    inFunction = true;
                    braceDepth = 1;
                    pendingFunction = false;
                } else if (inFunction) {
                    braceDepth++;
                }
            } else if (token.getType() == TokenType.RBRACE) {
                if (inFunction) {
                    braceDepth--;
                    if (braceDepth <= 0) {
                        validateFunctionExit();
                        currentFunctionReturnType = null;
                        functionNameToken = null;
                        hasReturn = false;
                        inFunction = false;
                        braceDepth = 0;
                    }
                }
            }
        }
    }

    public void validateFunctionExit() {
        if (functionNameToken == null) {
            return; 
        }

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
        for(String[] err : semanticErrors) {
            if(err[0].equals(message) && err[1].equals(String.valueOf(t.getLine()))) return;
        }
        semanticErrors.add(new String[]{message, String.valueOf(t.getLine()), t.getLexeme()});
    }
    
    private String checkAssignmentCompatibility(TokenType varType, Token valueToken) {
        TokenType valType = valueToken.getType();

        if (valType == TokenType.ID) {
            SymbolInfo info = symbolTable.get(valueToken.getLexeme());
            if (info == null) return "Variável '" + valueToken.getLexeme() + "' não declarada.";
            valType = info.type;
        }

        boolean isCompatible = switch (varType) {
            case INT, SHORT, LONG -> (valType == TokenType.INT || valType == TokenType.NUMBER_INT);
            case FLOAT, DOUBLE -> (valType == TokenType.FLOAT || valType == TokenType.NUMBER_FLOAT || 
                                   valType == TokenType.INT || valType == TokenType.NUMBER_INT);
            case CHAR -> (valType == TokenType.CHAR || valType == TokenType.LITERAL);
            default -> false;
        };

        if (!isCompatible) {
            return "Erro de Atribuição: Não é possível atribuir " + valType + " a uma variável " + varType;
        }
        return null;
    }

    public boolean isType(TokenType type) {
        return type == TokenType.INT || type == TokenType.FLOAT || type == TokenType.CHAR || 
               type == TokenType.VOID || type == TokenType.DOUBLE || type == TokenType.LONG || 
               type == TokenType.SHORT;
    }
}