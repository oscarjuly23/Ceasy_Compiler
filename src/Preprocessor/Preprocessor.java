/**
 * @author Oscar Julian
 * @author Bernat Segura
 * @author Victor Valles
 * @author Rafael Morera
 * @author Carles Torrubiano
 */
package Preprocessor;

import Utils.FileManager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.StringJoiner;

/**
 * Classe Preprocessor.
 * L'objectiu de la classe es eliminar comentaris, espais entre linies i fer el reemplaçament dels defines.
 */
public class Preprocessor {
    private final String code = "file.cy";
    private final String out = "file.cy.preprocessor";
    private FileManager fileManager;
    private HashMap<String, String> definesMap;

    /**
     * Constructor de la classe.
     * Incialitza el HashMap i el FileManager.
     */
    public Preprocessor() {
        definesMap = new HashMap<>();
        fileManager = new FileManager(code, out);
    }

    /**
     * Funció preprocess encarecada d'eliminar comentaris i reemplaçar els defines.
     * Retorna en un fitxer *.preprocessor el resultat.
     */
    public void preprocess() {
        String line = fileManager.readLine();
        for (int i = 0; line != null; i++) {
            //Comprova si la linea és un comentari i no es un define
            if (checkCommentLine(line) && checkCommentBlock(line) && !checkDefine(line)) {
                //Substitueix els possibles valors dels defines d'una linea
                line = replaceDefine(line);
                fileManager.writeFile(line);
            } else {
                fileManager.writeFile(""); //Volem mantenir la referencia de linies
            }
            //Llegeix següent linea
            line = fileManager.readLine();
        }
        fileManager.close();
    }

    /**
     * Funció que verifica el regex del comentari de linia.
     *
     * @param line línea llegida actualment.
     * @return booleà si compleix amb el regex d'un comentari de línia.
     */
    private boolean checkCommentLine(String line) {
        return !line.trim().matches("(^//.*)$");
    }

    /**
     * Funció que verifica el regex d'un define i el guarda en format K,V en un HashMap
     *
     * @param line línea llegida actualment.
     * @return booleà si compleix amb el regex d'un define.
     */
    private boolean checkDefine(String line) {
        //Comprova amb regex si coincideix amb un define
        if (line.trim().matches("(^#define\\s[a-zA-Z][a-zA-Z0-9_]*\\s.*)$")) {
            //Separem la linea per espais
            String[] splitedLine = line.split(" ");
            //Guardem la linea al HashMap
            definesMap.put(splitedLine[1], splitedLine[2]);
            return true;
        }
        return false;
    }

    /**
     * Funció que reemplaça la key del valor d'un define pel seu valor guardat en el HashMap.
     *
     * @param line línea llegida actualment.
     * @return String línia amb el replace de la key del define pel valor.
     */
    private String replaceDefine(String line) {
        //Recorrem el HashMap per cada linea per substituir els defines
        String[] repLine = line.split(" ");
        for (String key : definesMap.keySet()) {
            //Comprova si la linea conté la key guardada
            for (int i = 0; i < repLine.length; i++) {
                if (key.equals(repLine[i].trim())) {
                    repLine[i] = definesMap.get(key);
                }
            }
        }
        StringJoiner joiner = new StringJoiner(" ");
        for(int i = 0; i < repLine.length; i++) {
            joiner.add(repLine[i]);
        }
        line = joiner.toString();
        return line;
    }


    /**
     * Funció que verifica el regex del comentari de block.
     *
     * @param line línea llegida actualment.
     * @return booleà si compleix amb el regex d'un comentari de block.
     */
    private boolean checkCommentBlock(String line) {
        if (line.trim().matches("(^/[*]+.*)$")) {
            while (!fileManager.readLine().trim().matches("(^[*]+/$)")) {
            }
            return false;
        }
        return true;
    }
}
