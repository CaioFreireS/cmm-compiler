package com.mycompany.cmm.compiler.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SyntaxAnalyzer {

    public static class SyntaxIssue {
        private final String message;
        private final Token token;
        private final boolean warning;

        public SyntaxIssue(String message, Token token, boolean warning) {
            this.message = message;
            this.token = token;
            this.warning = warning;
        }

        public String getMessage() {
            return message;
        }

        public Token getToken() {
            return token;
        }

        public boolean isWarning() {
            return warning;
        }
    }

    private static final List<TokenType> ASSIGN_OPERATORS = Arrays.asList(
        TokenType.ASSIGN, TokenType.PLUS_ASSIGN, TokenType.MINUS_ASSIGN,
        TokenType.MULT_ASSIGN, TokenType.DIV_ASSIGN
    );

    private List<Token> tokens;
    private int current;
    private List<SyntaxIssue> issues;

    public List<SyntaxIssue> analyze(List<Token> input) {
        this.tokens = new ArrayList<>(input);
        this.tokens.add(new Token(TokenType.EOF, "", 0, 0));
        this.current = 0;
        this.issues = new ArrayList<>();

        while (!isAtEnd()) {
            if (check(TokenType.ERROR)) {
                Token err = advance();
                issues.add(new SyntaxIssue("Erro Léxico: " + err.getLiteral(), err, false));
                continue;
            }
            declaration();
        }

        return issues;
    }

    private void declaration() {
        if (isAtEnd()) return;

        if (isType(peek().getType()) && lookAheadType(1) == TokenType.ID) {
            Token typeToken = advance();
            Token identifier = consume(TokenType.ID, "Esperado identificador.");

            if (match(TokenType.LPAREN)) {
                parseParameters();
                consume(TokenType.RPAREN, "Esperado ')' apos parametros de funcao.");
                parseBlock();
            } else {
                parseVariableTail();
                consume(TokenType.SEMICOLON, "Esperado ';' ao final da declaracao.");
            }
            return;
        }

        statement();
    }

    private void parseVariableTail() {
        if (match(TokenType.LBRACKET)) {
            expression();
            consume(TokenType.RBRACKET, "Esperado ']' apos declaracao de vetor.");
        }

        if (ASSIGN_OPERATORS.contains(peek().getType())) {
            advance();
            expression();
        }
    }

    private void parseParameters() {
        if (check(TokenType.RPAREN)) return;

        do {
            if (!isType(peek().getType())) {
                issues.add(new SyntaxIssue("Esperado tipo de parametro.", peek(), false));
                break;
            }
            advance();
            consume(TokenType.ID, "Esperado nome do parametro.");
        } while (match(TokenType.COMMA));
    }

    private void parseBlock() {
        if (!match(TokenType.LBRACE)) {
            issues.add(new SyntaxIssue("Esperado '{' para iniciar bloco.", peek(), false));
            return;
        }

        while (!check(TokenType.RBRACE) && !isAtEnd()) {
            declaration();
        }
        consume(TokenType.RBRACE, "Esperado '}' para fechar bloco.");
    }

    private void statement() {
        if (match(TokenType.LBRACE)) {
            current--;
            parseBlock();
            return;
        }

        if (match(TokenType.IF)) {
            consume(TokenType.LPAREN, "Esperado '(' apos 'if'.");
            expression();
            consume(TokenType.RPAREN, "Esperado ')' apos condicao do 'if'.");
            statement();
            if (match(TokenType.ELSE)) {
                statement();
            }
            return;
        }

        if (match(TokenType.WHILE)) {
            consume(TokenType.LPAREN, "Esperado '(' apos 'while'.");
            expression();
            consume(TokenType.RPAREN, "Esperado ')' apos condicao do 'while'.");
            statement();
            return;
        }

        if (match(TokenType.FOR)) {
            consume(TokenType.LPAREN, "Esperado '(' apos 'for'.");
            if (!check(TokenType.SEMICOLON)) {
                if (isType(peek().getType()) && lookAheadType(1) == TokenType.ID) {
                    advance();
                    advance();
                    parseVariableTail();
                } else {
                    expression();
                }
            }
            consume(TokenType.SEMICOLON, "Esperado ';' na inicializacao do 'for'.");

            if (!check(TokenType.SEMICOLON)) {
                expression();
            }
            consume(TokenType.SEMICOLON, "Esperado ';' na condicao do 'for'.");

            if (!check(TokenType.RPAREN)) {
                expression();
            }
            consume(TokenType.RPAREN, "Esperado ')' apos clausulas do 'for'.");
            statement();
            return;
        }

        if (match(TokenType.RETURN)) {
            if (!check(TokenType.SEMICOLON)) {
                expression();
            }
            consume(TokenType.SEMICOLON, "Esperado ';' apos retorno.");
            return;
        }

        if (match(TokenType.BREAK)) {
            consume(TokenType.SEMICOLON, "Esperado ';' apos 'break'.");
            return;
        }

        expressionStatement();
    }

    private void expressionStatement() {
        ExpressionInfo info = expression();
        Token end = consume(TokenType.SEMICOLON, "Esperado ';' apos expressao.");
        if (info.isLonelyIdentifier()) {
            Token ref = info.reference != null ? info.reference : end;
            issues.add(new SyntaxIssue("Instrução com identificador isolado.", ref, true));
        }
    }

    private ExpressionInfo expression() {
        return assignment();
    }

    private ExpressionInfo assignment() {
        ExpressionInfo left = equality();

        if (ASSIGN_OPERATORS.contains(peek().getType())) {
            Token op = advance();
            ExpressionInfo right = assignment();
            return ExpressionInfo.fromAssignment(op, left, right);
        }

        return left;
    }

    private ExpressionInfo equality() {
        ExpressionInfo expr = comparison();

        while (match(TokenType.EQ, TokenType.NEQ)) {
            Token op = previous();
            ExpressionInfo right = comparison();
            expr = ExpressionInfo.combine(expr, right, op);
        }

        return expr;
    }

    private ExpressionInfo comparison() {
        ExpressionInfo expr = term();

        while (match(TokenType.LT, TokenType.LE, TokenType.GT, TokenType.GE)) {
            Token op = previous();
            ExpressionInfo right = term();
            expr = ExpressionInfo.combine(expr, right, op);
        }

        return expr;
    }

    private ExpressionInfo term() {
        ExpressionInfo expr = factor();

        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token op = previous();
            ExpressionInfo right = factor();
            expr = ExpressionInfo.combine(expr, right, op);
        }

        return expr;
    }

    private ExpressionInfo factor() {
        ExpressionInfo expr = unary();

        while (match(TokenType.MULTIPLY, TokenType.DIVIDE, TokenType.MOD)) {
            Token op = previous();
            ExpressionInfo right = unary();
            expr = ExpressionInfo.combine(expr, right, op);
        }

        return expr;
    }

    private ExpressionInfo unary() {
        if (match(TokenType.NOT, TokenType.MINUS)) {
            Token op = previous();
            ExpressionInfo right = unary();
            return ExpressionInfo.fromUnary(op, right);
        }
        return primary();
    }

    private ExpressionInfo primary() {
        if (match(TokenType.NUMBER_INT, TokenType.NUMBER_FLOAT, TokenType.LITERAL)) {
            return ExpressionInfo.fromLiteral(previous());
        }

        if (match(TokenType.ID)) {
            Token identifier = previous();
            ExpressionInfo info = new ExpressionInfo(identifier, true, false, false, false);

            if (match(TokenType.LPAREN)) {
                if (!check(TokenType.RPAREN)) {
                    do {
                        expression();
                    } while (match(TokenType.COMMA));
                }
                consume(TokenType.RPAREN, "Esperado ')' apos argumentos.");
                info.identifierOnly = false;
                info.functionCall = true;
            } else if (match(TokenType.LBRACKET)) {
                Token indexToken = peek();
                ExpressionInfo indexExpr = expression();
                consume(TokenType.RBRACKET, "Esperado ']' apos indice de vetor.");
                info.identifierOnly = false;
                if (indexExpr.literalOnly && indexToken != null) {
                    issues.add(new SyntaxIssue("Erro de Contexto: índice de vetor não pode ser literal.", indexToken, false));
                }
            }
            return info;
        }

        if (match(TokenType.LPAREN)) {
            ExpressionInfo expr = expression();
            consume(TokenType.RPAREN, "Esperado ')' apos expressao.");
            return expr;
        }

        Token err = advance();
        issues.add(new SyntaxIssue("Expressão inválida.", err, false));
        return new ExpressionInfo(err, false, false, false, false);
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        issues.add(new SyntaxIssue(message, peek(), false));
        if (!isAtEnd()) return advance();
        return new Token(type, "", peek().getLine(), peek().getColumn());
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return current >= tokens.size() || peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private TokenType lookAheadType(int distance) {
        if (current + distance >= tokens.size()) return TokenType.EOF;
        return tokens.get(current + distance).getType();
    }

    private boolean isType(TokenType type) {
        return type == TokenType.INT || type == TokenType.FLOAT || type == TokenType.CHAR ||
               type == TokenType.VOID || type == TokenType.DOUBLE || type == TokenType.LONG ||
               type == TokenType.SHORT;
    }

    private static final class ExpressionInfo {
        private boolean identifierOnly;
        private boolean literalOnly;
        private boolean functionCall;
        private boolean assignment;
        private final Token reference;

        private ExpressionInfo(Token reference, boolean identifierOnly, boolean literalOnly, boolean functionCall, boolean assignment) {
            this.reference = reference;
            this.identifierOnly = identifierOnly;
            this.literalOnly = literalOnly;
            this.functionCall = functionCall;
            this.assignment = assignment;
        }

        static ExpressionInfo fromLiteral(Token token) {
            return new ExpressionInfo(token, false, true, false, false);
        }

        static ExpressionInfo fromUnary(Token op, ExpressionInfo right) {
            return new ExpressionInfo(op, false, right.literalOnly, right.functionCall, right.assignment);
        }

        static ExpressionInfo fromAssignment(Token op, ExpressionInfo left, ExpressionInfo right) {
            return new ExpressionInfo(op, false, false, left.functionCall || right.functionCall, true);
        }

        static ExpressionInfo combine(ExpressionInfo left, ExpressionInfo right, Token op) {
            boolean lit = left.literalOnly && right.literalOnly;
            boolean fn = left.functionCall || right.functionCall;
            boolean assign = left.assignment || right.assignment;
            return new ExpressionInfo(op, false, lit, fn, assign);
        }

        boolean isLonelyIdentifier() {
            return identifierOnly && !functionCall && !assignment;
        }
    }
}
