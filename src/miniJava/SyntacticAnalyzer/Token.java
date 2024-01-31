package miniJava.SyntacticAnalyzer;

public class Token {
    TokenType type;
    String text;

    public Token(TokenType tokenType, String tokenText) {
        type = tokenType;
        text = tokenText;
    }

    public TokenType getTokenType() {
        return type;
    }

    public String getTokenText() {
        return text;
    }

}
