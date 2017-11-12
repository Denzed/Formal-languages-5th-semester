package symbols;

public class Comment extends Yytoken {
    public Comment(String text, int line, int column, int endColumn) {
        super(text, line, column, endColumn);
    }

    public String toString() {
        return String.format("Comment(\"%s\", %d, %d, %d)", type, line, column, endColumn);
    }
}