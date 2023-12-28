/**
 * @author Victor Vallés
 * @author Carles Torrubiano
 * @author Oscar Julian
 * @author Bernat Segura
 * @author Rafael Morera
 */
package Errors;
import java.util.LinkedList;

/**
 * Classe ErrorHandler que s'encarrega de afegir un error a la llista
 */

public class ErrorHandler {
    public static LinkedList<NodeError> nodeErrors = new LinkedList<>();

    /**
     * Constructor de ErrorHandler, agrega l'error a la llista d'errors.
     * @param error
     */
    public static void addError(NodeError error) {
        nodeErrors.add(error);
    }

    /**
     * Funcio que retorna tots els errors que ha tingut l'usuari
     */
    public static void getErrors(){
        if (nodeErrors.size() > 0) {
            for (NodeError nodeError: nodeErrors){
                System.out.println(nodeError.toString());
            }

            System.exit(0);
        }
    }
}