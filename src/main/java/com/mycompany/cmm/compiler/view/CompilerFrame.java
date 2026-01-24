/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cmm.compiler.view;

import com.mycompany.cmm.compiler.model.SemanticAnalyzer;
import com.mycompany.cmm.compiler.model.SymbolInfo;
import com.mycompany.cmm.compiler.model.Token;
import com.mycompany.cmm.compiler.model.TokenType;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 *
 * @author caiof
 */
public class CompilerFrame extends JFrame {

    private final RSyntaxTextArea textArea;
    private final LexerParser lexerParser;
    private final JTable tokenTable;
    private final DefaultTableModel tableModel;
    private final SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(); 

    public CompilerFrame() {
        setTitle("IDE C-- Compiler");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);

        lexerParser = new LexerParser();
        textArea.addParser(lexerParser);

        RTextScrollPane scrollPane = new RTextScrollPane(textArea);

        String[] columns = {"Linha", "Coluna", "Tipo", "Lexema", "Valor"};
        tableModel = new DefaultTableModel(columns, 0);
        tokenTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(tokenTable);

        JTabbedPane bottomTabs = new JTabbedPane();
        bottomTabs.addTab("Tokens", tableScroll);
        bottomTabs.setPreferredSize(new Dimension(800, 200));

        JButton btnCompile = new JButton("Compilar / Atualizar Tabela");
        btnCompile.addActionListener(e -> atualizarTabela());

        JToolBar toolBar = new JToolBar();
        toolBar.add(btnCompile);

        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomTabs, BorderLayout.SOUTH);
    }

    private void atualizarTabela() {
        textArea.forceReparsing(lexerParser); 

        List<Token> tokens = lexerParser.getLastTokens();
        
        semanticAnalyzer.analyze(tokens); 

        tableModel.setRowCount(0);

        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            String valorColuna = "";

            if (t.getType() == TokenType.ID) {
                boolean isDeclaration = false;
                if (i > 0) {
                    TokenType prev = tokens.get(i - 1).getType();
                    if (prev == TokenType.INT || prev == TokenType.VOID || 
                        prev == TokenType.FLOAT || prev == TokenType.CHAR) {
                        isDeclaration = true;
                    }
                }

                if (!isDeclaration && !semanticAnalyzer.getSymbolTable().exists(t.getLexeme())) {
                    valorColuna = "ERRO: Não declarado"; //
                } else if (semanticAnalyzer.getSymbolTable().exists(t.getLexeme())) {
                    SymbolInfo info = semanticAnalyzer.getSymbolTable().get(t.getLexeme());
                    valorColuna = "Tipo: " + info.type;
                }
            } 
            else if (t.getType() == TokenType.RETURN && i + 1 < tokens.size()) {
                Token nextToken = tokens.get(i + 1);
                String typeError = semanticAnalyzer.checkTypeCompatibility(nextToken);
                if (typeError != null) {
                    valorColuna = "ERRO: " + typeError; //
                }
            }
            else if (t.getLiteral() != null) {
                valorColuna = t.getLiteral().toString();
            }

            tableModel.addRow(new Object[]{
                t.getLine(),
                t.getColumn(),
                t.getType(),
                t.getLexeme(),
                valorColuna
            });
        }
    }
}