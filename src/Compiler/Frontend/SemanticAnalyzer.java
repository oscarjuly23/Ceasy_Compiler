package Compiler.Frontend;

import Compiler.Frontend.ParseTree.ParseTree;
import Compiler.Frontend.ParseTree.ParseTreeNode;
import Compiler.SymbolTable.Function;
import Compiler.SymbolTable.Symbol;
import Compiler.SymbolTable.SymbolTable;
import Compiler.SymbolTable.Variable;
import Errors.ErrorHandler;
import Errors.NodeError;

import java.util.Arrays;
import java.util.LinkedList;

public class SemanticAnalyzer {
    private static Symbol symbolActual;
    private static final String[] types = {"FLOAT_VALUE", "INT_VALUE", "STRING_CONTENT", "TRUE", "FALSE", "BOOL"};
    private static LinkedList<String> condBlockVars = new LinkedList<String>();
    private static boolean isInit = true;
    private static String lastParam = null;

    public static void semanticAnalyzerTree() {
        //Aplicar recursividad e ir recorriendo arbol
        SymbolTable.scopeIn();
        //Llegim l'array on es guardan les funcions d'entry point i llegim cadascuna per separat
        for (ParseTreeNode node : SyntaxAnalyzer.parseTree.getRoot().getLinkedList()) {
            //Llegim el ParseTree generat per cada funció
            readParseTree(node,"");
        }
        SymbolTable.scopeOut();
    }

    /**
     * Comprova el tipus de node i recorre l'arbre fins arribar als símbols terminals
     * @param parseTreeNode Node actual
     */
    private static void checkNode(ParseTreeNode parseTreeNode) {
        switch (parseTreeNode.getSymbol().getRegex()) {
            case "VAR_STATEMENT":
                //Llegim el parse tree per la producció VAR_STATEMENT
                readParseTree(parseTreeNode, "VAR_STATEMENT");
                isInit = true;
                break;
            case "IF_EXPRESSION":
                readParseTree(parseTreeNode.getRight(), "IF_EXPRESSION");
                //Comprovem els tipus de les expressions de 2 en 2 en una IF_EXPRESSION
                for (int i = 0; i < condBlockVars.size(); i+=2) {
                    if (condBlockVars.get(i).equals(condBlockVars.get(i+1)) || condBlockVars.get(i).contains(condBlockVars.get(i+1)) || condBlockVars.get(i+1).contains(condBlockVars.get(i))) {
                    } else {
                        ErrorHandler.addError(new NodeError(parseTreeNode.getSymbol().getLineNumber(),"Invalid IF expression","Semantic Error", "Need to evaluate ids of the same type", condBlockVars.get(i) + " != " + condBlockVars.get(i+1), ""));
                    }
                }
                //Reinicialitzem el array per la seguent IF_EXPRESSION
                condBlockVars = new LinkedList<>();
                break;
            case "WHILE_EXPRESSION":
                readParseTree(parseTreeNode.getRight(), "WHILE_EXPRESSION");
                //Comprovem els tipus de les expressions de 2 en 2 en una IF_EXPRESSION
                for (int i = 0; i < condBlockVars.size(); i+=2) {
                    if (condBlockVars.get(i).equals(condBlockVars.get(i+1)) || condBlockVars.get(i).contains(condBlockVars.get(i+1)) || condBlockVars.get(i+1).contains(condBlockVars.get(i))) {
                    } else {
                        ErrorHandler.addError(new NodeError(parseTreeNode.getSymbol().getLineNumber(),"Invalid WHILE expression","Semantic Error", "Need to evaluate ids of the same type", condBlockVars.get(i) + " != " + condBlockVars.get(i+1), ""));
                    }
                }
                //Reinicialitzem el array per la seguent IF_EXPRESSION
                condBlockVars = new LinkedList<>();
                break;
            case "ASSIGNMENT":
                //Llegim el parse tree per la producció ASSIGNMENT
                readParseTree(parseTreeNode, "ASSIGNMENT");
                break;
            case "RETURN_INSIDE_FUNCTION":
                //Es comprova quan hi ha un return i es mira el tipus que ha de retornar la funció i el tipus que realment retorna
                //Mirem el tipus de return de la funció
                Symbol funcInfo = getParentFunc(parseTreeNode);

                //Variable que retorna
                Symbol symbol = null;
                //Comprova si retorna un valor
                if (parseTreeNode.getRight() != null) {
                    symbol = SymbolTable.findSymbol(parseTreeNode.getRight().getSymbol().getToken());
                }

                //Comrpova que la funció retorna void i si retorna un valor dona error
                if (funcInfo.getRegex().equals("VOID") && parseTreeNode.getRight() != null) {
                    ErrorHandler.addError(new NodeError(parseTreeNode.getRight().getSymbol().getLineNumber(),"Void return","Semantic Error", "Void func can't return values", parseTreeNode.getRight().getSymbol().getToken(), ""));
                    break;
                }
                //Es casteja a una Variable per poder tractar-lo
                symbolActual = new Variable(funcInfo.getToken(), funcInfo.getRegex(), funcInfo.getLineNumber(), funcInfo.getRegex());
                //Comprova que la funció no retorna void i té un valor de return
                if (!funcInfo.getRegex().equals("VOID")) {
                    //Si es null es directament un valor i es comprova que estigui al arbre
                    if (parseTreeNode.getRight() != null) {
                        if (!(symbol instanceof Variable)) {
                            symbol = parseTreeNode.getRight().getSymbol();
                        }
                        //Comprova que la operació és permesa
                        checkPermitedOps(symbol);
                    } else {
                        ErrorHandler.addError(new NodeError(parseTreeNode.getSymbol().getLineNumber(),"Return type","Semantic Error", "This func must be return a value ", funcInfo.getToken(), ""));
                    }
                }
                break;
            default:
                break;
        }
    }

    private static Symbol getParentFunc(ParseTreeNode node) {
        while (!node.getSymbol().getRegex().equals("FUNC")) {
            node = node.getParent();
        }
        return node.getLeft().getRight().getSymbol();
    }

    /**
     * Comproba el regex d'un símbol terminal (variables) i els tipus dels valors d'operació assingats en la inicialització.
     * @param symbol Symbol terminal
     */
    private static void checkSymbol(Symbol symbol) {
        //Comprovem si el simbol actual es un parametre d'una funcio
        if (lastParam != null) {
            Variable var;
            //Mirem si el parametre es una variable i agafem el seu tipus
            if (symbol.getRegex().equals("NAME")) {
                var = (Variable) SymbolTable.findSymbol(symbol.getToken());
                if (!var.getTipus().equals(lastParam)) {
                    ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"Invalid param","Semantic Error", "This func expected " + lastParam + " param type", symbol.getToken(), ""));
                }
                lastParam = null;
                return;
            }
            //Mirem si el parametre es un valor
            if (!symbol.getRegex().contains(lastParam)) {
                ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"Invalid param","Semantic Error", "This func expected " + lastParam + " param type", symbol.getToken(), ""));
            }
            lastParam = null;
            return;
        }
        switch (symbol.getRegex()) {
            case "NAME":
                //Comprova si es una inicialització o la variable
                if (isInit) {
                    isInit = false;
                    symbolActual = SymbolTable.findSymbol(symbol.getToken());
                    if (symbolActual == null) {
                        ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"Out scope","Semantic Error", "Variable not found in scope", symbol.getToken(), ""));
                        break;
                    }
                } else {
                    //Comprova que la operació és permesa
                    checkPermitedOps(SymbolTable.findSymbol(symbol.getToken()));
                }
                break;
            case "FUNC_ID":
                //Comprova si la inicialització o asignació es una funció
                //Es comprova si existeix a la taula de symbol i s'obté la informació
                Function function = (Function) SymbolTable.findSymbol(symbol.getToken());
                //Comprova si al cridar la funció existeix
                if (function == null) {
                    ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"Out scope","Semantic Error", "Function not found in scope", symbol.getToken(), ""));
                    break;
                }
                //Si la funció retorna void no es pot assignar
                if (function.getReturns().get(0).getRegex().equals("VOID")) {
                    ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"Void return","Semantic Error", "Void func can't be assigned to variable", function.getToken(), ""));
                    break;
                }
                //Si la funció te parametres guardem el tipus del parametre que espera
                if (function.getParameters().size() > 0) {
                    lastParam = function.getParameters().get(0).getTipus();
                }
                //Comprova que la operació és permesa
                checkPermitedOps(new Variable(function.getToken(), function.getRegex(), function.getLineNumber(), function.getReturns().get(0).getRegex()));
                System.out.println(function.getReturns().get(0));
                break;
            default:
                //Comprova que la operació és permesa
                checkPermitedOps(symbol);
                break;
        }
    }

    /**
     * Afegix els tipus dels ids d'una expressió condicional a un array
     * @param symbol Symbol terminal
     */
    private static void checkConditionalBlock(Symbol symbol) {
        if (Arrays.stream(types).anyMatch(symbol.getRegex()::equals)) {
            condBlockVars.add(symbol.getRegex());
        } else if (symbol.getRegex().equals("NAME")) {
            Variable sym = (Variable) SymbolTable.findSymbol(symbol.getToken());
            condBlockVars.add(sym.getTipus());
        }
    }

    /**
     * Comprova que les assingacions a una variable compleixen amb el tipatge i especificacions del llenguatge
     * @param symbol symbol actual llegit
     */
    private static void checkPermitedOps(Symbol symbol) {
        //Comproba que correspon el tipus de variable amb el valor assignat. Amb els bool es mira si es true o false.
        if (symbolActual != null && Arrays.stream(types).anyMatch(symbol.getRegex()::equals)) {
            //STRING accepta inicialització de INT_VALUE, FLOAT_VALUE, STRING_CONTENT
            if (((Variable) symbolActual).getTipus().equals("STRING")) {
                //Si la inicialització és un bool KO
                if (symbol.getRegex().equals(types[5])) {
                    System.out.println("KO");
                    System.out.println(((Variable) symbolActual).getTipus() + " != " + symbol.getRegex());
                    ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"Operation not allowed","Semantic Error", "string type don't allow bool values", symbol.getToken(), "Expected: INT, FLOAT or string"));
                } else {
                    System.out.println("OK");
                    System.out.println(((Variable) symbolActual).getTipus() + " == " + symbol.getRegex());
                }
                //FLOAT accepta inicialització de INT_VALUE, FLOAT_VALUE
            } else if (((Variable) symbolActual).getTipus().equals("FLOAT")) {
                if (symbol.getRegex().contains(((Variable) symbolActual).getTipus()) || symbol.getRegex().contains("INT")) {
                    System.out.println("OK");
                    System.out.println(((Variable) symbolActual).getTipus() + " == " + symbol.getRegex());
                } else {
                    System.out.println("KO");
                    System.out.println(((Variable) symbolActual).getTipus() + " != " + symbol.getRegex());
                    ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"Operation not allowed","Semantic Error", "Float type don't allow bool or string values", symbol.getToken(), "Expected: INT or FLOAT"));
                }
                //INT accepta inicialització de INT_VALUE
            } else if (((Variable) symbolActual).getTipus().equals("INT")) {
                if (symbol.getRegex().contains(((Variable) symbolActual).getTipus())) {
                    System.out.println("OK");
                    System.out.println(((Variable) symbolActual).getTipus() + " == " + symbol.getRegex());
                } else {
                    System.out.println("KO");
                    System.out.println(((Variable) symbolActual).getTipus() + " != " + symbol.getRegex());
                    ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"Operation not allowed","Semantic Error", "Int type only allows int values", symbol.getToken(), "Expected: INT"));
                }
                //BOOL accepta inicialització TRUE, FALSE
            } else if (((Variable) symbolActual).getTipus().equals("BOOL")) {
                if ((symbol.getRegex().equals(types[3]) || symbol.getRegex().equals(types[4]))) {
                    System.out.println("OK");
                    System.out.println(((Variable) symbolActual).getTipus() + " == " + symbol.getRegex());
                } else {
                    ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"Operation not allowed","Semantic Error", "Bool type only allows bool values", symbol.getToken(), "Expected: BOOL"));
                }
            }
        //Comprova si el symbol és una variable
        } else if (symbolActual != null && symbol.getRegex().equals("NAME")) {
            //STRING accepta inicialització de INT_VALUE, FLOAT_VALUE, STRING_CONTENT
            if (((Variable) symbolActual).getTipus().equals("STRING")) {
                //Si la inicialització és un bool KO
                if (((Variable) symbol).getTipus().equals(types[5])) {
                    System.out.println("KO");
                    System.out.println(((Variable) symbolActual).getTipus() + " != " + ((Variable) symbol).getTipus());
                    ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"Operation not allowed","Semantic Error", "string type don't allow bool values", symbol.getToken(), "Expected: INT, FLOAT or string"));
                } else {
                    System.out.println("OK");
                    System.out.println(((Variable) symbolActual).getTipus() + " == " + ((Variable) symbol).getTipus());
                }
                //FLOAT accepta inicialització de INT_VALUE, FLOAT_VALUE
            } else if (((Variable) symbolActual).getTipus().equals("FLOAT")) {
                if (((Variable) symbol).getTipus().equals("FLOAT") || ((Variable) symbol).getTipus().equals("INT")) {
                    System.out.println("OK");
                    System.out.println(((Variable) symbolActual).getTipus() + " == " + ((Variable) symbol).getTipus());
                } else {
                    System.out.println("KO");
                    System.out.println(((Variable) symbolActual).getTipus() + " != " + ((Variable) symbol).getTipus());
                    ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"Operation not allowed","Semantic Error", "Float type don't allow bool or string values", symbol.getToken(), "Expected: INT or FLOAT"));
                }
                //INT accepta inicialització de INT_VALUE
            } else if (((Variable) symbolActual).getTipus().equals("INT")) {
                if (((Variable) symbol).getTipus().equals("INT")) {
                    System.out.println("OK");
                    System.out.println(((Variable) symbolActual).getTipus() + " == " + ((Variable) symbol).getTipus());
                } else {
                    System.out.println("KO");
                    System.out.println(((Variable) symbolActual).getTipus() + " != " + ((Variable) symbol).getTipus());
                    ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"Operation not allowed","Semantic Error", "Int type only allows int values", symbol.getToken(), "Expected: INT"));
                }
                //BOOL accepta inicialització TRUE, FALSE
            } else if (((Variable) symbolActual).getTipus().equals("BOOL")) {
                if (((Variable) symbol).getTipus().equals("BOOL")) {
                    System.out.println("OK");
                    System.out.println(((Variable) symbolActual).getTipus() + " == " + ((Variable) symbol).getTipus());
                } else {
                    System.out.println("KO");
                    System.out.println(((Variable) symbolActual).getTipus() + " != " + ((Variable) symbol).getTipus());
                    ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"Operation not allowed","Semantic Error", "Bool type only allows bool values", symbol.getToken(), "Expected: BOOL"));

                }
            }
        }
    }

    /**
     * Funció per recorrer el parse tree
     */
    private static void readParseTree(ParseTreeNode node, String prod) {
        //Mirem si es un node terminal o no
        if (!ParseTree.isTerminal(node)){
            //Recorrem left, mid i right
            for (int i = 0; i < 3; i++) {
                //Left
                if (i == 0) {
                    ParseTreeNode left = node.getLeft();
                    if (left != null) {
                        // System.out.println("Left " + left.getSymbol());
                        readParseTree(left,prod);
                    }
                    //Mid
                } else if (i == 1) {
                    ParseTreeNode mid = node.getMid();
                    if (mid != null) {
                        // System.out.println("Mid " + mid.getSymbol());
                        readParseTree(mid,prod);
                    }
                    //Right
                } else {
                    ParseTreeNode right = node.getRight();
                    //Recorrem la linkedlist en cas que sigui un BLOCK_STATEMENT
                    if (right != null) {
                        if (right.getSymbol().getRegex().equals("BLOCK_STATEMENT")) {
                            //Entrem al scope de la taula de simbols
                            SymbolTable.scopeIn();
                            //Mirem els BLOCK_STATEMENT_CONCAT
                            for (ParseTreeNode blockStatementConcat : right.getLinkedList()) {
                                SemanticAnalyzer.checkNode(blockStatementConcat.getLeft());
                            }
                            //Sortim del scope de la taula de simbols
                            SymbolTable.scopeOut();
                        } else {
                            // System.out.println("Right " + right.getSymbol());
                            readParseTree(right,prod);
                        }
                    }
                }
            }
        } else {
            //Si es terminal mirem quina producció l'ha generat
            if (prod.equals("VAR_STATEMENT") || prod.equals("ASSIGNMENT")) {
                SemanticAnalyzer.checkSymbol(node.getSymbol());
            } else if (prod.equals("IF_EXPRESSION") || prod.equals("WHILE_EXPRESSION")) {
                SemanticAnalyzer.checkConditionalBlock(node.getSymbol());
            }
        }
    }
}
