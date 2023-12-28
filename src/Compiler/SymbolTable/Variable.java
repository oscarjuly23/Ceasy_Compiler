package Compiler.SymbolTable;

/**
 * Classe Variable que extends de la classe Symbol
 * Ens permet diferenciar entre els 2 tipus de simbols
 * */
public class Variable extends Symbol{
    private String tipus;

    /**
     * Constructor de Variable
     * @param token String que rebem del codi
     * @param regex String que ens diu que conte el token
     */
    public Variable(String token, String regex, int line, String tipus) {
        super(token, regex, line);
        this.tipus = tipus;
    }

    /**
     * Getter de la variable tipus
     * @return String que conte el tipus de la variable
     */
    public String getTipus() {
        return tipus;
    }
}
