package symbols;

public class Literal extends Yytoken {
    private String value;

    public Literal(String type, int line, int column, int endColumn, String value) {
        super(type, line, column, endColumn);
        this.value = value;
    }

    public String toString() {
        return String.format("%sLiteral(%d, %d, %d, value=%s)", type, line, column, endColumn, value);
    }
}