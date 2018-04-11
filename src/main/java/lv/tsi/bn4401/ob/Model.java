package lv.tsi.bn4401.ob;

import de.vandermeer.asciitable.AsciiTable;

import java.util.*;

public class Model {
    private int maxInStack;
    private double startTime;
    private double endTime;
    private Random r1;
    private Random r2;
    private Random r3;
    private Random r4;
    private double WS1;
    private double WS2;
    private double H;
    private int S;
    private Stack<String> stack;
    private double Tm;
    private List<Record> list;

    public Model(){
        this.r1 = new Random(1);
        this.r2 = new Random(2);
        this.r3 = new Random(3);
        this.r4 = new Random(4);
        this.stack = new Stack<>();
        this.startTime = 0;
        this.endTime = 500;
        this.H = this.endTime + 1;
        this.WS1 = getExponential(r1, 0.2);
        this.WS2 = getErlang(r2, 2, 0.1);
        this.list = new ArrayList<>();
        this.maxInStack = 0;
    }

    public int getMaxInStack() {
        return maxInStack;
    }

    private double min(double a, double b, double c){
        if (a <= b && a <= c) return a;
        if (b <= a && b <= c) return b;
        else return c;
    }

    private String stackToString(Stack<String> stack){
        StringBuilder sb = new StringBuilder();
        stack.forEach(element -> sb.append(element + " "));
        return sb.toString();
    }

    public List<Record> getList() {
        return list;
    }

    private double getExponential(Random random, double lambda){
        return (-(1.0 / lambda)) * Math.log(1.0 - random.nextDouble());
    }

    private double getErlang(Random random, int l, double lambda){
        double out = 0;
        for (int i = 0; i < l; i++){
            out += getExponential(random, lambda);
        }
        return out;
    }

    private double getNormal(Random random, double M, double D, int n){
        double sum = 0;
        for(int i = 0; i < n; i++){
            sum += random.nextDouble();
        }
        return ((sum - (n / 2.0)) / Math.sqrt(n / 12.0)) * D + M;
    }

    public void process(){
        int w1CountInStack = 0;
        Record record = new Record(null, Tm, WS1, WS2, H, S, stack.size(), stackToString(stack));
        list.add(record);

        while ((Tm = min(WS1, WS2, H)) < endTime){
            Record rec = new Record();
            rec.setTm(Tm);

            if (WS1 == Tm){
                rec.setEvent("WS1");
                if (S == 0){
                    S = 1;
                    H = Tm + getNormal(r3, 20, 3, 100);
                } else {
                    w1CountInStack ++;
                    stack.push("WS1");
                }
                WS1 = Tm + getExponential(r1, 0.2);
            }
            if (WS2 == Tm){
                rec.setEvent("WS2");
                if (S == 0){
                    S = 1;
                    H = Tm + getExponential(r4, 0.2);
                } else {
                    //make counter
                    stack.push("WS2");
                }
                WS2 = Tm + getErlang(r2, 2, 0.1);
            }
            if (H == Tm){
                rec.setEvent("H");
                if (stack.size() == 0){
                    H = endTime + 1;
                    S = 0;
                } else {
                    String type = stack.pop();
                    if (type == "WS1"){
                        if (w1CountInStack > maxInStack) maxInStack = w1CountInStack;
                        w1CountInStack--;
                        H = Tm + getNormal(r3, 20, 3, 100);
                    } else {
                        H = Tm + getExponential(r4, 0.2);
                    }
                }
            }

            rec.setL1(WS1);
            rec.setL2(WS2);
            rec.setH(H);
            rec.setS(S);
            rec.setN(stack.size());
            rec.setStack(stackToString(stack));
            list.add(rec);
        }
    }

    class Record{
        private String event;
        private double Tm;
        private double l1;
        private double l2;
        private double h;
        private int S;
        private int N;
        private String stack;

        public Record() {
        }

        public Record(String event, double tm, double l1, double l2, double h, int s, int n, String stack) {
            this.event = event;
            Tm = tm;
            this.l1 = l1;
            this.l2 = l2;
            this.h = h;
            S = s;
            N = n;
            this.stack = stack;
        }



        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public double getTm() {
            return Tm;
        }

        public void setTm(double tm) {
            Tm = tm;
        }

        public double getL1() {
            return l1;
        }

        public void setL1(double l1) {
            this.l1 = l1;
        }

        public double getL2() {
            return l2;
        }

        public void setL2(double l2) {
            this.l2 = l2;
        }

        public double getH() {
            return h;
        }

        public void setH(double h) {
            this.h = h;
        }

        public int getS() {
            return S;
        }

        public void setS(int s) {
            S = s;
        }

        public int getN() {
            return N;
        }

        public void setN(int n) {
            N = n;
        }

        public String getStack() {
            return stack;
        }

        public void setStack(String stack) {
            this.stack = stack;
        }

        @Override
        public String toString() {
            return  "event=" +  (event == null ? "" : event) +
                    " Tm=" + Tm +
                    " WS1=" + l1 +
                    " WS2=" + l2 +
                    " h=" + h +
                    " S=" + S +
                    " N=" + N +
                    " stack='" + stack + '\'';
        }
    }


    public static void main(String[] args) {
        Model model = new Model();
        model.process();

        System.out.println("W1 max in stack: " + model.getMaxInStack());

        AsciiTable table = new AsciiTable();
        table.addRule();
        table.addRow("Event", "Tm", "WS1", "WS2", "H", "S", "N", "Stack");
        table.addRule();
        model.getList().forEach(
                record -> {
                    table.addRow(record.getEvent() == null ? "" : record.getEvent(),
                            Math.round(record.getTm() * 100.0) / 100.0,
                            Math.round(record.getL1() * 100.0) / 100.0,
                            Math.round(record.getL2() * 100.0) / 100.0,
                            Math.round(record.getH() * 100.0) / 100.0,
                            record.getS(),
                            record.getN(),
                            record.getStack());
                    table.addRule();
                }
        );

        System.out.println(table.render(120));

//        Random random1 = new Random(1);
//        System.out.println("Exponential: ");
//        for (int i = 0; i < 100; i++) {
//            System.out.print(model.getExponential(random1, 0.2) + ", ");
//        }
//
//        Random random2 = new Random(2);
//        System.out.println("\n\nErlang: ");
//        for (int i = 0; i < 100; i++) {
//            System.out.print(model.getErlang(random2, 2, 0.1) + ", ");
//
//        }
//
//        Random random3 = new Random(3);
//        System.out.println("\n\nNormal: ");
//        for (int i = 0; i < 100; i++) {
//            System.out.print(model.getNormal(random3, 20, 3, 200) + ", ");
//        }
    }
}
