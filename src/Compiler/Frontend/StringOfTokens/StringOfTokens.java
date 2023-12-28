package Compiler.Frontend.StringOfTokens;

import Compiler.SymbolTable.Symbol;
import Utils.FileManager;
import com.google.gson.Gson;

/**
 * Classe que gestiona l'estructura de l'estring of tokens
 */
public class StringOfTokens {

    private static final FileManager fileManager = new FileManager("file.cy.StringOfTokens");
    private static int line = 0;

    /**
     * Funció per afegir un nou symbol a l'estructura
     * @param symbol Symbol que es vol afegir
     */
    public static void add(Symbol symbol) {
        Gson gson = new Gson();
        String jsonInString = gson.toJson(symbol);
        fileManager.writeOnlyOneLine(jsonInString);
    }

    /**
     * Funció que et retorna els simbols un a un.
     * @return seguent symbol de la llista
     */
    public static Symbol next() {
        String symbolString = fileManager.readOnlyOneLine(line);
        line++;

        Gson gson = new Gson();
        return gson.fromJson(symbolString, Symbol.class);
    }
}
