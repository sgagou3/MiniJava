package miniJava.SyntacticAnalyzer;

public class SourcePosition {
    int row, col;

    public SourcePosition(int r, int c) {
        row = r;
        col = c;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void incrementRow() {
        row += 1;
    }

    public void incrementColumn() {
        col += 1;
    }

    public String toString() {
        return "position is " + row + ", " + col;
    }
}
