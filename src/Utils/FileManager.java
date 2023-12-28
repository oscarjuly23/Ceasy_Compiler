/**
 * @author Victor Vallés
 * @author Carles Torrubiano
 * @author Oscar Julian
 * @author Bernat Segura
 * @author Rafael Morera
 */

package Utils;

import Compiler.Frontend.SyntaxAnalyzer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Scanner;

/**
 * Classe FileManager
 * L'objectiu de la classe es gestionar les dades de input i output de cada fase.
 * Aquesta ens proporciona una interficie per llegir i escriure als fitxers pertinents.
 */
public class FileManager {

    private final String PATH = "outputFiles/";
    private Scanner reader;
    private FileWriter writer;
    private String fileOneUse;

    /**
     * Constructor de la classe
     * @param fileReadParam Nom del fitxer de lectura
     * @param fileWriteParam Nom del fitxer d'escriptura
     */
    public FileManager(String fileReadParam, String fileWriteParam) {
        File fileRead = new File( PATH + fileReadParam);
        File fileWrite = new File( PATH + fileWriteParam);

        try {
            reader = new Scanner(fileRead);
            writer = new FileWriter(fileWrite, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor de la classe nomes per escriure
     * @param fileOneUse nom del fitxer a escriure
     */
    public FileManager(String fileOneUse) {
        this.fileOneUse = PATH + fileOneUse;
        File fileWrite = new File(this.fileOneUse);

        //Buidem el fitxer
        try {
            FileWriter oneUsWriter = new FileWriter(fileWrite, false);
            oneUsWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Funció que permet llegir una linea del fitxer de lectura
     * @return String: Linea que ha llegit
     */
    public String readLine(){
        if (reader.hasNextLine()){
            return reader.nextLine();
        }
        return null;
    }

    /**
     * Funció que permet escriure una linea al fitxer d'escriptura
     * @param line Linea que volem escriure al fitxer
     */
    public void writeFile(String line) {
        try {
            writer.write(line + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Fa un flush al Writer
     */
    public void writeFlush() {
        try {
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Funcio que guarda al fitxer d'un sol us la linea
     * @param line String que es guarda
     */
    public void writeOnlyOneLine(String line) {
        //Afegim la linea al final del fitxer
        try {
            File fileWrite = new File(this.fileOneUse);
            FileWriter oneUseWriter = new FileWriter(fileWrite, true);
            oneUseWriter.write(line + "\n");
            oneUseWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Funció que tanca tots els recursos i fitxers oberts
     */
    public void close() {
        try {
            writer.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Funció que llegeix la una linea del fitxer d'un sol us
     * @param line linea que es vol llegir
     * @return String amb la linea que hem demanat o null si no existeix
     */
    public String readOnlyOneLine(int line) {

        List<String> list = null;
        try {
            list = Files.readAllLines(new File(this.fileOneUse).toPath(), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (list != null && line < list.size()) {
            return list.get(line);
        }
        return null;
    }

    public static void parseTreeToJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            FileWriter fileWriter = new FileWriter("outputFiles/parseTree.json");
            gson.toJson(SyntaxAnalyzer.parseTree, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
