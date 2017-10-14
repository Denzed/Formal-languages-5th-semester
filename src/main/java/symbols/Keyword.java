package symbols;

public class Keyword extends Yytoken {
    public Keyword(String type, int line, int column, int endColumn) {
        super(type, line, column, endColumn);
    }

    public String toString() {
        return String.format("%sKeyword(%d, %d, %d)", type, line, column, endColumn);
    }
}