package symbols;

public class Separator extends Yytoken {
    public Separator(String type, int line, int column, int endColumn) {
        super(type, line, column, endColumn);
    }

    public String toString() {
        return String.format("%s(%d, %d, %d)", type, line, column, endColumn);
    }
}