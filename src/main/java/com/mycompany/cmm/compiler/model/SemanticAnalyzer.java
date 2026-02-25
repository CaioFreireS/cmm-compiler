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
    private java.util.List<SymbolInfo> pendingParameters = new ArrayList<>();
    private java.util.List<String> pendingFunctionNames = new ArrayList<>();
    private java.util.Map<String, java.util.List<SymbolInfo>> functionParamsMap = new java.util.HashMap<>();
    private java.util.Map<String, SymbolInfo> allSymbols = new java.util.HashMap<>();
    private java.util.Map<String, TokenType> functionReturnTypeMap = new java.util.HashMap<>();
    private java.util.Map<String, Token> functionNameTokenMap = new java.util.HashMap<>();

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
        this.pendingParameters.clear();
        this.pendingFunctionNames.clear();
        this.functionParamsMap.clear();
        this.allSymbols.clear();
        this.functionReturnTypeMap.clear();
        this.functionNameTokenMap.clear();
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
        this.pendingFunctionNames.clear();
        this.functionParamsMap.clear();
        this.allSymbols.clear();
        this.functionReturnTypeMap.clear();
        this.functionNameTokenMap.clear();
        this.pendingParameters.clear();

        SymbolTable currentScope = this.symbolTable;

        // Passo 1: Pré-processamento para funções e defines
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            // #define
            if (token.getType() == TokenType.HASHTAG && i + 3 < tokens.size()
                && tokens.get(i + 1).getType() == TokenType.ID
                && tokens.get(i + 1).getLexeme().equals("define")
                && tokens.get(i + 2).getType() == TokenType.ID) {

                Token nameTok = tokens.get(i + 2);
                Token valueTok = tokens.get(i + 3);
                TokenType defType = null;
                Object val = null;
                if (valueTok.getType() == TokenType.NUMBER_INT) {
                    defType = TokenType.INT;
                    val = valueTok.getLiteral();
                } else if (valueTok.getType() == TokenType.NUMBER_FLOAT) {
                    defType = TokenType.FLOAT;
                    val = valueTok.getLiteral();
                }
                if (symbolTable.exists(nameTok.getLexeme())) {
                    reportError("Variável redeclarada", nameTok);
                } else if (defType != null) {
                    symbolTable.add(nameTok.getLexeme(), defType, nameTok.getLine(), val);
                    allSymbols.putIfAbsent(nameTok.getLexeme(), new SymbolInfo(nameTok.getLexeme(), defType, nameTok.getLine(), val));
                }
                continue;
            }

            // Função (suporta opcionalmente um especificador antes do tipo)
            if ((isType(token.getType()) && i + 2 < tokens.size() &&
                tokens.get(i + 1).getType() == TokenType.ID &&
                tokens.get(i + 2).getType() == TokenType.LPAREN)
                || (isSpecifier(token.getType()) && i + 3 < tokens.size() && isType(tokens.get(i + 1).getType()) &&
                    tokens.get(i + 2).getType() == TokenType.ID && tokens.get(i + 3).getType() == TokenType.LPAREN)) {

                TokenType retType;
                Token nameTok;
                int j;
                if (isSpecifier(token.getType())) {
                    retType = tokens.get(i + 1).getType();
                    nameTok = tokens.get(i + 2);
                    j = i + 4; // after LPAREN when specifier is present
                } else {
                    retType = token.getType();
                    nameTok = tokens.get(i + 1);
                    j = i + 3; // after LPAREN when no specifier
                }

                this.currentFunctionReturnType = retType;
                this.functionNameToken = nameTok;
                this.hasReturn = false;
                this.pendingFunction = true;

                // Parse parameters and store them for later when entering function scope
                java.util.List<SymbolInfo> params = new java.util.ArrayList<>();
                while (j < tokens.size() && tokens.get(j).getType() != TokenType.RPAREN) {
                    if (isType(tokens.get(j).getType())) {
                        TokenType pType = tokens.get(j).getType();
                        if (j + 1 < tokens.size() && tokens.get(j + 1).getType() == TokenType.ID) {
                            Token pName = tokens.get(j + 1);
                            params.add(new SymbolInfo(pName.getLexeme(), pType, pName.getLine()));
                            j += 2;
                            if (j < tokens.size() && tokens.get(j).getType() == TokenType.LBRACKET) {
                                // skip size
                                j++; if (j < tokens.size()) j++; if (j < tokens.size() && tokens.get(j).getType() == TokenType.RBRACKET) j++;
                            }
                            if (j < tokens.size() && tokens.get(j).getType() == TokenType.COMMA) j++;
                            continue;
                        }
                    } else if (tokens.get(j).getType() == TokenType.SIGNED || tokens.get(j).getType() == TokenType.UNSIGNED) {
                        if (j + 1 < tokens.size() && (tokens.get(j + 1).getType() == TokenType.SHORT || tokens.get(j + 1).getType() == TokenType.INT || tokens.get(j + 1).getType() == TokenType.LONG)) {
                            TokenType pType = tokens.get(j + 1).getType();
                            if (j + 2 < tokens.size() && tokens.get(j + 2).getType() == TokenType.ID) {
                                Token pName = tokens.get(j + 2);
                                params.add(new SymbolInfo(pName.getLexeme(), pType, pName.getLine()));
                                j += 3;
                                if (j < tokens.size() && tokens.get(j).getType() == TokenType.LBRACKET) {
                                    j++; if (j < tokens.size()) j++; if (j < tokens.size() && tokens.get(j).getType() == TokenType.RBRACKET) j++;
                                }
                                if (j < tokens.size() && tokens.get(j).getType() == TokenType.COMMA) j++;
                                continue;
                            }
                        }
                    }
                    j++;
                }

                String fname = functionNameToken.getLexeme();
                pendingFunctionNames.add(fname);
                functionParamsMap.put(fname, params);

                // Adiciona nome da função ao escopo global (e ao mapa global)
                if (!symbolTable.exists(fname)) {
                    symbolTable.add(fname, this.currentFunctionReturnType, functionNameToken.getLine());
                    allSymbols.put(fname, new SymbolInfo(fname, this.currentFunctionReturnType, functionNameToken.getLine()));
                    functionReturnTypeMap.put(fname, this.currentFunctionReturnType);
                    functionNameTokenMap.put(fname, functionNameToken);
                }
            }
            // Função com signed/unsigned possivelmente precedido por especificador
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
            if (isSpecifier(token.getType()) && i + 4 < tokens.size() &&
                (tokens.get(i + 1).getType() == TokenType.SIGNED || tokens.get(i + 1).getType() == TokenType.UNSIGNED) &&
                (tokens.get(i + 2).getType() == TokenType.SHORT || tokens.get(i + 2).getType() == TokenType.INT || tokens.get(i + 2).getType() == TokenType.LONG) &&
                tokens.get(i + 3).getType() == TokenType.ID &&
                tokens.get(i + 4).getType() == TokenType.LPAREN) {
                this.currentFunctionReturnType = tokens.get(i + 2).getType();
                this.functionNameToken = tokens.get(i + 3);
                this.hasReturn = false;
                this.pendingFunction = true;
            }
        }

        // Passo 2: Análise com escopo
        currentScope = this.symbolTable;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            // Escopo: abre bloco
            if (token.getType() == TokenType.LBRACE) {
                currentScope = new SymbolTable(currentScope);
                this.symbolTable = currentScope;

                if (!pendingFunctionNames.isEmpty()) {
                    // This block corresponds to the next pending function in queue
                    String fname = pendingFunctionNames.remove(0);
                    java.util.List<SymbolInfo> params = functionParamsMap.getOrDefault(fname, java.util.List.of());
                    inFunction = true;
                    braceDepth = 1;
                    // set current function context
                    this.currentFunctionReturnType = functionReturnTypeMap.get(fname);
                    this.functionNameToken = functionNameTokenMap.get(fname);
                    this.hasReturn = false;
                    // add parameters to current scope
                    for (SymbolInfo p : params) {
                        if (currentScope.containsLocal(p.name)) {
                            reportError("Variável redeclarada", new Token(TokenType.ID, p.name, p.line, 0));
                        } else {
                            currentScope.add(p.name, p.type, p.line);
                            allSymbols.putIfAbsent(p.name, new SymbolInfo(p.name, p.type, p.line));
                        }
                    }
                } else if (inFunction) {
                    braceDepth++;
                }
                continue;
            }
            // Escopo: fecha bloco
            if (token.getType() == TokenType.RBRACE) {
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
                if (currentScope.getParent() != null) {
                    currentScope = currentScope.getParent();
                    this.symbolTable = currentScope;
                }
                continue;
            }

            // Declaração de variável (tipo simples) - suporta especificador antes do tipo
            if ((isType(token.getType()) && i + 1 < tokens.size() &&
                tokens.get(i + 1).getType() == TokenType.ID &&
                (i + 2 >= tokens.size() || tokens.get(i + 2).getType() != TokenType.LPAREN))
                || (isSpecifier(token.getType()) && i + 2 < tokens.size() && isType(tokens.get(i + 1).getType()) &&
                    tokens.get(i + 2).getType() == TokenType.ID && (i + 3 >= tokens.size() || tokens.get(i + 3).getType() != TokenType.LPAREN))) {

                // Skip declarations that are actually function parameter declarations
                boolean inFunctionHeader = false;
                if (i - 1 >= 0 && tokens.get(i - 1).getType() == TokenType.LPAREN && i - 2 >= 0 && tokens.get(i - 2).getType() == TokenType.ID) {
                    int k = i - 3;
                    if (k >= 0) {
                        TokenType before = tokens.get(k).getType();
                        if (isType(before) || isSpecifier(before) || before == TokenType.SIGNED || before == TokenType.UNSIGNED) {
                            inFunctionHeader = true;
                        }
                    }
                }
                if (inFunctionHeader) continue;

                Token varToken;
                TokenType declType;
                int declLine;
                int assignIdx = -1;
                if (isSpecifier(token.getType())) {
                    declType = tokens.get(i + 1).getType();
                    varToken = tokens.get(i + 2);
                    declLine = tokens.get(i + 1).getLine();
                    if (i + 3 < tokens.size() && tokens.get(i + 3).getType() == TokenType.ASSIGN) assignIdx = i + 3;
                } else {
                    declType = token.getType();
                    varToken = tokens.get(i + 1);
                    declLine = token.getLine();
                    if (i + 2 < tokens.size() && tokens.get(i + 2).getType() == TokenType.ASSIGN) assignIdx = i + 2;
                }

                if (currentScope.containsLocal(varToken.getLexeme())) {
                    reportError("Variável redeclarada", varToken);
                } else {
                    Object initVal = null;
                    if (assignIdx != -1 && assignIdx + 1 < tokens.size()) {
                        Token valueToken = tokens.get(assignIdx + 1);
                        if (valueToken.getType() == TokenType.NUMBER_INT || valueToken.getType() == TokenType.NUMBER_FLOAT || valueToken.getType() == TokenType.LITERAL || valueToken.getType() == TokenType.ID) {
                            String error = checkAssignmentCompatibility(declType, valueToken);
                            if (error != null) {
                                reportError(error, valueToken);
                            } else {
                                if (valueToken.getLiteral() != null) initVal = valueToken.getLiteral();
                            }
                        }
                    }
                    currentScope.add(varToken.getLexeme(), declType, declLine, initVal);
                    allSymbols.putIfAbsent(varToken.getLexeme(), new SymbolInfo(varToken.getLexeme(), declType, declLine, initVal));
                }
                // skip to end of declaration (semicolon) to avoid double-processing tokens
                int nxt = i;
                while (nxt < tokens.size() && tokens.get(nxt).getType() != TokenType.SEMICOLON) nxt++;
                i = nxt;
                continue;
                
            }

            // Declaração de variável (signed/unsigned), suporta especificador antes
            if (((token.getType() == TokenType.SIGNED || token.getType() == TokenType.UNSIGNED) &&
                i + 2 < tokens.size() &&
                (tokens.get(i + 1).getType() == TokenType.SHORT || tokens.get(i + 1).getType() == TokenType.INT || tokens.get(i + 1).getType() == TokenType.LONG) &&
                tokens.get(i + 2).getType() == TokenType.ID &&
                (i + 3 >= tokens.size() || tokens.get(i + 3).getType() != TokenType.LPAREN))
                || (isSpecifier(token.getType()) && i + 3 < tokens.size() &&
                    (tokens.get(i + 1).getType() == TokenType.SIGNED || tokens.get(i + 1).getType() == TokenType.UNSIGNED) &&
                    (tokens.get(i + 2).getType() == TokenType.SHORT || tokens.get(i + 2).getType() == TokenType.INT || tokens.get(i + 2).getType() == TokenType.LONG) &&
                    tokens.get(i + 3).getType() == TokenType.ID &&
                    (i + 4 >= tokens.size() || tokens.get(i + 4).getType() != TokenType.LPAREN))) {

                // Skip signed/unsigned declarations that are actually function parameters
                boolean inFunctionHeader2 = false;
                if (i - 1 >= 0 && tokens.get(i - 1).getType() == TokenType.LPAREN && i - 2 >= 0 && tokens.get(i - 2).getType() == TokenType.ID) {
                    int k = i - 3;
                    if (k >= 0) {
                        TokenType before = tokens.get(k).getType();
                        if (isType(before) || isSpecifier(before) || before == TokenType.SIGNED || before == TokenType.UNSIGNED) {
                            inFunctionHeader2 = true;
                        }
                    }
                }
                if (inFunctionHeader2) continue;

                TokenType baseType;
                Token varToken;
                int declLine;
                int assignIdx = -1;
                if (isSpecifier(token.getType())) {
                    baseType = tokens.get(i + 2).getType();
                    varToken = tokens.get(i + 3);
                    declLine = tokens.get(i + 2).getLine();
                    if (i + 4 < tokens.size() && tokens.get(i + 4).getType() == TokenType.ASSIGN) assignIdx = i + 4;
                } else {
                    baseType = tokens.get(i + 1).getType();
                    varToken = tokens.get(i + 2);
                    declLine = tokens.get(i + 1).getLine();
                    if (i + 3 < tokens.size() && tokens.get(i + 3).getType() == TokenType.ASSIGN) assignIdx = i + 3;
                }

                if (currentScope.containsLocal(varToken.getLexeme())) {
                    reportError("Variável redeclarada", varToken);
                } else {
                    Object initVal = null;
                    if (assignIdx != -1 && assignIdx + 1 < tokens.size()) {
                        Token valueToken = tokens.get(assignIdx + 1);
                        if (valueToken.getType() == TokenType.NUMBER_INT || valueToken.getType() == TokenType.NUMBER_FLOAT || valueToken.getType() == TokenType.LITERAL || valueToken.getType() == TokenType.ID) {
                            String error = checkAssignmentCompatibility(baseType, valueToken);
                            if (error != null) {
                                reportError(error, valueToken);
                            } else {
                                if (valueToken.getLiteral() != null) initVal = valueToken.getLiteral();
                            }
                        }
                    }
                    currentScope.add(varToken.getLexeme(), baseType, varToken.getLine(), initVal);
                    allSymbols.putIfAbsent(varToken.getLexeme(), new SymbolInfo(varToken.getLexeme(), baseType, varToken.getLine(), initVal));
                }
                int nxt2 = i;
                while (nxt2 < tokens.size() && tokens.get(nxt2).getType() != TokenType.SEMICOLON) nxt2++;
                i = nxt2;
                continue;
            }

            // return
            if (token.getType() == TokenType.RETURN && i + 1 < tokens.size()) {
                Token retTok = tokens.get(i + 1);
                String typeErr = checkTypeCompatibility(retTok);
                if (typeErr != null) {
                    reportError(typeErr, retTok);
                }
                continue;
            }

            // Índice de vetor literal (apenas para uso, não declaração)
            if (token.getType() == TokenType.ID && i + 3 < tokens.size() &&
                tokens.get(i + 1).getType() == TokenType.LBRACKET &&
                tokens.get(i + 3).getType() == TokenType.RBRACKET) {
                Token indexToken = tokens.get(i + 2);
                TokenType idxType = indexToken.getType();
                // skip array declarations (preceded by a type or signed/unsigned modifier)
                if (i > 0) {
                    TokenType prev = tokens.get(i - 1).getType();
                    if (!(isType(prev) || prev == TokenType.SIGNED || prev == TokenType.UNSIGNED)) {
                        if (idxType == TokenType.NUMBER_INT || idxType == TokenType.NUMBER_FLOAT || idxType == TokenType.LITERAL) {
                            reportError("Erro de Contexto: índice de vetor não pode ser literal.", indexToken);
                        }
                    }
                }
                continue;
            }
        }

        // After analysis, build a flattened symbol table so the UI can query declarations
        SymbolTable flattened = new SymbolTable(null);
        for (SymbolInfo si : allSymbols.values()) {
            flattened.add(si.name, si.type, si.line, si.value);
        }
        this.symbolTable = flattened;
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

    public boolean isSpecifier(TokenType type) {
        return type == TokenType.AUTO || type == TokenType.STATIC || type == TokenType.EXTERN || type == TokenType.CONST;
    }
}