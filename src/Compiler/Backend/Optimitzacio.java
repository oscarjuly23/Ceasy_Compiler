package Compiler.Backend;

import Compiler.Frontend.Quadruple.Quadruple;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static Compiler.Frontend.TAC.code;
import static Compiler.Frontend.TAC.tmpCount;

public class Optimitzacio {

    public static void optimize() {
        loadMem();
        while(constOperation());
        //initVars();
    }

    private static void loadMem() {
        for (int i = 7; i < tmpCount; i++) {
            code.add(1, new Quadruple("VARIABLE", "t" + i, null, null ));
        }
    }

    private static boolean constOperation() {
        boolean repeat = false;
        for (int quadi = 0; quadi < code.size() && !repeat; quadi++) {
            Quadruple quad = code.get(quadi);
            int res = 0;
            boolean notDefault = false;
            switch (quad.getOp()){
                case "ADD":
                    if (quad.getArg1().matches("(^[0-9]+)$") && quad.getArg2().matches("(^[0-9]+)$")) {
                        res = Integer.parseInt(quad.getArg1()) + Integer.parseInt(quad.getArg2());

                        code.get(code.indexOf(quad)).setOp("EQUAL");
                        code.get(code.indexOf(quad)).setArg1(Integer.toString(Math.abs(res)));
                        code.get(code.indexOf(quad)).setArg2(null);

                        notDefault = true;
                    }
                    break;
                case "SUBSTRACT":
                    if (quad.getArg1().matches("(^[0-9]+)$") && quad.getArg2().matches("(^[0-9]+)$")) {
                        res = Integer.parseInt(quad.getArg1()) - Integer.parseInt(quad.getArg2());

                        code.get(code.indexOf(quad)).setOp("EQUAL");
                        code.get(code.indexOf(quad)).setArg1(Integer.toString(Math.abs(res)));
                        code.get(code.indexOf(quad)).setArg2(null);

                        notDefault = true;
                    }
                    break;
                case "MULTIPLICATION":
                    if (quad.getArg1().matches("(^[0-9]+)$") && quad.getArg2().matches("(^[0-9]+)$")) {
                        res = Integer.parseInt(quad.getArg1()) * Integer.parseInt(quad.getArg2());

                        code.get(code.indexOf(quad)).setOp("EQUAL");
                        code.get(code.indexOf(quad)).setArg1(Integer.toString(Math.abs(res)));
                        code.get(code.indexOf(quad)).setArg2(null);

                        notDefault = true;
                    }
                    break;
                case "DIVISION":
                    if (quad.getArg1().matches("(^[0-9]+)$") && quad.getArg2().matches("(^[0-9]+)$")) {
                        res = Integer.parseInt(quad.getArg1()) / Integer.parseInt(quad.getArg2());

                        code.get(code.indexOf(quad)).setOp("EQUAL");
                        code.get(code.indexOf(quad)).setArg1(Integer.toString(Math.abs(res)));
                        code.get(code.indexOf(quad)).setArg2(null);

                        notDefault = true;
                    }
                    break;
                case "PERCENTAGE":
                    if (quad.getArg1().matches("(^[0-9]+)$") && quad.getArg2().matches("(^[0-9]+)$")) {
                        res = Integer.parseInt(quad.getArg1()) % Integer.parseInt(quad.getArg2());

                        code.get(code.indexOf(quad)).setOp("EQUAL");
                        code.get(code.indexOf(quad)).setArg1(Integer.toString(Math.abs(res)));
                        code.get(code.indexOf(quad)).setArg2(null);

                        notDefault = true;
                    }
                    break;
                default:
                    notDefault = false;
                    break;
            }
            ArrayList<String> opList = new ArrayList<>();
            opList.add("ADD");
            opList.add("SUBSTRACT");
            opList.add("MULTIPLICATION");
            opList.add("DIVISION");
            opList.add("PERCENTAGE");
            opList.add("EQUAL");
            if (notDefault) {
                for (int i = quadi + 1; i < code.size(); i++) {
                    if (code.get(i).getTmp() != null && code.get(i).getTmp().equals(quad.getTmp())) {
                        break;
                    }
                    if (code.get(i).getArg1() != null && code.get(i).getArg1().equals(quad.getTmp()) && opList.contains(code.get(i).getOp())) {
                        //Expandim el valor
                        code.get(i).setArg1(Integer.toString(Math.abs(res)));
                        //Borrem l'assignacio innecessaria
                        code.remove(quadi);
                        //Indiquem que es torni a executar la optimitzacio
                        repeat = true;
                        break;
                    }

                    if (code.get(i).getArg2() != null && code.get(i).getArg2().equals(quad.getTmp()) && opList.contains(code.get(i).getOp())) {
                        //Expandim el valor
                        code.get(i).setArg2(Integer.toString(Math.abs(res)));
                        //Borrem l'assignacio innecessaria
                        code.remove(quadi);
                        //Indiquem que es torni a executar la optimitzacio
                        repeat = true;
                        break;
                    }
                }
            }
        }
        return repeat;
    }

    private static void initVars () {
        ArrayList<Quadruple> varList = new ArrayList<>();

        code.stream().filter( quad -> quad.getOp().equals("VARIABLE"))
                .collect(Collectors.toCollection(() -> varList));

        boolean loadIntFound = false;
        for (Quadruple varQuad: varList) {
            loadIntFound = false;
            for (Quadruple currQuad : code) {
                if (loadIntFound && varQuad.getTmp().equals(currQuad.getTmp()) && !currQuad.getOp().equals("VARIABLE")
                        && currQuad.getArg1().equals(code.get(code.indexOf(currQuad) - 1).getArg1())) {
                    varQuad.setArg1(code.get(code.indexOf(currQuad) - 1).getArg1());
                    code.remove(code.indexOf(currQuad) - 1);
                    code.remove(currQuad);
                    break;
                }
                loadIntFound = false;
                if (!currQuad.getOp().equals("VARIABLE") && (varQuad.getTmp().equals(currQuad.getTmp()) || varQuad.getTmp().equals(currQuad.getArg1()) || varQuad.getTmp().equals(currQuad.getArg2()))) {
                    break;
                }

                if (currQuad.getOp().equals("EQUAL") && currQuad.getTmp().contains("t") && currQuad.getArg1().matches("(^[0-9]+)$")) {
                    loadIntFound = true;
                }
            }
        }
        for (Quadruple varQuad: varList) {
            for (Quadruple currQuad : code) {
                if (varQuad.getTmp().equals(currQuad.getTmp()) && !currQuad.getOp().equals("VARIABLE")) {
                    if (currQuad.getOp().equals("EQUAL") && currQuad.getArg1().matches("(^[0-9]+)$")) {
                        varQuad.setArg1(currQuad.getArg1());
                        code.remove(currQuad);
                    }
                    break;
                }
            }
        }
    }
}
