package com.mycompany.cmm.compiler.view;

import com.mycompany.cmm.compiler.lexer.Lexer;
import com.mycompany.cmm.compiler.model.SemanticAnalyzer;
import com.mycompany.cmm.compiler.model.SyntaxAnalyzer;
import com.mycompany.cmm.compiler.model.SyntaxAnalyzer.SyntaxIssue;
import com.mycompany.cmm.compiler.model.Token;
import com.mycompany.cmm.compiler.model.TokenType;
import java.util.ArrayList;
import java.util.List;
import javax.swing.text.Element;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;
import org.fife.ui.rsyntaxtextarea.parser.ParserNotice;

/**
 * @author caiof
 */
public class LexerParser extends AbstractParser {

    private final List<Token> lastTokens = new ArrayList<>();
    private final SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
    private final SyntaxAnalyzer syntaxAnalyzer = new SyntaxAnalyzer();

    public LexerParser() {
        setEnabled(true);
    }

    @Override
    public ParseResult parse(RSyntaxDocument doc, String style) {
        DefaultParseResult result = new DefaultParseResult(this);
        lastTokens.clear();

        try {
            String text = doc.getText(0, doc.getLength());
            Lexer lexer = new Lexer(text);
            Token token = lexer.scan();

            while (token.getType() != TokenType.EOF) {
                lastTokens.add(token);
                if (token.getType() == TokenType.ERROR) {
                    addNotice(result, doc, token, "Erro Léxico: " + token.getLiteral(), ParserNotice.Level.ERROR);
                }
                token = lexer.scan();
            }

            for (SyntaxIssue issue : syntaxAnalyzer.analyze(lastTokens)) {
                ParserNotice.Level level = issue.isWarning() ? ParserNotice.Level.WARNING : ParserNotice.Level.ERROR;
                addNotice(result, doc, issue.getToken(), issue.getMessage(), level);
            }

            semanticAnalyzer.analyze(lastTokens); 

            for (int i = 0; i < lastTokens.size(); i++) {
                Token t = lastTokens.get(i);

                if (t.getType() == TokenType.ID) {
                    boolean isDeclaration = false;
                    if (i > 0) {
                        TokenType prevType = lastTokens.get(i - 1).getType();
                        if (semanticAnalyzer.isType(prevType)) {
                            isDeclaration = true;
                        }
                    }
                    
                    if (!isDeclaration && !semanticAnalyzer.getSymbolTable().exists(t.getLexeme())) {
                        addNotice(result, doc, t, "Erro Semântico: Variável '" + t.getLexeme() + "' não declarada.", ParserNotice.Level.ERROR);
                    }
                }

                if (t.getType() == TokenType.RETURN && i + 1 < lastTokens.size()) {
                    Token next = lastTokens.get(i + 1);
                    String typeError = semanticAnalyzer.checkTypeCompatibility(next);
                    if (typeError != null) {
                        addNotice(result, doc, next, typeError, ParserNotice.Level.ERROR);
                    }
                }
            }

            semanticAnalyzer.validateFunctionExit();

            for (String[] err : semanticAnalyzer.getSemanticErrors()) {
                int line = Integer.parseInt(err[1]);
                addNoticeAtLine(result, doc, line, err[0], err[2].length());
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.setError(e);
        }

        return result;
    }

    private void addNotice(DefaultParseResult result, RSyntaxDocument doc, Token token, String message, ParserNotice.Level level) {
        int offset = getOffset(doc, token.getLine(), token.getColumn());
        int length = token.getLexeme().length();

        DefaultParserNotice notice = new DefaultParserNotice(
            this, message, token.getLine() - 1, offset, length
        );
        notice.setLevel(level);
        result.addNotice(notice);
    }

    private void addNoticeAtLine(DefaultParseResult result, RSyntaxDocument doc, int line, String message, int length) {
        Element root = doc.getDefaultRootElement();
        if (line < 1 || line > root.getElementCount()) return;
        Element lineElem = root.getElement(line - 1);
        
        DefaultParserNotice notice = new DefaultParserNotice(
            this, message, line - 1, lineElem.getStartOffset(), length
        );
        notice.setLevel(ParserNotice.Level.ERROR);
        result.addNotice(notice);
    }
    
    private int getOffset(RSyntaxDocument doc, int line, int col) {
        Element root = doc.getDefaultRootElement();
        if (line < 1 || line > root.getElementCount()) return 0;
        Element lineElem = root.getElement(line - 1); 
        return lineElem.getStartOffset() + (col - 1);
    }

    public List<Token> getLastTokens() {
        return lastTokens;
    }
}