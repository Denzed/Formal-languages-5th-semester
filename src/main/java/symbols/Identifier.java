package symbols;

public class Identifier extends Yytoken {
    public Identifier(String name, int line, int column, int endColumn) {
        super(name, line, column, endColumn);
    }

    public String toString() {
        return String.format("Identifier(%s, %d, %d, %d)", type, line, column, endColumn);
    }
}