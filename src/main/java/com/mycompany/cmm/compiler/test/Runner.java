package com.mycompany.cmm.compiler.test;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.mycompany.cmm.compiler.lexer.Lexer;
import com.mycompany.cmm.compiler.model.SemanticAnalyzer;
import com.mycompany.cmm.compiler.model.Token;
import com.mycompany.cmm.compiler.model.TokenType;

public class Runner {
    public static void main(String[] args) throws Exception {
        String path = "arq/testes/teste_completo.cmm";
        String src = new String(Files.readAllBytes(Paths.get(path)));
        Lexer lexer = new Lexer(src);
        List<Token> tokens = new ArrayList<>();
        Token t;
        do {
            t = lexer.scan();
            tokens.add(t);
        } while (t.getType() != TokenType.EOF && tokens.size() < 100000);

        System.out.println("Tokens:");
        for (int i = 0; i < tokens.size(); i++) {
            Token tt = tokens.get(i);
            System.out.println(i + ": " + tt.getType() + " \t '" + tt.getLexeme() + "' \t line=" + tt.getLine());
        }

        SemanticAnalyzer sa = new SemanticAnalyzer();
        sa.analyze(tokens);

        System.out.println("Semantic errors:");
        for (String[] e : sa.getSemanticErrors()) {
            System.out.println(e[0] + " [Linha " + e[1] + "] " + (e.length > 2 ? e[2] : ""));
        }
    }
}
