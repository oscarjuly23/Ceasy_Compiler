/**
 * @author Victor Vallés
 * @author Carles Torrubiano
 * @author Oscar Julian
 * @author Bernat Segura
 * @author Rafael Morera
 */

package Compiler.SymbolTable;

import Compiler.Frontend.SyntaxAnalyzer;
import Errors.ErrorHandler;
import Errors.NodeError;
import java.util.ArrayList;

/**
 * Classe que permet treballar amb les multiples funcionalitats de la taula de símbols.
 */
public class SymbolTable {
    private static int currentDepth = 0;
    public  static Scope topScope = new Scope(currentDepth, "global");
    private static ArrayList<Symbol> funcParams = new ArrayList<>();
    public  static ArrayList<Symbol> allVars = new ArrayList<>();

    /**
     * Funció que ens crea un nou scope
     */
    public static void openScope(String scopeName) {
        // Ceem el nou scope
        Scope newScope = new Scope(++currentDepth, scopeName);
        // Fem que el nou scope pugui anar cap a l'escope pare
        newScope.father = topScope;
        // Si tenim pare afegim el nou scope com a fill
        if (topScope != null){
            // Assignem el nou scope com a fill de l'escope actual
            topScope.child.add(newScope);
        }
        // Ens movem al nou scope
        topScope = newScope;

        // Si es tracta d'una funció afegim els parametres
        funcParams.forEach(SymbolTable::addSymbol);
        funcParams.clear();
    }

    /**
     * Funció que surt de scope actual
     */
    public static void closeScope() {
        topScope = topScope.father;
        currentDepth--;
    }

    /**
     * Funció que ens permet navegar a l'escope fill de l'actual
     * @return int: 1 => ens hem mogut a l'escope fill | 0 => no existeixen mes fills (seguim a l'escope actual)
     */
    public static int scopeIn() {
        // Comprovem si tenim algun fill
        if(topScope.child.isEmpty()) {
            return 0;
        }

        // Comprovem que no haguem comprovat ja tots els fills
        if (topScope.currentChild == topScope.child.size()) {
            return 0;
        }

        // Si arribem aqui es que tenim un fill no explorat i ens hi movem.
        topScope = topScope.child.get(topScope.currentChild++);
        return 1;
    }

    /**
     * Funció que ens permet navegar a l'escope pare de l'actual
     * @return int: 1 => ens hem mogut a l'escope pare | 0 => no tenim pare (scope global)
     */
    public static int scopeOut() {
        // Comprovem si tenim un pare
        if (topScope.father == null) {
            return 0;
        }

        // Si arribem aqui tenim un pare i ens hi movem
        topScope = topScope.father;
        return 1;
    }

    /**
     * Funció que ens permet afegir un symbol a l'escope actual
     * @param symbol simbol que volem afegir
     */
    public static void addSymbol(Symbol symbol) {
        //Lafegirem si el nom encara no existeix
        if (findSymbol(symbol.getToken()) == null) {
            topScope.addSymbol(symbol);
        } else {
            ErrorHandler.addError(new NodeError(symbol.getLineNumber(), "Semantic", "Name already exist", SyntaxAnalyzer.lastName, symbol.getToken(), "" ));
            ErrorHandler.getErrors();
        }
    }

    /**
     * Funció que ens permet afegir un parametre a la funcio que s'esta creant
     * @param symbol simbol que volem afegir
     */
    public static void addParameter(Symbol symbol) {
        funcParams.add(symbol);
    }

    /**
     * Funció que ens permet buscar un token en l'escope actual
     * @param name token que conte el nom del simbol que volem buscar
     * @return Symbol que hem trobat o null si no l'hem trobat
     */
    public static Symbol findSymbol(String name) {
        Scope scope = topScope;
        Symbol symbol;
        while (scope != null) {
            symbol = scope.findSymbol(name);
            if (symbol != null) {
                return symbol;
            }
            scope = scope.father;
        }

        //ErrorHandler.addError(new NodeError(0, "Undeclared name", "error", "context", "text user", "prediction" ));
        return null;
    }

    /**
     * Retorna la profunditat actual de la taula de simbols
     * @return int que representa la profunditat
     */
    public int getCurrentDepth() {
        return currentDepth;
    }

    /**
     * Recorrem tot l'arbre resetejant els valors per poder tornarlo a recorrer
     */
    public static void resetVisit(Scope sc) {
        sc.currentChild = 0;
        sc.child.forEach(SymbolTable::resetVisit);
        allVars.clear();
    }

    /**
     * Funcio que
     */
    public static void getAllVars (Scope scope) {
        scope.localScopeSymbols.forEach((key, value) -> allVars.add(value));
        scope.child.forEach(SymbolTable::getAllVars);
    }
}
