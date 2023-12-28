/**
 * @author Victor Vallés
 * @author Carles Torrubiano
 * @author Oscar Julian
 * @author Bernat Segura
 * @author Rafael Morera
 */

package Compiler.SymbolTable;

/**
 * Classe conté tota la informació relacionada amb un token
 */
public class Symbol {
    private String token;
    private String regex;
    private int lineNumber;

    /**
     * Constructor de Symbol
     * @param token String del token que volem insertar
     */
    public Symbol(String token) {
        this.token = token;
    }

    /**
     * Constructor de Symbol
     * @param token String del token que volem insertar
     * @param regex String del significat del token que ens dona el regex
     */
    public Symbol(String token, String regex) {
        this.token = token;
        this.regex = regex;
    }

    /**
     * Constructor de Symbol
     * @param token String del token que volem insertar
     * @param regex String del significat del token que ens dona el regex
     */
    public Symbol(String token, String regex, int lineNumber) {
        this.token = token;
        this.regex = regex;
        this.lineNumber = lineNumber;
    }

    /**
     * Getter de la variable token
     * @return String que conte el token
     */
    public String getToken() {
        return token;
    }

    /**
     * Getter de la variable lineNumber
     * @return int que es el numero de linea
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Getter de la variable regex
     * @return String que conte el regex
     */
    public String getRegex() {
        return regex;
    }

    @Override
    public String toString() {
        return "Symbol{" +
                "token='" + token + '\'' +
                ", regex='" + regex + '\'' +
                ", lineNumber='" + lineNumber + '\'' +
                '}';
    }
}
