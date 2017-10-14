package symbols;

public class Yytoken {
    protected String type;
    protected int line;
    protected int column;
    protected int endColumn;

    public Yytoken(String type, int line, int column, int endColumn) {
        this.type = type;
        this.line = line;
        this.column = column;
        this.endColumn = endColumn;
    }
}