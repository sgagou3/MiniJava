package miniJava;

import miniJava.SyntacticAnalyzer.Parser;
import miniJava.SyntacticAnalyzer.Scanner;

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

        Scanner sc = new Scanner(in, reporter);
        Parser parser = new Parser(sc, reporter);
        parser.parse();

        if (reporter.isEmpty()) {
            System.out.println("Success");
        } else {
            System.out.println("Error");
            reporter.showErrorQueue();
        }
    }
}
