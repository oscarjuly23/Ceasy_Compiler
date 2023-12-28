package Compiler.Backend;

import Compiler.Frontend.Quadruple.Quadruple;
import Compiler.Frontend.TAC;
import Utils.FileManager;

import java.util.LinkedList;

public class MIPS {

    private static LinkedList<String> varsList = new LinkedList<>();
    private static String fakeVarName = null;

    public static void build() {
        saveAsCode();
    }

    private static void saveAsCode() {
        FileManager fm = new FileManager("file.cy.asm");
        FileManager fmTAC = new FileManager("file.cy.tac");
        for (Quadruple quad : TAC.code) {
            fmTAC.writeOnlyOneLine(quad.toString());
            switch (quad.getOp()) {
                case "MIPS_DATA":
                case "MIPS_TEXT":
                    fm.writeOnlyOneLine("." + quad.getTmp());
                    break;
                case "VARIABLE":
                    String initValue = "0";
                    if (quad.getArg1() != null) {
                        initValue = quad.getArg1();
                    }
                    fm.writeOnlyOneLine(quad.getTmp() + ": .word " + initValue);
                    varsList.add(quad.getTmp());
                    break;
                case "SECTION":
                    fm.writeOnlyOneLine(quad.getTmp() + ":");
                    break;
                case "JUMP":
                    //Tornem al principi del while
                    fm.writeOnlyOneLine("j " + quad.getTmp());
                    break;
                case "IF":
                case "WHILE":
                    //En ambdos casos si evalua true no hem de saltar i si evalua false si

                    //Condicio de si ens saltem la linea o no
                    //beq cond, $zero, while_end ->

                    //Passem els arg a comparar a registres
                    String label = quad.getArg2();
                    quad.setArg2(null);
                    argsToRegisters(quad, varsList, fm);
                    fm.writeOnlyOneLine("beq $zero, $t8, " + label);

                    break;
                case "CALL_FUNC":
                    //Setejem els parametres
                    if (quad.getArg2() != null) {
                        // $a0-3 registres per passar els 4 primers parametres a una funció
                        // Movem el contingut de la variable temp al registre de parametres

                        if (checkIfIsFakeVar(quad.getArg2())){
                            fm.writeOnlyOneLine("lw $t7, " + quad.getArg2() + "($zero)");
                            fakeVarName = quad.getArg2();
                            quad.setArg2("t7");
                        }

                        fm.writeOnlyOneLine("add $a0, $zero , $" + quad.getArg2());

                        if (fakeVarName != null) {
                            fm.writeOnlyOneLine("sw $t7, " + fakeVarName + "($zero)");
                            fakeVarName = null;
                        }
                    }

                    // Cridem ens movem a la funcio en questio
                    fm.writeOnlyOneLine("jal " + quad.getArg1());

                    //Guardem el return de la funció
                    if (quad.getTmp() != null) {
                        // $v0-1 registres per passar els valors de retorn de les funcions

                        if (checkIfIsFakeVar(quad.getTmp())){
                            fm.writeOnlyOneLine("lw $t7, " + quad.getTmp() + "($zero)");
                            fakeVarName = quad.getTmp();
                            quad.setTmp("t7");
                        }

                        fm.writeOnlyOneLine("add $" + quad.getTmp() + ", $zero , $v0");

                        if (fakeVarName != null) {
                            fm.writeOnlyOneLine("sw $t7, " + fakeVarName + "($zero)");
                            fakeVarName = null;
                        }

                    }
                    break;
                case "RETURN":
                    // Setejem els parametres de retorn
                    if (quad.getArg1() != null) {

                        if (checkIfIsFakeVar(quad.getArg1())){
                            fm.writeOnlyOneLine("lw $t7, " + quad.getArg1() + "($zero)");
                            fakeVarName = quad.getArg1();
                            quad.setArg1("t7");
                        }

                        fm.writeOnlyOneLine("add $v0, $zero , $" + quad.getArg1());

                        if (fakeVarName != null) {
                            fm.writeOnlyOneLine("sw $t7, " + fakeVarName + "($zero)");
                            fakeVarName = null;
                        }
                    }
                    //Fem un salt a la adreça guardada a $ra
                    fm.writeOnlyOneLine("jr $ra");
                    break;
                case "END_PROGRAM":
                    // Hem arribat al final del main i per tant saltem al final del programa
                    fm.writeOnlyOneLine("j END_PROGRAM");
                    break;
                case "BIGGER_THAN":
                    //Passem els arg a comparar a registres
                    argsToRegisters(quad, varsList, fm);
                    //Condicio de comparacio

                    if (checkIfIsFakeVar(quad.getTmp())){
                        fm.writeOnlyOneLine("lw $t7, " + quad.getTmp() + "($zero)");
                        fakeVarName = quad.getTmp();
                        quad.setTmp("t7");
                    }

                    fm.writeOnlyOneLine("slt $" + quad.getTmp() + ", $t9, $t8");

                    if (fakeVarName != null) {
                        fm.writeOnlyOneLine("sw $t7, " + fakeVarName + "($zero)");
                        fakeVarName = null;
                    }
                    break;
                case "LOWER_THAN":
                    //Passem els arg a comparar a registres
                    argsToRegisters(quad, varsList, fm);
                    //Condicio de comparacio

                    if (checkIfIsFakeVar(quad.getTmp())){
                        fm.writeOnlyOneLine("lw $t7, " + quad.getTmp() + "($zero)");
                        fakeVarName = quad.getTmp();
                        quad.setTmp("t7");
                    }

                    fm.writeOnlyOneLine("slt $" + quad.getTmp() + ", $t8, $t9");

                    if (fakeVarName != null) {
                        fm.writeOnlyOneLine("sw $t7, " + fakeVarName + "($zero)");
                        fakeVarName = null;
                    }
                    break;
                case "EQUAL_THAN":
                    //Passem els arg a comparar a registres
                    argsToRegisters(quad, varsList, fm);

                    //Condicio de comparacio
                    if (checkIfIsFakeVar(quad.getTmp())){
                        fm.writeOnlyOneLine("lw $t7, " + quad.getTmp() + "($zero)");
                        fakeVarName = quad.getTmp();
                        quad.setTmp("t7");
                    }

                    fm.writeOnlyOneLine("xor $" + quad.getTmp() + ", $t8, $t9");
                    fm.writeOnlyOneLine("slti $" + quad.getTmp() + ", $" + quad.getTmp() + ", 1");

                    if (fakeVarName != null) {
                        fm.writeOnlyOneLine("sw $t7, " + fakeVarName + "($zero)");
                        fakeVarName = null;
                    }
                    break;
                case "DIFFERENT_THAN":
                    //Passem els arg a comparar a registres
                    argsToRegisters(quad, varsList, fm);

                    //Condicio de comparacio
                    if (checkIfIsFakeVar(quad.getTmp())){
                        fm.writeOnlyOneLine("lw $t7, " + quad.getTmp() + "($zero)");
                        fakeVarName = quad.getTmp();
                        quad.setTmp("t7");
                    }
                    fm.writeOnlyOneLine("xor $" + quad.getTmp() + ", $t8, $t9");
                    fm.writeOnlyOneLine("slt $" + quad.getTmp() + ", $zero, $" + quad.getTmp());

                    if (fakeVarName != null) {
                        fm.writeOnlyOneLine("sw $t7, " + fakeVarName + "($zero)");
                        fakeVarName = null;
                    }
                    break;
                case "EQUAL":
                    /* OPCIONS

                        (1) VAR = VARFAKE || VAR
                        (2) VAR = TMP
                        (3) VAR = INT

                        (4) TMP = VAR || VARFAKE
                        (5) TMP = TMP
                        (6) TMP = INT

                        (7) VARFAKE = VAR || VARFAKE
                        (8) VARFAKE = INT
                        (9) VARFAKE = TMP

                     */

                    if (varsList.contains(quad.getTmp())) {
                        if (varsList.contains(quad.getArg1())) {
                            // (1) VAR = VARFAKE || VAR
                            fm.writeOnlyOneLine("lw $t7, " + quad.getArg1() + "($zero)");
                            fm.writeOnlyOneLine("sw $t7, " + quad.getTmp() + "($zero)");
                        } else if ((!varsList.contains(quad.getArg1()) && quad.getArg1().contains("t") && quad.getArg1().split("t")[1].matches("(^[0-9]+)$") && Integer.parseInt(quad.getArg1().split("t")[1]) <= 6) || quad.getArg1().equals("a0")) {
                            // (2) VAR = TMP
                            fm.writeOnlyOneLine("sw $" + quad.getArg1() + ", " + quad.getTmp() + "($zero)");
                        } else {
                            // (3) VAR = INT
                            fm.writeOnlyOneLine("li $t7, " + quad.getArg1());
                            fm.writeOnlyOneLine("sw $t7, " + quad.getTmp() + "($zero)");
                        }
                    } else if ((!varsList.contains(quad.getTmp()) && quad.getTmp().contains("t") && quad.getTmp().split("t")[1].matches("(^[0-9]+)$") && Integer.parseInt(quad.getTmp().split("t")[1]) <= 6) || quad.getTmp().equals("a0")) {
                        if (varsList.contains(quad.getArg1())) {
                            // (4) TMP = VAR || VARFAKE
                            fm.writeOnlyOneLine("lw $" + quad.getTmp() + ", " + quad.getArg1() + "($zero)");
                        } else if ((!varsList.contains(quad.getArg1()) && quad.getArg1().contains("t") && quad.getArg1().split("t")[1].matches("(^[0-9]+)$") && Integer.parseInt(quad.getArg1().split("t")[1]) <= 6) || quad.getArg1().equals("a0")) {
                            // (5) TMP = TMP
                            fm.writeOnlyOneLine("add $" +  quad.getTmp() + ", $zero , $" + quad.getArg1());
                        } else {
                            // (6) TMP = INT
                            fm.writeOnlyOneLine("li $" + quad.getTmp() + ", " + quad.getArg1());
                        }
                    }
                    break;
                case "ADD":
                    /* Code
                        addu    $t2,$t0,$t1
                        add registers t0 and t1, put result in t2
                    */
                    argsToRegisters(quad, varsList, fm);

                    if (checkIfIsFakeVar(quad.getTmp())){
                        fm.writeOnlyOneLine("lw $t7, " + quad.getTmp() + "($zero)");
                        fakeVarName = quad.getTmp();
                        quad.setTmp("t7");
                    }

                    fm.writeOnlyOneLine("add $" + quad.getTmp() + ",$t8, $t9");

                    if (fakeVarName != null) {
                        fm.writeOnlyOneLine("sw $t7, " + fakeVarName + "($zero)");
                        fakeVarName = null;
                    }
                    break;
                case "MULTIPLICATION":
                    /* Code
                        mult $t8, $t9
                        mflo $t0
                        mul registers t8 and t9, put result in t0
                    */
                    argsToRegisters(quad,varsList,fm);
                    fm.writeOnlyOneLine("mult $t8, $t9");

                    if (checkIfIsFakeVar(quad.getTmp())){
                        fakeVarName = quad.getTmp();
                        quad.setTmp("t7");
                    }

                    fm.writeOnlyOneLine("mflo $" + quad.getTmp());

                    if (fakeVarName != null) {
                        fm.writeOnlyOneLine("sw $t7, " + fakeVarName + "($zero)");
                        fakeVarName = null;
                    }

                    break;
                case "DIVISION":
                    /* Code
                        div $t8, $t9
                        mflo $t0
                        div registers t8 and t9, put quotient in t0
                    */
                    argsToRegisters(quad,varsList,fm);
                    fm.writeOnlyOneLine("div $t8, $t9");

                    if (checkIfIsFakeVar(quad.getTmp())){
                        fakeVarName = quad.getTmp();
                        quad.setTmp("t7");
                    }

                    fm.writeOnlyOneLine("mflo $" + quad.getTmp());

                    if (fakeVarName != null) {
                        fm.writeOnlyOneLine("sw $t7, " + fakeVarName + "($zero)");
                        fakeVarName = null;
                    }

                    break;
                case "PERCENTAGE":
                    /* Code
                        div $t8, $t9
                        mfhi $t0
                        div registers t8 and t9, put remainder in t0
                    */
                    argsToRegisters(quad,varsList,fm);
                    fm.writeOnlyOneLine("div $t8, $t9");

                    if (checkIfIsFakeVar(quad.getTmp())){
                        fakeVarName = quad.getTmp();
                        quad.setTmp("t7");
                    }

                    fm.writeOnlyOneLine("mfhi $" + quad.getTmp());

                    if (fakeVarName != null) {
                        fm.writeOnlyOneLine("sw $t7, " + fakeVarName + "($zero)");
                        fakeVarName = null;
                    }
                    break;
                case "SUBSTRACT":
                     /* Code
                        sub $t8, $t9
                        sub registers t8 and t9, put substract in t0
                    */
                    argsToRegisters(quad, varsList, fm);

                    if (checkIfIsFakeVar(quad.getTmp())){
                        fm.writeOnlyOneLine("lw $t7, " + quad.getTmp() + "($zero)");
                        fakeVarName = quad.getTmp();
                        quad.setTmp("t7");
                    }

                    fm.writeOnlyOneLine("sub $" + quad.getTmp() + ",$t8, $t9");

                    if (fakeVarName != null) {
                        fm.writeOnlyOneLine("sw $t7, " + fakeVarName + "($zero)");
                        fakeVarName = null;
                    }
                    break;
            }
        }
    }

    private static void argsToRegisters(Quadruple quad, LinkedList<String> varsList, FileManager fm) {
        //Passem els arg a comparar a registres
        if (quad.getArg1() != null) {
            if (varsList.contains(quad.getArg1())) {
                // Si es tracta d'una variable
                fm.writeOnlyOneLine("lw " + "$t8, " + quad.getArg1() + "($zero)");
            } else if (quad.getArg1().contains("t")) {
                // Si es tracta d'un registre
                fm.writeOnlyOneLine("add $t8, $zero, $" + quad.getArg1());
            } else {
                // Si es tracta d'un valor
                fm.writeOnlyOneLine("li " + "$t8, " + quad.getArg1());
            }
        }

        if (quad.getArg2() != null) {
            if (varsList.contains(quad.getArg2())) {
                // Si es tracta d'una variable
                fm.writeOnlyOneLine("lw " + "$t9, " + quad.getArg2() + "($zero)");
            } else if (quad.getArg2().contains("t")) {
                // Si es tracta d'un registre
                fm.writeOnlyOneLine("add $t9, $zero, $" + quad.getArg2());
            } else {
                // Si es tracta d'un valor
                fm.writeOnlyOneLine("li " + "$t9, " + quad.getArg2());
            }
        }
    }

    private static boolean checkIfIsFakeVar (String cositas) {
        return varsList.contains(cositas) && cositas.length() >= 2 && cositas.contains("t") && cositas.split("t")[1].matches("(^[0-9]+)$") && Integer.parseInt(cositas.split("t")[1]) > 6;
    }
}
