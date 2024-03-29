package miniJava;

import miniJava.AbstractSyntaxTrees.AST;
import miniJava.AbstractSyntaxTrees.ASTDisplay;
import miniJava.SyntacticAnalyzer.Parser;
import miniJava.SyntacticAnalyzer.Scanner;
import miniJava.SyntacticAnalyzer.SourcePosition;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class Compiler {
  public static void main(String[] args) {
    ErrorReporter reporter = new ErrorReporter();

    if (args == null || args[0] == null) {
      throw new UnsupportedOperationException();
    }

    InputStream in = null;

    try {
      in = new FileInputStream(args[0]);
    } catch (FileNotFoundException e) {
      System.err.println(e.toString());
      System.exit(-1);
    }

    SourcePosition position = new SourcePosition(0, 0);

    Scanner sc = new Scanner(in, reporter, position);
    Parser parser = new Parser(sc, reporter);
    ASTDisplay display = new ASTDisplay();
    AST ast = parser.parse();

    parser.parse();

    if (reporter.isEmpty()) {
      display.showTree(ast);
    } else {
      System.out.println("Error");
      reporter.showErrorQueue();
    }
  }
}
