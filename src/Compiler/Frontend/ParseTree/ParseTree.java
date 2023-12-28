package Compiler.Frontend.ParseTree;

import Compiler.Frontend.SemanticAnalyzer;
import Compiler.SymbolTable.SymbolTable;

import java.util.Arrays;

public class ParseTree {
    private ParseTreeNode root;

    public ParseTree(ParseTreeNode root) {
        this.root = root;
    }

    public ParseTree() {
    }

    public static boolean isOperation(ParseTreeNode node) {
        String[] types = {"BLOCK_STATEMENT", "ASSIGNMENT", "OPERACIO", "OPERACIO_BASICA", "SUMA_RES_PRIMA", "SUMA_RES",
                "IF_EXPRESSION", "CONDITIONAL_BLOCK", "WHILE_EXPRESSION", "CALL_FUNC", "RETURN_INSIDE_FUNCTION",
                "MUL_DIV_PRIMA", "MODUL_PRIMA", "VAR_INIT_STATEMENT", "ELSE_EXPRESSION"};
        return Arrays.asList(types).contains(node.getSymbol().getRegex());
    }

    public ParseTreeNode getRoot() {
        return root;
    }

    public void setRoot(ParseTreeNode root) {
        this.root = root;
    }

    /**
     * Funció que comproba si el símbol de l'operació és un terminal
     */
    public static Boolean isTerminal(ParseTreeNode parseTreeNode) {
        // Si tots els nodes són null retornarem que és un terminal
        if (parseTreeNode == null) {
            return false;
        }
        return parseTreeNode.getLeft() == null && parseTreeNode.getMid() == null && parseTreeNode.getRight() == null;
    }
}
