package Compiler.SymbolTable;

import java.util.ArrayList;

/**
 * Classe Function que extends de la classe Symbol
 * Ens permet diferenciar entre els 2 tipus de simbols
 * */
public class Function extends Symbol{
    public ArrayList<Variable> parameters = new ArrayList<Variable>();
    public ArrayList<Variable> returns = new ArrayList<Variable>();

    /**
     * Contructor
     * @param token String que rebem del codi
     * @param regex String que ens diu que conte el token
     */
    public Function(String token, String regex, int line) {
        super(token, regex, line);
    }

    /**
     * Afegim un parametre a la funció
     * @param var variable de parametre que estem afegint
     */
    public void addParameter(Variable var) {
        this.parameters.add(var);
    }

    /**
     * Afegim un retorn de la funció
     * @param var variable de retorn
     */
    public void addReturn(Variable var) {
        this.returns.add(var);
    }

    /**
     * Getter del array de params d'una funció
     * @return array amb els params d'una funció
     */
    public ArrayList<Variable> getParameters() {
        return parameters;
    }

    /**
     * Getter del array de returns d'una funció
     * @return array amb els params d'una funció
     */
    public ArrayList<Variable> getReturns() {
        return returns;
    }
}
