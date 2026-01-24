/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.cmm.compiler;

import com.mycompany.cmm.compiler.view.CompilerFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author caiof
 */
public class CmmCompiler {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new CompilerFrame().setVisible(true);
        });
    }
}