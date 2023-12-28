/**
 * @author Victor Vallés
 * @author Carles Torrubiano
 * @author Oscar Julian
 * @author Bernat Segura
 * @author Rafael Morera
 */

package Compiler.Frontend;

import Compiler.Frontend.StringOfTokens.StringOfTokens;
import Compiler.SymbolTable.Symbol;
import Errors.ErrorHandler;
import Errors.NodeError;
import Utils.FileManager;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Classe encarregada de crear tokens i crear les noves entrades a la taula de simbols
 */
public class LexicalAnalyzer {
    private final static String fileReadParam = "file.cy.preprocessor";
    private final static String fileWriteParam = "file.cy.StringOfTokens";

    /**
     * Funcio que genera un fitxer que conte tots els tokens i la taula de simbols amb la informació disponible fins al moment
     */
    public static void buildTokens() {
        int numLine = 1;
        //Creem el file manager
        FileManager fileManager = new FileManager(fileReadParam, fileWriteParam);

        //Guardem tots els tokens a la taula de hash per cada linea
        String line;
        while ((line = fileManager.readLine()) != null) {
            if (line.length() != 0) {
                saveLine(line, numLine);
            }
            numLine++;
        }
        //Tanquem el file manager
        fileManager.close();
    }

    /**
     * Funcio que guarda totes les linees per tokens amb tota la informacio a la taula de simbols i en el fitxer.
     * @param line linea actual
     */
    private static void saveLine(String line, int numLine) {
        String[] tokens = line.trim().split(" ");
        //Recorrem tots els tokens d'una linea
        for (String token : tokens) {
            //Comprovem el token amb el regex
            String tokenSymbol = checkRegex(token);
            //Comprovem si el token llegit correspon a un symbol existent sino l'afegim al error handler
            if (tokenSymbol.equals("UNKNOWN_REGEX")) {
                ErrorHandler.addError(new NodeError(numLine,"UNKNOWN_REGEX","Lexical Error", line, token, ""));
            }
            //S'afegeix un nou symbol a la linea de la taula string of tokens
            StringOfTokens.add(new Symbol(token, tokenSymbol, numLine));
        }
    }

    /**
     * Funció que comprova quin quin regex correspon al token donat
     * @param token token a comprovar
     * @return String que conte el valor regex del token donat
     */
    private static String checkRegex (String token)  {
        Path path = FileSystems.getDefault().getPath("data/Diccionari.txt");
        try {
            Map<String, String> mapFromFile = Files.lines(path)
                    //.filter(s -> s.matches("^\\w+:\\w+"))
                    .collect(Collectors.toMap(k -> k.split(":")[0], v -> v.split(":")[1]));

            if (mapFromFile.containsKey(token.trim())) {
                return mapFromFile.get(token.trim());
            }

            if (token.trim().matches("(^\"[a-zA-Z0-9\\s]+\")$")) {
                return "STRING_CONTENT";
            } else if (token.trim().matches("(^[0-9]+[.][0-9]+)$")) {
                return "FLOAT_VALUE";
            } else if (token.trim().matches("(^[0-9]+)$")) {
                return "INT_VALUE";
            }else if (token.trim().matches("(^[a-zA-Z][a-zA-Z0-9_]*)$")) {
                return "NAME";
            }

        } catch (IOException e) {
            System.out.println(e);
        }

        return "UNKNOWN_REGEX";
    }
}

