/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cmm.compiler.view;

import com.mycompany.cmm.compiler.lexer.Lexer;
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
 *
 * @author caiof
 */
public class LexerParser extends AbstractParser {

    private final List<Token> lastTokens = new ArrayList<>();

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
                    int offset = getOffset(doc, token.getLine(), token.getColumn());
                    int length = token.getLexeme().length();

                    DefaultParserNotice notice = new DefaultParserNotice(
                        this, 
                        "Erro Léxico: " + token.getLiteral(),
                        token.getLine() - 1,
                        offset, 
                        length
                    );
                    notice.setLevel(ParserNotice.Level.ERROR);
                    result.addNotice(notice);
                }
                
                token = lexer.scan();
            }

        } catch (Exception e) {
            e.printStackTrace();
            result.setError(e);
        }

        return result;
    }
    
    private int getOffset(RSyntaxDocument doc, int line, int col) {
        Element root = doc.getDefaultRootElement();
        Element lineElem = root.getElement(line - 1); 
        return lineElem.getStartOffset() + (col - 1);
    }

    public List<Token> getLastTokens() {
        return lastTokens;
    }
}
