package Compiler.Frontend.ParseTree;

import Compiler.SymbolTable.Symbol;

import java.util.LinkedList;

public class ParseTreeNode {
    private transient ParseTreeNode parent;
    private Symbol symbol;
    private ParseTreeNode left;
    private ParseTreeNode right;
    private ParseTreeNode mid;
    private LinkedList<ParseTreeNode> linkedList;

    public ParseTreeNode(Symbol symbol) {
        this.symbol = symbol;
    }

    public ParseTreeNode(Symbol symbol, ParseTreeNode left, ParseTreeNode right) {
        this.symbol = symbol;
        this.left = left;
        this.right = right;
    }

    public ParseTreeNode(Symbol symbol, ParseTreeNode left, ParseTreeNode mid, ParseTreeNode right) {
        this.symbol = symbol;
        this.left = left;
        this.mid = mid;
        this.right = right;
    }

    public ParseTreeNode(Symbol symbol, LinkedList<ParseTreeNode> linkedList) {
        this.symbol = symbol;
        this.linkedList = linkedList;
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public ParseTreeNode getParent() {
        return parent;
    }

    public void setParent(ParseTreeNode parent) {
        this.parent = parent;
    }

    public ParseTreeNode getLeft() {
        return left;
    }

    public void setLeft(ParseTreeNode left) {
        this.left = left;
    }

    public ParseTreeNode getRight() {
        return right;
    }

    public void setRight(ParseTreeNode right) {
        this.right = right;
    }

    public ParseTreeNode getMid() {
        return mid;
    }

    public void setMid(ParseTreeNode mid) {
        this.mid = mid;
    }

    public LinkedList<ParseTreeNode> getLinkedList() {
        return linkedList;
    }
}
