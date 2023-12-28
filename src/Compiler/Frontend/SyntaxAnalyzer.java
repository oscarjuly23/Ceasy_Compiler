/**
 * @author Victor Vallés
 * @author Carles Torrubiano
 * @author Oscar Julian
 * @author Bernat Segura
 * @author Rafael Morera
 */

package Compiler.Frontend;

import Compiler.Frontend.ParseTree.ParseTree;
import Compiler.Frontend.ParseTree.ParseTreeNode;
import Compiler.Frontend.StringOfTokens.StringOfTokens;
import Compiler.SymbolTable.Function;
import Compiler.SymbolTable.Symbol;
import Compiler.SymbolTable.SymbolTable;
import Compiler.SymbolTable.Variable;
import Errors.ErrorHandler;
import Errors.NodeError;
import Utils.FileManager;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Classe encarregada de fer l'arbre de parseig i comprobar la sintaxi (gramàtica) del codi
 */
public class SyntaxAnalyzer {

    public static ParseTree parseTree; // Arbre del codi
    public static Symbol symbol;   // Símbol a analitzar
    public static String lastName = "root";
    private static boolean isMain = false;
    private static int lastFuncLine = -1;

    /**
     * Funció que comproba la gramàtica de tot el codi
     */
    public static void checkGrammar() {
        // Agafem el primer símbol de la línia que hem llegir del String Of Tokens (provinent de l'Anàlisi Lèxic)
        symbol = StringOfTokens.next();

        // Primerament mirarem que sigui correcte el EntryPoint del programa
        parseTree = new ParseTree(checkEntryPoint());
        FileManager.parseTreeToJson(); // Guardem el ParseTree en un fitxer JSON
        System.out.println("TEST");

    }

    /**
     * Funció que comproba la gramàtica del Entry Point del codi
     */
    private static ParseTreeNode checkEntryPoint() {
        // P ->  <primary_statement> <main>
        int funcLine = symbol.getLineNumber();

        // Crearem una llista que anirà acumulant els primary_statement concatenats.
        LinkedList<ParseTreeNode> parseTreeNodeArray = new LinkedList<>();

        // Anirem guardant-los en la llista fins a trobar un '}'.
        while (!isMain) {
            parseTreeNodeArray.add(primary_statement());
        }

        if (symbol != null) {
            ErrorHandler.addError(new NodeError(funcLine,"'Syntax Error'","'Unexpected symbol'", "All functions must be declared before Main", "", ""));
            ErrorHandler.getErrors();
        }

        // Si hem trobat un Assignment retornem el Node generat a l'esquerra.
        return new ParseTreeNode(new Symbol("", "ENTRY_POINT", funcLine), parseTreeNodeArray);
    }


    /**
     * Funció que comproba el símbol i passa al següent.
     */
    private static void match(String regex) {
        // Comprobarem que el símbol que trobem és l'esperat per la nostre gramàtica.
        if (regex.equals(symbol.getRegex())) {

            // Si trobem un '{' entrem en un nou context.
            if (regex.equals("CURLY_BRACKET_OPEN")) {
                SymbolTable.openScope(lastName);
            }

            // Si trobem un '}' sortim del context actual.
            if (regex.equals("CURLY_BRACKET_CLOSE")) {
                SymbolTable.closeScope();
            }

            // Si trobem una funcio guardem el numero de linea
            if (regex.equals("FUNC")) {
                lastFuncLine = symbol.getLineNumber();
            }

            // Passem a analitzar el següent símbol.
            symbol = StringOfTokens.next();
        }
    }

    /**
     * Funció que salta l'error si el símbol comprobar no és l'esperat.
     */
    private static void checkMatch(String regex) {
        if (!regex.equals(symbol.getRegex())){
            ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"'Syntax Error'","'Unexpected symbol'", "'Expected another symbol'", "'" + symbol.getToken() + "'", "'" + regex.toLowerCase() + "'"));
            ErrorHandler.getErrors();
        }
        match(regex);
    }

    /**
     * Funció que comproba si el símbol comprobar no l'esperat o no.
     */
    private static boolean checkSymbol(String regex) {
        if (!regex.equals(symbol.getRegex())) {
            return false;
        }
        match(regex);

        return true;
    }

    /**
     * Funció que comproba la gramàtica d'una funció (pot ser main).
     */
    private static ParseTreeNode readFunc() {
        Symbol funcName, funcType;
        // Comprobarem si cada símbol és l'esperat, si no tenim error farem el match.
        checkMatch("FUNC");
        funcName = symbol;
        if (funcName.getRegex().equals("MAIN")) {
            isMain = true;
        }
        checkMatch(funcName.getRegex());
        checkMatch("PARENTHESES_OPEN");
        ParseTreeNode node_Func_Declaration_Params = func_declaration_params();
        if (isMain && node_Func_Declaration_Params != null) {
            ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"'Syntax Error'","'Invalid syntax'", "Main can't contain parameters", "'" + symbol.getToken() + "'", ""));
            ErrorHandler.getErrors();
        }
        lastName = funcName.getToken();
        checkMatch("PARENTHESES_CLOSE");
        checkMatch("ARROW");
        funcType = symbol;
        ParseTreeNode node_Return_Declaracio_Func = return_declaracio_func();
        if (isMain && (!node_Return_Declaracio_Func.getSymbol().getRegex().equals("VOID"))) {
            ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"'Syntax Error'","'Invalid syntax'", "Main must be VOID return type", "'" + symbol.getToken() + "'", ""));
            ErrorHandler.getErrors();
        }
        //Node esquerra: Node amb Símbol Main || Node dreta: Node amb Símbol
        ParseTreeNode node = new ParseTreeNode(new Symbol("", "FUNC_PRIMA", symbol.getLineNumber()), new ParseTreeNode(new Symbol(funcName.getToken(), funcName.getRegex(), symbol.getLineNumber())), node_Func_Declaration_Params, new ParseTreeNode(new Symbol("", funcType.getRegex(), symbol.getLineNumber())));
        node.getLeft().setParent(node);
        node.getRight().setParent(node);

        //Afegim la funcio a la taula de simbols
        Function symTabFunc = new Function(funcName.getToken(), funcName.getRegex(), funcName.getLineNumber());
        if (node_Func_Declaration_Params != null) {
            symTabFunc.addParameter(new Variable(node_Func_Declaration_Params.getRight().getSymbol().getToken(), node_Func_Declaration_Params.getRight().getSymbol().getRegex(), node_Func_Declaration_Params.getRight().getSymbol().getLineNumber(), node_Func_Declaration_Params.getLeft().getSymbol().getRegex()));
        }

        if (node_Return_Declaracio_Func != null) {
            symTabFunc.addReturn(new Variable(node_Return_Declaracio_Func.getSymbol().getToken(), node_Return_Declaracio_Func.getSymbol().getRegex(), node_Return_Declaracio_Func.getSymbol().getLineNumber(), node_Return_Declaracio_Func.getSymbol().getRegex()));
        }

        SymbolTable.addSymbol(symTabFunc);
        return node;
    }

    /**
     * Funció que comproba la gramàtica de la possible concatenació de block_statement
     */
    private static ParseTreeNode block_statement() {
        // <block_statement> ::= <block_statement_concat> <block_statement> | ε

        // Crearem una llista que anirà acumulant els block_statement concatenats.
        LinkedList<ParseTreeNode> parseTreeNodeArray = new LinkedList<>();

        // Anirem guardant-los en la llista fins a trobar un '}'.
        while (!symbol.getRegex().equals("CURLY_BRACKET_CLOSE")) {
            parseTreeNodeArray.add(block_statement_concat());
        }

        // Si hem trobat un Assignment retornem el Node generat a l'esquerra.
        return new ParseTreeNode(new Symbol("", "BLOCK_STATEMENT", symbol.getLineNumber()), parseTreeNodeArray);
    }

    /**
     * Funció que comproba la gramàtica d'un block statement (blocs generals que ens podem trobar en el codi)
     */
    private static ParseTreeNode block_statement_concat() {
        // <block_statement_concat> ::= <if> | <while> | <for> | <func_callback> | <return_inside_function>| <assignment> | <var_statement> | <break> | ε

        // En el block_statement_concat hem de realitzar diferentes comprobacions per saber si el que ens ha arribat és un símbol esperat.
        // Segons quin símbol sigui l'esperat és faràn les comprobacions necessaries, i es retornarà aquesta expressió.

        ParseTreeNode node_Name = name();
        if (node_Name != null) {
            ParseTreeNode node_Assignment = assignment(node_Name);
            if (node_Assignment != null) {
                // Si hem trobat un Assignment retornem el Node generat a l'esquerra.
                ParseTreeNode node = new ParseTreeNode(new Symbol("", "BLOCK_STATEMENT_CONCAT", symbol.getLineNumber()), node_Assignment, null);
                node.getLeft().setParent(node);
                return node;
            }
            ParseTreeNode node_Func_Callback = func_callback(node_Name);
            if (node_Func_Callback != null) {
                //Si hem trobat un call a una funcio retornem el Node generat a l'esquerra
                ParseTreeNode node = new ParseTreeNode(new Symbol("", "BLOCK_STATEMENT_CONCAT", symbol.getLineNumber()), node_Func_Callback, null);
                node.getLeft().setParent(node);
                return node;
            }
        }

        ParseTreeNode node_Var_Statement = var_statement();
        if (node_Var_Statement != null) {
            // Si hem trobat una declaració de variable retornem el Node generat a l'esquerra.
            ParseTreeNode node = new ParseTreeNode(new Symbol("", "BLOCK_STATEMENT_CONCAT", symbol.getLineNumber()), node_Var_Statement, null);
            node.getLeft().setParent(node);
            return node;
        }

        ParseTreeNode node_if = if_expr();
        if (node_if != null) {
            // Si hem trobat una expressió condicional if retornem el Node generat a l'esquerra.
            ParseTreeNode node = new ParseTreeNode(new Symbol("", "BLOCK_STATEMENT_CONCAT", symbol.getLineNumber()), node_if, null);
            node.getLeft().setParent(node);
            return node;
        }

        ParseTreeNode node_while = while_expr();
        if (node_while != null) {
            // Si hem trobat una expressió de bucle while retornem el Node generat a l'esquerra.
            ParseTreeNode node = new ParseTreeNode(new Symbol("", "BLOCK_STATEMENT_CONCAT", symbol.getLineNumber()), node_while, null);
            node.getLeft().setParent(node);
            return node;
        }

        ParseTreeNode node_Return_Inside_Function = return_inside_function();
        if (node_Return_Inside_Function != null){
            //Si hem trobat un return retornem el Node generat a l'esquerra
            ParseTreeNode node = new ParseTreeNode(new Symbol("", "BLOCK_STATEMENT_CONCAT", symbol.getLineNumber()), node_Return_Inside_Function, null);
            node.getLeft().setParent(node);
            return node;
        }

        // Si no tenim cap block reconegut i no ha acabat el BlockStatment saltarà un error.
        if (!symbol.getRegex().equals("CURLY_BRACKET_CLOSE")) {
            ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"'Syntax Error'","'Invalid syntax'", "'Unrecognized symbol'", "'" + symbol.getToken() + "'", ""));
            ErrorHandler.getErrors();
        }

        // Si no hem trobat cap d'aquestes expressions retornarem un null, vol dir que ja hem concatenat tot el que hem trobat.
        return null;
    }

    /**
     * Funció que comproba la gramàtica de l'expressió d'un condicional if
     */
    private static ParseTreeNode if_expr() {
        // <if_expr> ::= ‘if’  <if_statement>

        if (!checkSymbol("IF")) {
            // Si no hem trobat l'expressió retornarem un null.
            return null;
        }

        ParseTreeNode node_If_Statement = if_statement();

        //Setejem els pares
        if (node_If_Statement.getSymbol().getRegex().equals("CONDITIONAL_BLOCK")){
            for (ParseTreeNode nodei : node_If_Statement.getRight().getLinkedList()) {
                nodei.setParent(node_If_Statement.getRight());
            }
        } else if (node_If_Statement.getSymbol().getRegex().equals("IF_STATEMENT")){
            for (ParseTreeNode nodei : node_If_Statement.getRight().getLeft().getRight().getLinkedList()) {
                nodei.setParent(node_If_Statement.getRight());
            }
        }
        node_If_Statement.getRight().setParent(node_If_Statement);

        // Si hem trobat una declaració condicional if retornem el Node generat a l'esquerra.
        ParseTreeNode node = new ParseTreeNode(new Symbol("", "IF_EXPRESSION", symbol.getLineNumber()), new ParseTreeNode(new Symbol("if", "IF", symbol.getLineNumber())), node_If_Statement);
        node.getLeft().setParent(node);
        node.getRight().setParent(node);
        return node;
    }

    /**
     * Funció que comproba la gramàtica de la declaració d'un condicional if
     */
    private static ParseTreeNode if_statement() {
        // <if_statement> ::= <conditional_block> <if_concat>

        ParseTreeNode node_Conditional_Block = conditional_block();

        ParseTreeNode node_If_Concat = if_concat();
        if (node_If_Concat == null) {
            // Si no hem trobat l'expressió retornarem un null.
            return node_Conditional_Block;
        }

        // Node esquerra: Node BlockCondicional || Node dret: Node Concatenació if (else, elif)
        ParseTreeNode node = new ParseTreeNode(new Symbol("", "IF_STATEMENT", symbol.getLineNumber()), node_Conditional_Block, node_If_Concat);
        node.getLeft().setParent(node);
        node.getRight().setParent(node);
        return node;
    }

    /**
     * Funció que comproba la gramàtica del block condicional
     */
    private static ParseTreeNode conditional_block() {
        // <conditional_block> ::=  ‘(‘<binary_cond_expression>’)’ ‘{‘ <block_statement> ‘}’

        checkMatch("PARENTHESES_OPEN");
        ParseTreeNode node_Binary_Cond_Expression = binary_cond_expression();
        checkMatch("PARENTHESES_CLOSE");
        checkMatch("CURLY_BRACKET_OPEN");
        ParseTreeNode node_Block_Statement = block_statement();
        checkMatch("CURLY_BRACKET_CLOSE");

        // Node esquerra: Node d'Exp. Binaria condicional || Node dret: Node BlockStatement
        ParseTreeNode node = new ParseTreeNode(new Symbol("", "CONDITIONAL_BLOCK", symbol.getLineNumber()), node_Binary_Cond_Expression, node_Block_Statement);
        node.getLeft().setParent(node);
        node.getRight().setParent(node);
        return node;
    }

    /**
     * Funció que comproba la gramàtica de l'expressió d'un bucle while
     */
    private static ParseTreeNode while_expr() {
        // <while_expr > ::= ‘while’ <conditional_block>

        if (!checkSymbol("WHILE")) {
            // Si no hem trobat l'expressió retornarem un null.
            return null;
        }
        ParseTreeNode node_Conditional_Block = conditional_block();

        //Setejem els pares
        for (ParseTreeNode nodei : node_Conditional_Block.getRight().getLinkedList()) {
            nodei.setParent(node_Conditional_Block.getRight());
        }
        node_Conditional_Block.getRight().setParent(node_Conditional_Block);

        // Node esquerra: Symbol while || Node dreta: block conditional
        ParseTreeNode node = new ParseTreeNode(new Symbol("", "WHILE_EXPRESSION", symbol.getLineNumber()), new ParseTreeNode(new Symbol("while", "WHILE", symbol.getLineNumber())), node_Conditional_Block);
        node.getLeft().setParent(node);
        node.getRight().setParent(node);
        return node;
    }

    /**
     * Funció que comproba la gramàtica d'una expressió binaria per condicions
     */
    private static ParseTreeNode binary_cond_expression() {
        // <binary_cond_expression> ::= <expression> <binary_expression_prime>

        ParseTreeNode node_Expression = expression();

        ParseTreeNode node_Binary_Expression_Prime = binary_expression_prime();
        if (node_Binary_Expression_Prime == null) {
            // Si no hem trobat l'expressió retornarem un null.
            return node_Expression;
        }

        // Node esquerra: Node Expressió || Node dret: Node Expressio Binaria Prima
        ParseTreeNode node = new ParseTreeNode(new Symbol("", "BINARY_COND_EXPRESSION", symbol.getLineNumber()), node_Expression, node_Binary_Expression_Prime);
        node.getLeft().setParent(node);
        node.getRight().setParent(node);
        return node;
    }

    /**
     * Funció que comproba la gramàtica d'una expressió
     */
    private static ParseTreeNode expression() {
        //<expression> ::= <id> <op> <id>

        ParseTreeNode node_Id = id();
        if (node_Id == null){
            ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"'Syntax Error'","'Invalid syntax'", "'Expected ID'", "'" + symbol.getToken() + "'", ""));
            ErrorHandler.getErrors();
        }

        String opsMatch = checkOps();
        if (opsMatch.equals("")){
            ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"'Syntax Error'","'Invalid syntax'", "'Expected operator'", "'" + symbol.getToken() + "'", "{ '<', '<=', '>', '>=', '==', '!=' }"));
            ErrorHandler.getErrors();
        }

        ParseTreeNode node_Id2 = id();
        if (node_Id2 == null){
            ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"'Syntax Error'","'Invalid syntax'", "'Expected ID'", "'" + symbol.getToken() + "'", ""));
            ErrorHandler.getErrors();
        }

        // Node esquerra: Node ID || Node dret: Node ID2 || Node mid: OP
        ParseTreeNode node = new ParseTreeNode(new Symbol("", "EXPRESSION", symbol.getLineNumber()), node_Id, new ParseTreeNode(new Symbol("", opsMatch)), node_Id2);
        node.getLeft().setParent(node);
        node.getMid().setParent(node);
        node.getRight().setParent(node);
        return node;
    }

    /**
     * Funció que comproba l'operador d'una expressió
     */
    private static String checkOps() {
        String[] ops = {"LOWER_THAN", "LOWER_EQUAL_THAN", "BIGGER_THAN", "BIGGER_EQUAL_THAN", "EQUAL_THAN", "DIFFERENT_THAN"};
        String opsMatch = "";

        // Comprovem que el operador que tenim és un dels esperats
        if (Arrays.stream(ops).anyMatch(symbol.getRegex()::equals)) {
            for (String op : ops) {
                if (op.equals(symbol.getRegex())) {
                    opsMatch = op;
                    break;
                }
            }
            match(opsMatch);
        } else {
            return "";
        }

        // Retornem l'operador trobat
        return opsMatch;
    }

    /**
     * Funció que comproba la gramàtica de la concatenació d'expressions
     */
    private static ParseTreeNode binary_expression_prime() {
        //<binary_expression_prime> ::=  <op_expr> <binary_cond_expression> | ε
        String op = "";

        if (symbol.getRegex().equals("AND")) {
            op = symbol.getRegex();
            match("AND");
        } else if (symbol.getRegex().equals("OR")) {
            op = symbol.getRegex();
            match("OR");
        } else {
            // Si no hem trobat l'expressió retornarem un null.
            return null;
        }

        ParseTreeNode node_Binary_Cond_Expression = binary_cond_expression();

        // Node esquerra: Node OP || Node dret: Node Expressió Binaria
        ParseTreeNode node = new ParseTreeNode(new Symbol("", "BINARY_EXPRESSION_PRIME", symbol.getLineNumber()), new ParseTreeNode(new Symbol("", op)), node_Binary_Cond_Expression);
        node.getLeft().setParent(node);
        node.getRight().setParent(node);
        return node;
    }

    /**
     * Funció que comproba la gramàtica de la concatenació de elif i else
     */
    private static ParseTreeNode if_concat() {
        // <if-concat> ::=  <elif_expr> | <else_expr> | ε

        ParseTreeNode node_Else_Expr = else_expr();
        if (node_Else_Expr != null) {
            // Si hem trobat una expressió condicional else retornem el Node generat a l'esquerra.
            ParseTreeNode node = new ParseTreeNode(new Symbol("", "IF_CONCAT", symbol.getLineNumber()), node_Else_Expr, null);
            node.getLeft().setParent(node);
            return node;
        }

        // Si no hem trobat l'expressió retornarem un null.
        return null;
    }

    /**
     * Funció que comproba la gramàtica de l'expressió d'un else
     */
    private static ParseTreeNode else_expr() {
        //<else_expr> ::= ‘else’ ‘{‘< block_statement >’}’

        if (!checkSymbol("ELSE")) {
            return null;
        }

        checkMatch("CURLY_BRACKET_OPEN");
        ParseTreeNode node_Block_Statement = block_statement();
        checkMatch("CURLY_BRACKET_CLOSE");

        // Node esquerra: Node d'Exp. else || Node dret: Node BlockStatement
        ParseTreeNode node = new ParseTreeNode(new Symbol("", "ELSE_EXPRESSION", symbol.getLineNumber()), new ParseTreeNode(new Symbol("else", "ELSE", symbol.getLineNumber())), node_Block_Statement);

        node.getLeft().setParent(node);
        node.getRight().setParent(node);
        return node;
    }

    /**
     * Funció que comproba la gramàtica de la inicialització d'una variable
     */
    private static ParseTreeNode var_init_statement() {
        // <var_init_statement> ::= ‘=’ <assignment_value> | ε

        if (!checkSymbol("EQUAL")) {
            return null;
        }
        ParseTreeNode node_Assignment_Value = assignment_value();
        // Si hem trobat una inicialització en una variable retornem el Node generat a la dreta.
        ParseTreeNode node = new ParseTreeNode(new Symbol("", "VAR_INIT_STATEMENT", symbol.getLineNumber()), null, new ParseTreeNode(new Symbol("=", "EQUAL", symbol.getLineNumber())), node_Assignment_Value);
        node.getMid().setParent(node);
        node.getRight().setParent(node);
        return node;
    }

    /**
     * Funció que comproba la definició del tipus i nom d'una variable
     */
    private static ParseTreeNode type_var_name() {
        // <type_var_name> ::= <type> <name>

        ParseTreeNode node_Type = type();
        if (node_Type == null) {
            return null;
        }
        ParseTreeNode node_Name = name();
        if (node_Name == null) {
            ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"'Syntax Error'","'Invalid syntax'", "'Missing name'", "'" + symbol.getToken() + "'", ""));
            ErrorHandler.getErrors();
        }

        // Si estem en la declaracio d'una funcio
        if (node_Name.getSymbol().getLineNumber() == lastFuncLine) {
            // En aquest cas deixarem la variable en una llista per quan s'obri l'escope de la funcio
            SymbolTable.addParameter(new Variable(node_Name.getSymbol().getToken(), node_Name.getSymbol().getRegex(), node_Name.getSymbol().getLineNumber(), node_Type.getSymbol().getRegex()));
        } else {
            // Afegim a la taula de símbols la variable definida en l'escope actual.
            SymbolTable.addSymbol(new Variable(node_Name.getSymbol().getToken(), node_Name.getSymbol().getRegex(), node_Name.getSymbol().getLineNumber(), node_Type.getSymbol().getRegex()));
        }

        // Node esquerra: Node Tipus || Node dret: Node Name
        ParseTreeNode node = new ParseTreeNode(new Symbol("", "TYPE_VAR_NAME", symbol.getLineNumber()), node_Type, node_Name);
        node.getLeft().setParent(node);
        node.getRight().setParent(node);
        return node;
    }

    /**
     * Funció que comproba la gramàtica del tipus d'una variable
     */
    private static ParseTreeNode type() {
        // <type> ::== ‘int’ | ‘float’ | ‘boolean’ | ‘string’ | ‘int64’ | ‘float64’

        String[] types = {"FLOAT", "INT", "BOOL", "INT_64", "FLOAT_64", "STRING"};
        // Mirarem que el tipus de la variable declarat és existent
        if (Arrays.stream(types).anyMatch(symbol.getRegex()::equals)) {
            String typeMatch = "";
            for (String type : types) {
                if (type.equals(symbol.getRegex())) {
                    typeMatch = type;
                    break;
                }
            }
            match(typeMatch);

            // Si hem trobat el tipus retornem el símbol.
            return new ParseTreeNode(new Symbol("", typeMatch, symbol.getLineNumber()));
        }

        return null;
    }

    /**
     * Funció que comproba la gramàtica del nom d'una variable
     */
    private static ParseTreeNode name() {
        // <name> ::= a-zA-Z0-9_ //Format per lletres, números i/o ‘_’

        // Mirarem que el nom que declarem és correcte
        if (symbol.getRegex().equals("NAME")) {
            String varName = symbol.getToken();
            match("NAME");

            // Si hem trobat el nom retornem el símbol.
            return new ParseTreeNode(new Symbol(varName, "NAME", symbol.getLineNumber()));
        }

        return null;
    }

    /**
     * Funció que comproba la gramàtica del Statment de concatenació en la inicialització d'una variable
     */
    private static ParseTreeNode assignment_value() {
        // <assignment_value> ::=  <func_callback> | <operacio>

        // ParseTreeNode node_Func_Callback = func_callback();

        return operacio();
    }

    /**
     * Funció que comproba la gramàtica de'un ID final
     */
    private static ParseTreeNode id() {
        // <id> ::= Integer (0..9) || Float (0..9 + ‘.’ + 0..9) || String (“a-zA-Z0-9_”) || bool (true/false) || Operació (Ex. 2+3) || Funció (no void) || <name>
        String[] values = {"FLOAT_VALUE", "INT_VALUE", "TRUE", "FALSE", "STRING_CONTENT", "NAME"};

        // Mirarem que el id que analitzem és correcte
        if (Arrays.stream(values).anyMatch(symbol.getRegex()::equals)) {
            String nameMatch = "";
            for (String value : values) {
                if (value.equals(symbol.getRegex())) {
                    nameMatch = value;
                    // Si es un nom actualitzem el lastName
                    if (symbol.getRegex().equals("NAME")) {
                        lastName = symbol.getToken();
                    }
                    break;
                }
            }
            String value = symbol.getToken();
            match(nameMatch);
            if (nameMatch.equals("NAME") && symbol.getRegex().equals("PARENTHESES_OPEN")) {
                checkMatch("PARENTHESES_OPEN");
                ParseTreeNode node_Func_Callback_Params = funcCallBackParams();
                checkMatch("PARENTHESES_CLOSE");
                ParseTreeNode node = new ParseTreeNode(new Symbol(value, "FUNC_ID", symbol.getLineNumber()), null, null);
                return new ParseTreeNode(new Symbol(value, "CALL_FUNC", symbol.getLineNumber()), node, node_Func_Callback_Params);
            }

            // Si hem trobat el id retornem el símbol.
            return new ParseTreeNode(new Symbol(value, nameMatch, symbol.getLineNumber()));
        }

        return null;
    }

    /**
     * Funció que comproba la gramàtica de assignació d'una variable
     */
    private static ParseTreeNode assignment(ParseTreeNode node_Name) {
        // <assignment> ::= <name> <assignment_var_arr> '=' <assignment_value>

        //assignment_var_arr();
        if (!checkSymbol("EQUAL")) {
            return null;
        }

        ParseTreeNode node_Assignment_Value = assignment_value();

        // Node esquerra: Node Name || Node Dret: Node AssValue || Node Mid: '='
        ParseTreeNode node = new ParseTreeNode(new Symbol("", "ASSIGNMENT", symbol.getLineNumber()), node_Name, new ParseTreeNode(new Symbol("=", "EQUAL", symbol.getLineNumber())), node_Assignment_Value);
        node.getLeft().setParent(node);
        node.getMid().setParent(node);
        node.getRight().setParent(node);
        return node;
    }

    /**
     * Funció que comproba la gramàtica de la assignació de una variable del tipus array
     */
    private static ParseTreeNode assignment_var_arr() {
        // <assignment_var_arr> ::=  ‘[’ <id> ‘]’ | ε

        // Si no hem trobat l'expressió retornarem un null.
        return null;
    }

    /**
     * Funció que comproba la gramàtica de declaració d'una variable
     */
    private static ParseTreeNode var_statement() {
        // <var_statement> ::=  <type_var_name> <var_statement_concat>

        ParseTreeNode node_Var_Name = type_var_name();
        if (node_Var_Name == null) {
            return null;
        }
        // Si hem trobat una una var i name comprobarem si té cooncatenació.
        ParseTreeNode node_Var_Statement_Concat = var_statement_concat();

        // Node esquerra: VarName || Node dreta: Concatenació declaració variable
        ParseTreeNode node = new ParseTreeNode(new Symbol("", "VAR_STATEMENT", symbol.getLineNumber()), node_Var_Name, node_Var_Statement_Concat);
        node.getLeft().setParent(node);
        if (node.getRight() != null) node.getRight().setParent(node);
        return node;
    }

    /**
     * Funció que comproba la gramàtica d'una concatenació en la declaració d'una variable
     */
    private static ParseTreeNode var_statement_concat() {
        // <var_statement_concat> ::= <var_init_statement> |  <assignment_var_arr> | ε

        return var_init_statement();
    }


    /********************************************************
     ******************** OPERACIO **************************
     ********************************************************/

    /**
     * Funció que comproba la gramàtica d'una operació
     */
    private static ParseTreeNode operacio() {
        //<operacio> ::= <operacio_basica> | <operacions_simplificades> | <operacions_increments>

        ParseTreeNode node_Operacio_basica = operacio_basica();
        if (node_Operacio_basica != null) {
            // Retornem el node a l'esquerra.
            ParseTreeNode node = new ParseTreeNode(new Symbol("", "OPERACIO", symbol.getLineNumber()), node_Operacio_basica, null);
            node.getLeft().setParent(node);
            return node;
        }

        // Si no hem trobat l'expressió retornarem un null.
        return null;
    }

    /**
     * Funció que comproba la gramàtica d'una operació bàsica amb concatenació d'operacions de primer nivell (suma i resta)
     */
    private static ParseTreeNode operacio_basica() {
        //<operacio_basica> ::= <sum_res> <sum_res_prima>

        ParseTreeNode node_suma_res = suma_res();
        ParseTreeNode node_suma_res_prima = suma_res_prima(node_suma_res);
        if (node_suma_res_prima == null || ParseTree.isTerminal(node_suma_res_prima) || node_suma_res_prima.getSymbol().getRegex().equals("CALL_FUNC")) {
            // Si detectem que és terminal o no trobem concatenació retornem la forma no prima (sense concatenació).
            return node_suma_res;
        }

        // Retornem el node amb la operació 'concatenada' fins ara.
        ParseTreeNode node = new ParseTreeNode(new Symbol("", "OPERACIO_BASICA", symbol.getLineNumber()), node_suma_res_prima.getLeft(), node_suma_res_prima.getMid(), node_suma_res_prima.getRight());
        node.getLeft().setParent(node);
        node.getMid().setParent(node);
        node.getRight().setParent(node);
        return node;
    }

    /**
     * Funció que comproba la gramàtica d'una concatenació d'operacions de segon nivell (multiplicació i divisió)
     */
    private static ParseTreeNode suma_res() {
        //<sum_res> ::= <mul_div> <mul_div_prima>

        ParseTreeNode mul_div = mul_div();
        ParseTreeNode mul_div_prima = mul_div_prima(mul_div);
        if (mul_div_prima == null || ParseTree.isTerminal(mul_div_prima) || mul_div_prima.getSymbol().getRegex().equals("CALL_FUNC")) {
            // Si detectem que és terminal o no trobem concatenació retornem la forma no prima (sense concatenació).
            return mul_div;
        }

        // Retornem el node amb la operació 'concatenada' fins ara.
        ParseTreeNode node = new ParseTreeNode(new Symbol("", mul_div_prima.getSymbol().getRegex(), symbol.getLineNumber()), mul_div_prima.getLeft(), mul_div_prima.getMid(), mul_div_prima.getRight());
        node.getLeft().setParent(node);
        node.getMid().setParent(node);
        node.getRight().setParent(node);
        return node;
    }

    /**
     * Funció que comproba la gramàtica del primer nivell d'operacions amb concatenació (+ i -)
     */
    private static ParseTreeNode suma_res_prima(ParseTreeNode parseTreeNode) {
        //<sum_res_prima > ::= <sym_op_low> <sum_res> <sum_res_prima> | ε

        ParseTreeNode aux = null;

        // Comprobem si trobem els signes.
        if (symbol.getRegex().equals("ADD")) {
            match("ADD");
            ParseTreeNode node_suma_res = suma_res();

            aux = new ParseTreeNode(new Symbol("", "SUMA_RES_PRIMA", symbol.getLineNumber()), parseTreeNode, new ParseTreeNode(new Symbol("+", "ADD", symbol.getLineNumber())), node_suma_res);
            aux.getLeft().setParent(aux);
            aux.getMid().setParent(aux);
            aux.getRight().setParent(aux);

        } else if (symbol.getRegex().equals("SUBTRACT")) {
            match("SUBTRACT");
            ParseTreeNode node_suma_res = suma_res();

            // Construim arbre auxiliar amb l'arbre construit fins al moment, afegint l'expressió actual.
            aux = new ParseTreeNode(new Symbol("", "SUMA_RES_PRIMA", symbol.getLineNumber()), parseTreeNode, new ParseTreeNode(new Symbol("-", "SUBSTRACT", symbol.getLineNumber())), node_suma_res);
            aux.getLeft().setParent(aux);
            aux.getMid().setParent(aux);
            aux.getRight().setParent(aux);
        } else {
            // Si no trobem cap signe retornem l'arbre obtingut fins aquest moment.
            return parseTreeNode;
        }

        // Cridem a la funció recursivament per si hem de afegir més operacions.
        parseTreeNode = suma_res_prima(aux);

        // Finalment retornem tot l'arbre construit fins al moment.
        return parseTreeNode;
    }

    /**
     * Funció que comproba la gramàtica d'una operació bàsica amb concatenació d'operacions de tercer nivell (modul)
     */
    private static ParseTreeNode mul_div() {
        //<mul_div> ::= <modul> <modul_prima>

        ParseTreeNode modul = modul();
        ParseTreeNode modul_prima = modul_prima(modul);
        if (modul_prima == null || ParseTree.isTerminal(modul_prima) || modul_prima.getSymbol().getRegex().equals("CALL_FUNC")) {
            // Si detectem que és terminal o no trobem concatenació retornem la forma no prima (sense concatenació).
            return modul;
        }

        // Retornem el node amb la operació 'concatenada' fins ara.
        ParseTreeNode node = new ParseTreeNode(new Symbol("", modul_prima.getSymbol().getRegex(), symbol.getLineNumber()), modul_prima.getLeft(), modul_prima.getMid(), modul_prima.getRight());
        node.getLeft().setParent(node);
        node.getMid().setParent(node);
        node.getRight().setParent(node);
        return node;
    }

    /**
     * Funció que comproba la gramàtica del segon nivell d'operacions amb concatenació (/ i *)
     */
    private static ParseTreeNode mul_div_prima(ParseTreeNode parseTreeNode) {
        //<mul_div_prima > ::= <sym_op_high> <mul_div> <mul_div_prima> | ε

        ParseTreeNode aux = null;
        // Comprobem si trobem els signes.
        if (symbol.getRegex().equals("MULTIPLICATION")) {
            match("MULTIPLICATION");
            ParseTreeNode node_mul_div = mul_div();

            // Construim arbre auxiliar amb l'arbre construit fins al moment, afegint l'expressió actual.
            aux = new ParseTreeNode(new Symbol("", "MUL_DIV_PRIMA", symbol.getLineNumber()), parseTreeNode, new ParseTreeNode(new Symbol("*", "MULTIPLICATION", symbol.getLineNumber())), node_mul_div);
            aux.getLeft().setParent(aux);
            aux.getMid().setParent(aux);
            aux.getRight().setParent(aux);

        } else if (symbol.getRegex().equals("DIVISION")) {
            match("DIVISION");
            ParseTreeNode node_mul_div = mul_div();

            // Construim arbre auxiliar amb l'arbre construit fins al moment, afegint l'expressió actual.
            aux = new ParseTreeNode(new Symbol("", "MUL_DIV_PRIMA", symbol.getLineNumber()), parseTreeNode, new ParseTreeNode(new Symbol("/", "DIVISION", symbol.getLineNumber())), node_mul_div);
            aux.getLeft().setParent(aux);
            aux.getMid().setParent(aux);
            aux.getRight().setParent(aux);

        } else {
            // Si no trobem cap signe retornem l'arbre obtingut fins aquest moment.
            return parseTreeNode;
        }

        // Cridem a la funció recursivament per si hem de afegir més operacions.
        parseTreeNode = mul_div_prima(aux);

        // Finalment retornem tot l'arbre construit fins al moment.
        return parseTreeNode;
    }

    /**
     * Funció que comproba la gramàtica en l'últim nivell d'operacions (id)
     */
    private static ParseTreeNode modul() {
        //<modul> ::= ‘(‘ <operacio_basica>’)’  | <id>
        ParseTreeNode nodeID = id();
        if (nodeID == null){
            ErrorHandler.addError(new NodeError(symbol.getLineNumber(),"'Syntax Error'","'Invalid syntax'", "'Expected ID'", "'" + symbol.getToken() + "'", ""));
            ErrorHandler.getErrors();
        }
        return nodeID;
    }

    /**
     * Funció que comproba la gramàtica del tercer nivell d'operacions amb concatenació (%)
     */
    private static ParseTreeNode modul_prima(ParseTreeNode parseTreeNode) {
        // <modul_prima> ::= ‘%’ <modul> <modul_prima> | ε

        ParseTreeNode aux = null;
        // Comprobem si trobem el signe.
        if (symbol.getRegex().equals("PERCENTAGE")) {
            match("PERCENTAGE");
            ParseTreeNode node_modul = modul();

            // Construim arbre auxiliar amb l'arbre construit fins al moment, afegint l'expressió actual.
            aux = new ParseTreeNode(new Symbol("", "MODUL_PRIMA", symbol.getLineNumber()), parseTreeNode, new ParseTreeNode(new Symbol("%", "PERCENTAGE", symbol.getLineNumber())), node_modul);
            aux.getLeft().setParent(aux);
            aux.getMid().setParent(aux);
            aux.getRight().setParent(aux);

        } else {
            // Si no trobem cap signe retornem l'arbre obtingut fins aquest moment.
            return parseTreeNode;
        }

        // Cridem a la funció recursivament per si hem de afegir més operacions.
        parseTreeNode = modul_prima(aux);

        // Finalment retornem tot l'arbre construit fins al moment.
        return parseTreeNode;
    }

    /********************************************************
     ******************** FUNCTIONS **************************
     ********************************************************/

    /**
     * Funció que comproba la gramàtica d'un Callback
     */

    private static ParseTreeNode func_callback(ParseTreeNode node_Name) {
        // <func_callback> ::=  <name> ‘(‘ <func_callback_params> ‘)’

        int funcLine = symbol.getLineNumber();

        if (!checkSymbol("PARENTHESES_OPEN")) {
            return null;
        }
        ParseTreeNode node_Func_Callback_Params = funcCallBackParams();
        match("PARENTHESES_CLOSE");

        //Node esquerra: Node Call name Function || Node dreta: Node Call Params in Function
        ParseTreeNode node = new ParseTreeNode(new Symbol("", "CALL_FUNC", funcLine), node_Name, node_Func_Callback_Params);
        node.getLeft().setParent(node);
        if ( node_Func_Callback_Params != null) {
            node.getRight().setParent(node);
        }
        return node;
    }

    private static ParseTreeNode funcCallBackParams(){
        //<func_callback_params> ::=  <id> <func_callback_params_concat>  | ε

        // Comprobem si tenim un ')' vol dir que no tindrem paràmetres
        if (symbol.getRegex().equals("PARENTHESES_CLOSE")){

            return null;
        }

        return id();
    }

    /**
     * Funció que comproba la gramàtica de la concatenació de primary_statement
     */
    private static ParseTreeNode primary_statement() {
        // <primary_satatement>  ::= <primary_statement-concat> <primary_statement> | ε

        return primary_statement_concat();
    }

    /**
     * Funció que comproba la gramàtica del Primary Statement
     */
    private static ParseTreeNode primary_statement_concat() {
        // <primary_statement-concat> ::= <var_statement> | <func_statement> | <var_init_statement> | <operacio> | ε

        return func_statement();
    }

    /**
     * Funció que comproba la gramàtica d'una funció
     */
    private static ParseTreeNode func_statement(){
        //<func_statement> ::= ‘func’ <name> ‘(‘ <func_declaration_params> ‘)’ ‘->’ <return_declaracio_func> ‘{‘ <block_statement> ‘}’

        if (symbol == null) {

            return null;
        }

        int funcLine = symbol.getLineNumber();

        ParseTreeNode node_ReadFunc = readFunc();
        checkMatch("CURLY_BRACKET_OPEN");
        ParseTreeNode node_Block_Statement = block_statement();
        checkMatch("CURLY_BRACKET_CLOSE");

        //Node esquerra: Node readFunction || Node dreta: Node BLOCK_STATEMENT
        ParseTreeNode node = new ParseTreeNode(new Symbol("", "FUNC", funcLine), node_ReadFunc, node_Block_Statement);
        node.getLeft().setParent(node);

        for (ParseTreeNode nodei : node.getRight().getLinkedList()) {
            nodei.setParent(node.getRight());
        }
        node.getRight().setParent(node);

        return node;
    }

    /**
     * Funció que comproba la gramàtica dels paràmetres en la declaració d'una funció
     */
    private static ParseTreeNode func_declaration_params(){
        // <func_declaration_params> ::= <type_var_name> <func_declaration_params_concat> | ε

        return type_var_name();
    }

    /**
     * Funció que comproba la gramàtica del return en la declaració d'una funció
     */
    private static ParseTreeNode return_declaracio_func(){
        // <return_declaracio_func> ::= <type> <return_declaracio_func_concat> | ‘void’

        ParseTreeNode node_Type = type();

        if (node_Type == null) {
            // Si no trobem cap tipus, comprobarem si és Void
            checkMatch("VOID");

            return new ParseTreeNode(new Symbol("", "VOID", symbol.getLineNumber()));
        }

        return node_Type;
    }

    /**
     * Funció que comproba la gramàtica del return d'una funció
     */
    private static ParseTreeNode return_inside_function(){
        //<return_inside_function> ::= ‘return’  <return_var>
        if (!checkSymbol("RETURN")) {
            // Si no hem trobat el símbol retornarem un null.
            return null;
        }
        ParseTreeNode node_Return_Var = return_var();

        ParseTreeNode node = new ParseTreeNode(new Symbol("", "RETURN_INSIDE_FUNCTION", symbol.getLineNumber()), new ParseTreeNode(new Symbol("","RETURN")), node_Return_Var);
        node.getLeft().setParent(node);

        return node;
    }

    /**
     * Funció que comproba la gramàtica del valor del return d'una funció
     */
    private static ParseTreeNode return_var(){
        //<return_var> ::= <id> <return_var_concat> | ε

        return id();
    }
}