/**
 * @author Victor Vallés
 * @author Carles Torrubiano
 * @author Oscar Julian
 * @author Bernat Segura
 * @author Rafael Morera
 */

package Compiler;


import Compiler.Backend.MIPS;
import Compiler.Backend.Optimitzacio;
import Compiler.Frontend.*;
import Errors.ErrorHandler;
import Preprocessor.Preprocessor;

/**
 * Classe Compiler
 * Executa tota la part del bloc de compilador del nostre projecte
 */
public class Compiler {

    /**
     * Funció que fa les crides pertinents a cada apartat del compilador
     */
    public static void compile(String[] args) {
        Preprocessor preprocessor = new Preprocessor();

        //Frontend
        switch (args[0]){
            case "all":
                //Si no hi ha flag, s'executa tots els nivells
                System.out.println("*** Preprocessor ***");
                preprocessor.preprocess();
                System.out.println("*** Lexical ***");
                LexicalAnalyzer.buildTokens();
                System.out.println("*** Syntax ***");
                SyntaxAnalyzer.checkGrammar();
                System.out.println("*** Semantic ***");
                SemanticAnalyzer.semanticAnalyzerTree();
                System.out.println("*** TAC ***");
                TAC.build();
                Optimitzacio.optimize();
                System.out.println("*** MIPS ***");
                MIPS.build();
                System.out.println("***Executed all***");
                ErrorHandler.getErrors();
                break;
            case "--lexical":
                //--lexical: Implementa el lexical i el preprocessor
                preprocessor.preprocess();
                LexicalAnalyzer.buildTokens();
                System.out.println("***Executed lexical***");
                ErrorHandler.getErrors();
                break;
            case "--syntactic":
                //--syntactic: Implementa el sintactic, lexical i el preprocessor
                preprocessor.preprocess();
                LexicalAnalyzer.buildTokens();
                SyntaxAnalyzer.checkGrammar();
                System.out.println("***Executed syntactic***");
                ErrorHandler.getErrors();
                break;
            case "--semantic":
                //--semantic: Implementa el semantic, sintactic, lexical i el preprocessor
                preprocessor.preprocess();
                LexicalAnalyzer.buildTokens();
                SyntaxAnalyzer.checkGrammar();
                SemanticAnalyzer.semanticAnalyzerTree();
                System.out.println("***Executed semantic***");
                ErrorHandler.getErrors();
                break;
        }
    }
}
