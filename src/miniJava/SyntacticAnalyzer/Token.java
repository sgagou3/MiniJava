package miniJava.SyntacticAnalyzer;

public class Token {
  TokenType type;
  String text;
  SourcePosition position;

  public Token(TokenType tokenType, String tokenText, SourcePosition sourcePosition) {
    type = tokenType;
    text = tokenText;
    position = sourcePosition;
  }

  public TokenType getTokenType() {
    return type;
  }

  public String getTokenText() {
    return text;
  }

  public SourcePosition getTokenPosition() {
    return position;
  }
}
