package symbols;

public class Operator extends Yytoken {
    public Operator(String type, int line, int column, int endColumn) {
        super(type, line, column, endColumn);
    }

    public String toString() {
        return String.format("Operator(%s, %d, %d, %d)", type, line, column, endColumn);
    }
}