package Compiler.Frontend.Quadruple;

public class Quadruple {
    private String arg1;
    private String arg2;
    private String op;
    private String tmp;

    public Quadruple(String op, String tmp, String arg1, String arg2) {
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.op = op;
        this.tmp = tmp;
    }

    public String getArg1() {
        return arg1;
    }

    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public void setArg2(String arg2) {
        this.arg2 = arg2;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getTmp() {
        return tmp;
    }

    public void setTmp(String tmp) {
        this.tmp = tmp;
    }

    @Override
    public String toString() {
        return "Quadruple{" +
                "arg1='" + arg1 + '\'' +
                ", arg2='" + arg2 + '\'' +
                ", op='" + op + '\'' +
                ", tmp='" + tmp + '\'' +
                '}';
    }
}
