import Preprocessor.Preprocessor;
import Compiler.Compiler;

public class main {
    public static void main(String[] args) {
        if (args.length == 1 && args[0].toLowerCase().equals("--preprocessor")){
            Preprocessor preprocessor = new Preprocessor();
            preprocessor.preprocess();
            System.out.println("***Executed preprocessor***");
        } else if (args.length == 1 && args[0].toLowerCase().equals("--help")) {
            System.out.println("******** CEASY HELP ********");
            System.out.println("Només es pot utilitzar un flag o sense flag\n");
            System.out.println("FLAGS:");
            System.out.println("--preprocessor");
            System.out.println("--lexical");
            System.out.println("--syntactic");
            System.out.println("--semantic");
        } else if (args.length > 1) {
            System.out.println("Sobren flags, consultar --help");
        } else if (args.length == 1 && args[0].toLowerCase().equals("--lexical")){
            Compiler.compile(args);
        } else if (args.length == 1 && args[0].toLowerCase().equals("--syntactic")){
            Compiler.compile(args);
        } else if (args.length == 1 && args[0].toLowerCase().equals("--semantic")){
            Compiler.compile(args);
        } else if (args.length == 0) {
            //Si no te cap flag, executa tots els nivells
            String[] arg = new String[] {"all"};
            Compiler.compile(arg);
        } else {
            System.out.println("Error amb paràmetres, consultar --help");
        }
    }
}