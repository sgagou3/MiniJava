package miniJava.SyntacticAnalyzer;

public class ParseException extends Exception {

    public ParseException(){
        super();
    }
    public ParseException(String desc) {
        super(desc);
    }
}
