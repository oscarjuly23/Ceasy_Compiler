/**
 * @author Victor Vallés
 * @author Carles Torrubiano
 * @author Oscar Julian
 * @author Bernat Segura
 * @author Rafael Morera
 */
package Errors;

/**
 * Classe node error amb tota la informació d'un error. Amb el seu tipus, predició, la línia, etc.
 */
public class NodeError {
    private int line;
    private String typeError;
    private String error;
    private String context;
    private String textUser;
    private String prediction;

    /**
     * Constructor del node
     * @param line Mostra en quina línia s'ha comès l'error
     * @param typeError Indica quin tipus d'error tracta
     * @param error Mostra l'error que és
     * @param context Indica en quin context s'ha comès l'error
     * @param textUser Mostra l'error que l'usuari ha escrit
     * @param prediction Mostra una possible solució a l'usuari
     */
    public NodeError(int line, String typeError, String error, String context, String textUser, String prediction) {
        this.line = line;
        this.typeError = typeError;
        this.error = error;
        this.context = context;
        this.textUser = textUser;
        this.prediction = prediction;
    }

    /**
     * Funcio que printa el Node
     * @return Retorna totes les dades del node
     */
    @Override
    public String toString(){
        return "Error line: " + line + ", Error type: " + typeError + ", Error name: " + error + ", Context: " + context + ", User input: " + textUser + ", Prediction: " + prediction;
    }
}
