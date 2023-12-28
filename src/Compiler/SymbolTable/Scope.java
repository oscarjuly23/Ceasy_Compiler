package Compiler.SymbolTable;

import Compiler.Frontend.SyntaxAnalyzer;
import Errors.ErrorHandler;
import Errors.NodeError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe que ens permet gestionar els scopes
 */
public class Scope {
    public Scope father;
    public ArrayList<Scope> child;
    public int currentChild;
    public int depth;
    public String scopeName;

    public HashMap<String, Symbol> localScopeSymbols = new HashMap<String, Symbol>();

    /**
     * Contructor
     * @param depth Numero que ens indica la profunditat de l'escope actual
     */
    public Scope(int depth, String scopeName) {
        this.father = null;
        this.depth = depth;
        this.currentChild = 0;
        this.child = new ArrayList<Scope>();
        this.scopeName = scopeName;
    }

    /**
     * Funció que comprova que no es repeteixin noms i ens genera un error si no compleix
     * @param sym Symbol que volem
     */
    public void checkUniqueSymbolName(Symbol sym) {
        for (String local_name : this.localScopeSymbols.keySet()) {
            if (local_name.equals(sym.getToken())) {
                ErrorHandler.addError(new NodeError(sym.getLineNumber(), "Critical", "Name already exist", SyntaxAnalyzer.lastName, sym.getToken(), "" ));
            }
        }
    }

    /**
     * Funció que afegeix un symbol a Scope actual
     * @param sym symbol que volem afegir
     */
    public void addSymbol(Symbol sym) {
        //this.checkUniqueSymbolName(sym);
        this.localScopeSymbols.put(sym.getToken(), sym);
    }

    /**
     * Funció que ens busca un symbol en tots l'escope actual i en els superiors.
     * @param name token del nom del symbol
     * @return Symbol que busques o null si no l'ha trobat
     */
    public Symbol findSymbol(String name) {
        for (Map.Entry<String, Symbol> symbol_entry : this.localScopeSymbols.entrySet()) {
            if (name.equals(symbol_entry.getKey())) {
                return symbol_entry.getValue();
            }
        }
        return null;
    }

}
