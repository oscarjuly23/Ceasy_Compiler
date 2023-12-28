package Compiler.Frontend;

import Compiler.Frontend.ParseTree.ParseTree;
import Compiler.Frontend.ParseTree.ParseTreeNode;
import Compiler.SymbolTable.Symbol;
import Compiler.SymbolTable.SymbolTable;
import Compiler.Frontend.Quadruple.Quadruple;
import Compiler.SymbolTable.Variable;
import Utils.FileManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class TAC {

    public static ArrayList<Quadruple> code = new ArrayList<>();
    public static ArrayList<Quadruple> codeElse = new ArrayList<>();
    public static int tmpCount = 1;
    private static int tmpCountIf = 1;
    private static int tmpIfCount = 0;
    private static int tmpCountWhile = 1;
    private static boolean upNdown = false;
    private static boolean setVars = true;
    private static boolean hasElse = false;
    private static boolean whileElse = false;


    public static void build() {
        // Creem a TAC totes les funcions
        SymbolTable.resetVisit(SymbolTable.topScope);
        Collections.reverse(SyntaxAnalyzer.parseTree.getRoot().getLinkedList());
        SyntaxAnalyzer.parseTree.getRoot().getLinkedList().forEach(TAC::readParseTree);

        //Marquem el final del programa (nomes en cas de tenir alguna funcio mes apart del main)
        if (SyntaxAnalyzer.parseTree.getRoot().getLinkedList().size() > 1) {
            code.add(new Quadruple("SECTION", "END_PROGRAM", null, null));
        }

        //Escrivim el fitxer del TAC
        FileManager fmTAC = new FileManager("file.cy.tac");
        for (Quadruple quad: TAC.code) {
            fmTAC.writeOnlyOneLine(quad.toString());
        }
    }

    /**
     * Funció per recorrer el parse tree
     */
    private static void readParseTree(ParseTreeNode node) {
        //Mirem si es un node en el qual hem de traduir a codi
        if (ParseTree.isOperation(node)){
            //Si es terminal mirem quina producció l'ha generat
            switch (node.getSymbol().getRegex()) {
                case "BLOCK_STATEMENT":
                    ParseTreeNode scopeNode = null;
                    //Marquem l'inici de lescope
                    if (node.getParent().getSymbol().getRegex().equals("FUNC")) {
                        scopeNode = node.getParent().getLeft().getLeft();
                    } else if (node.getParent().getSymbol().getRegex().equals("CONDITIONAL_BLOCK")) {
                        scopeNode = node.getParent().getParent().getLeft();
                    } else if (node.getParent().getSymbol().getRegex().equals("ELSE_EXPRESSION")) {
                        scopeNode = node.getParent().getLeft();
                    }

                    //Creem les variables del programa
                    if (setVars) {
                        setVars = false;
                        scopeVars();
                    }
                    SymbolTable.scopeIn();
                    scopeOpen(scopeNode);

                    //Seguir llegint tots els blocs statments
                    for (ParseTreeNode blockStatementConcat : node.getLinkedList()) {
                        readParseTree(blockStatementConcat);
                    }

                    //Marca final de lescope
                    SymbolTable.scopeOut();
                    scopeClose(scopeNode.getSymbol());
                    //Sortim de la funció de read amb el node actual i en seguim explorant d'altres
                    break;
                case "ASSIGNMENT":
                    readParseTree(node.getRight());
                    codeAdd(node.getLeft().getSymbol().getToken(), "t" + (tmpCount - 1), node.getMid().getSymbol().getRegex(), null);
                    break;
                case "VAR_INIT_STATEMENT":
                    readParseTree(node.getRight());
                    codeAdd(node.getParent().getLeft().getRight().getSymbol().getToken(), "t" + (tmpCount - 1), node.getMid().getSymbol().getRegex(), null);
                    break;
                case "OPERACIO":
                    if (ParseTree.isTerminal(node.getLeft())) {
                        codeAdd("t" + tmpCount++, node.getLeft().getSymbol().getToken(), "EQUAL", null);//node.getLeft().getParent().getParent().getParent().getLeft().getRight().getSymbol().getToken());
                    } else {
                        //Tenim operacio basica a get left
                        upNdown = false;
                        readParseTree(node.getLeft());
                    }
                    break;
                case "IF_EXPRESSION":
                    //Fer TAC if
                    //Operacions de la condicio
                    tmpIfCount = tmpCountIf++;

                    //Quad per l'inici de l'if
                    codeAdd("IF_LABEL_" + tmpIfCount + "_INIT", null, "SECTION", null);

                    //Operacions de la condicio
                    readParseTree(node.getRight());

                    //Quad per if
                    if (hasElse) {

                        codeAdd(null, "t" + (tmpCount - 1), "IF", "IF_LABEL_" + tmpIfCount + "_ELSE");
                        //Block_statment if amb else
                        readParseTree(node.getRight().getLeft().getRight());
                        codeAdd("IF_LABEL_" + tmpIfCount + "_END", null, "JUMP", null);
                        codeAdd("IF_LABEL_" + tmpIfCount + "_ELSE", null, "SECTION", null);
                        readParseTree(node.getRight().getRight().getLeft());//.getRight());
                    } else {
                        codeAdd(null, "t" + (tmpCount - 1), "IF", "IF_LABEL_" + tmpIfCount + "_END");
                        //Readtree node left.right
                        //Block_statment if
                        readParseTree(node.getRight().getRight());
                    }

                    //Tancar l'etiqueta de l'if
                    codeAdd("IF_LABEL_" + tmpIfCount + "_END", null, "SECTION", null);
                    hasElse = false;
                    break;
                case "ELSE_EXPRESSION":
                    if (hasElse) {
                        //Codi else
                        whileElse = true;
                        readParseTree(node.getRight());
                        whileElse = false;
                        code.addAll(codeElse);
                        codeElse.clear();
                    }
                    hasElse = true;
                    break;
                case "WHILE_EXPRESSION":
                    //Fer TAC while
                    int tmpWhileCount = tmpCountWhile++;

                    //Quad per l'inici del while
                    codeAdd("WHILE_LABEL_" + tmpWhileCount + "_INIT", null, "SECTION", null);

                    //Operacions de la condicio
                    readParseTree(node.getRight());

                    //Quad per while
                    codeAdd(null, "t" + (tmpCount - 1), "WHILE", "WHILE_LABEL_" + tmpWhileCount + "_END");

                    //Readtree node left.right
                    //Block_statment
                    readParseTree(node.getRight().getRight());

                    //Fem goto per tornar a l'inici del while a evaluar la condicio
                    codeAdd("WHILE_LABEL_" + tmpWhileCount + "_INIT", null, "JUMP", null);
                    codeAdd("WHILE_LABEL_" + tmpWhileCount + "_END", null, "SECTION", null);
                    break;
                case "CONDITIONAL_BLOCK":
                    //Gestiona una condicions
                    codeAdd("t" + tmpCount++, node.getLeft().getLeft().getSymbol().getToken(), node.getLeft().getMid().getSymbol().getRegex(), node.getLeft().getRight().getSymbol().getToken());
                    break;
                case "CALL_FUNC":
                    //Ens suda la potlla el return, es una crida d'una funció a pelo

                    //comprovem els params
                    String arg2 = null; //Iniciem a null si no hi ha parametre
                    if (node.getRight() != null) {
                        //En els dos casos (variable o integer) tenim les dades en el mateix lloc
                        codeAdd("t" + tmpCount, node.getRight().getSymbol().getToken(), "EQUAL", null);
                        arg2 = "t" + tmpCount;
                        tmpCount++;
                    }

                    //Comprovem si tenim que posar return
                    String tmp = null; //Iniciem a null si no hi ha parametre
                    if (!node.getParent().getSymbol().getRegex().equals("BLOCK_STATEMENT_CONCAT")) {
                        tmp = "t" + tmpCount;
                        tmpCount++;
                    }

                    //Afeim la quadrupleta de crida a funcio sense return
                    // tmp  == variable on guardar el return (sempre registre $tx) | OPCIONAL (null)
                    // arg1 == nom de la funció a cridar
                    // arg2 == parametre que li estem passant a la funció (sempre registre $tx) | OPCIONAL (null)
                    codeAdd(tmp, node.getLeft().getSymbol().getToken(), "CALL_FUNC", arg2);
                    break;
                case "RETURN_INSIDE_FUNCTION":
                    String arg1 = null;
                    if (node.getRight() != null) {
                        //En els dos casos (variable o integer) tenim les dades en el mateix lloc
                        codeAdd("t" + tmpCount, node.getRight().getSymbol().getToken(), "EQUAL", null);
                        arg1 = "t" + tmpCount;
                        tmpCount++;
                    }

                    //Afeim la quadrupleta de return
                    //Arg1 == parametre que retorna la funció (sempre registre $tx)
                    codeAdd(null, arg1, "RETURN", null);
                    break;
                default: // OPERACIO_BASICA | SUMA | SUMA_PRIMA | ...
                        //case "OPERACIO_BASICA":
                        //case "SUMA_PRIMA":
                        //case "SUMA":
                    // Si els dos nodes son terminals afegim la operacio i tornem amunt
                    if (ParseTree.isTerminal(node.getLeft()) && ParseTree.isTerminal(node.getRight())) {
                        upNdown = true;
                        codeAdd("t" + tmpCount++, node.getLeft().getSymbol().getToken(), node.getMid().getSymbol().getRegex(), node.getRight().getSymbol().getToken());
                        return;
                    }

                    // Si el node de la dreta NO es terminal hi tenim una operacio amb mes prioritat
                    if (!ParseTree.isTerminal(node.getRight())) {
                        if (node.getLeft().getLeft() != null && node.getLeft().getMid() != null) {
                            codeAdd("t" + tmpCount++, node.getLeft().getLeft().getSymbol().getToken(), node.getLeft().getMid().getSymbol().getRegex(), node.getLeft().getRight().getSymbol().getToken());
                        }
                        readParseTree(node.getRight());
                        if (node.getLeft().getLeft() != null) {
                            codeAdd("t" + tmpCount++, "t" + (tmpCount - 2), node.getMid().getSymbol().getRegex(), "t1");
                        }
                    }

                    if (ParseTree.isTerminal(node.getLeft())) {
                        upNdown = true;
                        codeAdd("t" + tmpCount++, node.getLeft().getSymbol().getToken(), node.getMid().getSymbol().getRegex(), "t" + (tmpCount-2));
                        return;
                    }

                    if (!upNdown) {
                        readParseTree(node.getLeft());
                        codeAdd("t" + tmpCount++,"t" + (tmpCount - 2), node.getMid().getSymbol().getRegex(), node.getRight().getSymbol().getToken());
                    }

                    break;
            }
        } else if (!ParseTree.isTerminal(node)) {
            //Si no es un node que hem de traduir a codi
            //Si no es un node terminal
            //Seguim recorrent left, mid i right
            for (int i = 0; i < 3; i++) {
                //Left
                if (i == 0) {
                    ParseTreeNode left = node.getLeft();
                    if (left != null) {
                        readParseTree(left);
                    }
                    //Mid
                } else if (i == 1) {
                    ParseTreeNode mid = node.getMid();
                    if (mid != null) {
                        readParseTree(mid);
                    }
                    //Right
                } else {
                    ParseTreeNode right = node.getRight();
                    //Recorrem la linkedlist en cas que sigui un BLOCK_STATEMENT
                    if (right != null) {
                        readParseTree(right);
                    }
                }
            }
        }
        if (node.getParent() == null && node.getLeft().getLeft() != null && node.getLeft().getLeft().getSymbol().getRegex().equals("MAIN") && SyntaxAnalyzer.parseTree.getRoot().getLinkedList().size() > 1) {
            codeAdd(null, null, "END_PROGRAM", null);
        }
    }

    private static void scopeOpen(ParseTreeNode node) {
        /* TAC: Etiqueta amb el nom de la funcio
        FUNC_NAME_INIT:
         */
        if (!node.getSymbol().getRegex().equals("CONDITIONAL_BLOCK") && !node.getSymbol().getRegex().equals("ELSE") && !node.getSymbol().getRegex().equals("IF")) {
            code.add(new Quadruple("SECTION", node.getSymbol().getToken(), null, null));
        }
        //Rebem els parametres
        if (node.getParent().getParent().getSymbol().getRegex().equals("FUNC")) {
            if (node.getParent().getMid() != null) {
                codeAdd(node.getParent().getMid().getRight().getSymbol().getToken(),"a0", "EQUAL", null);
            }
        }

        //System.out.println("open scope: " + node.getSymbol().getRegex());
    }
    private static void scopeClose(Symbol sym) {
        /* TAC: Etiqueta amb el nom de la funcio
        FUNC_NAME_END:
         */
        //System.out.println("close scope: " + sym.getRegex());
    }

    public static void codeAdd(String tmp, String arg1, String op, String arg2) {
        if (!whileElse) {
            code.add(new Quadruple(op, tmp, arg1, arg2));
        } else  {
            codeElse.add(new Quadruple(op, tmp, arg1, arg2));
        }
        //System.out.println(tmp + " = " + arg1 + " " + op + " " + arg2);
    }

    private static void scopeVars() {
        if (!SymbolTable.topScope.localScopeSymbols.isEmpty()) {
            TAC.code.add(new Quadruple("MIPS_DATA", "data", null, null));
            SymbolTable.resetVisit(SymbolTable.topScope);
            SymbolTable.getAllVars(SymbolTable.topScope);
            SymbolTable.allVars.forEach( value -> {
                if (value instanceof Variable) TAC.codeAdd(value.getToken(), null, "VARIABLE", null);
            });
            TAC.code.add(new Quadruple("MIPS_TEXT", "text", null, null));
        }
    }
}
