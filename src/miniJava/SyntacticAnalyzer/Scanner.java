package miniJava.SyntacticAnalyzer;

import miniJava.ErrorReporter;

import java.io.IOException;
import java.io.InputStream;

public class Scanner {
  InputStream in;
  ErrorReporter reporter;
  char currentChar;
  boolean eof = false;
  SourcePosition position;

  static final char eolUnix = '\n';
  static final char eolWindows = '\r';

  public Scanner(
      InputStream inputStream, ErrorReporter errorReporter, SourcePosition sourcePosition) {
    in = inputStream;
    reporter = errorReporter;
    position = sourcePosition;
    nextChar();
  }

  public Token scan() {
    while (isWhiteSpace()) {
      if (eof) {
        return new Token(TokenType.EOF, "", position);
      }
      nextChar();
    }
    StringBuilder builder = new StringBuilder();
    if (isLetter()) {
      while (isLetter() || isDigit()) {
        builder.append(currentChar);
        nextChar();
      }
      switch (builder.toString()) {
        case "class":
          return new Token(TokenType.CLASS, "class", position);
        case "void":
          return new Token(TokenType.VOID, "void", position);
        case "public":
          return new Token(TokenType.PUBLIC, "public", position);
        case "private":
          return new Token(TokenType.PRIVATE, "private", position);
        case "static":
          return new Token(TokenType.STATIC, "static", position);
        case "int":
          return new Token(TokenType.INT, "int", position);
        case "boolean":
          return new Token(TokenType.BOOLEAN, "boolean", position);
        case "this":
          return new Token(TokenType.THIS, "this", position);
        case "true":
          return new Token(TokenType.TRUE, "true", position);
        case "false":
          return new Token(TokenType.FALSE, "false", position);
        case "new":
          return new Token(TokenType.NEW, "new", position);
        case "return":
          return new Token(TokenType.RETURN, "return", position);
        case "if":
          return new Token(TokenType.IF, "if", position);
        case "else":
          return new Token(TokenType.ELSE, "else", position);
        case "while":
          return new Token(TokenType.WHILE, "while", position);
        default:
          char firstChar = builder.charAt(0);
          if (firstChar == '_' || ('0' <= firstChar && firstChar <= '9')) {
            return new Token(TokenType.ERROR, "", position);
          } else {
            return new Token(TokenType.ID, builder.toString(), position);
          }
      }
    } else if (isDigit()) {
      while (isDigit()) {
        builder.append(currentChar);
        nextChar();
      }
      return new Token(TokenType.INT_LITERAL, builder.toString(), position);
    } else {
      switch (currentChar) {
        case ';':
          nextChar();
          return new Token(TokenType.SEMICOLON, ";", position);
        case '.':
          nextChar();
          return new Token(TokenType.PERIOD, ".", position);
        case ',':
          nextChar();
          return new Token(TokenType.COMMA, ",", position);
        case '/':
          nextChar();
          if (currentChar == '/') {
            while (currentChar != eolUnix && currentChar != eolWindows) {
              if (eof) {
                return new Token(TokenType.EOF, "", position);
              }
              nextChar();
            }
            return scan();
          } else if (currentChar == '*') {
            nextChar();
            while (!eof) {
              if (currentChar == '*') {
                nextChar();
                if (currentChar == '/') {
                  nextChar();
                  return scan();
                }
              } else {
                nextChar();
              }
            }
            return new Token(TokenType.ERROR, "", position);
          } else {
            return new Token(TokenType.OPERATOR, "/", position);
          }
        case '+':
          nextChar();
          return new Token(TokenType.OPERATOR, "+", position);
        case '-':
          nextChar();
          return new Token(TokenType.OPERATOR, "-", position);
        case '*':
          nextChar();
          return new Token(TokenType.OPERATOR, "*", position);
        case '&':
          nextChar();
          if (currentChar == '&') {
            nextChar();
            return new Token(TokenType.OPERATOR, "&&", position);
          }
          return new Token(TokenType.ERROR, "", position);
        case '|':
          nextChar();
          if (currentChar == '|') {
            nextChar();
            return new Token(TokenType.OPERATOR, "||", position);
          }
          return new Token(TokenType.ERROR, "", position);
        case '!':
          nextChar();
          if (currentChar == '=') {
            nextChar();
            return new Token(TokenType.OPERATOR, "!=", position);
          }
          return new Token(TokenType.OPERATOR, "!", position);
        case '>':
          nextChar();
          if (currentChar == '=') {
            nextChar();
            return new Token(TokenType.OPERATOR, ">=", position);
          }
          return new Token(TokenType.OPERATOR, ">", position);
        case '<':
          nextChar();
          if (currentChar == '=') {
            nextChar();
            return new Token(TokenType.OPERATOR, "<=", position);
          }
          return new Token(TokenType.OPERATOR, "<", position);
        case '=':
          nextChar();
          if (currentChar == '=') {
            nextChar();
            return new Token(TokenType.OPERATOR, "==", position);
          }
          return new Token(TokenType.EQUAL, "=", position);
        case '{':
          nextChar();
          return new Token(TokenType.OPEN_BRACE, "{", position);
        case '}':
          nextChar();
          return new Token(TokenType.CLOSED_BRACE, "}", position);
        case '(':
          nextChar();
          return new Token(TokenType.OPEN_P, "(", position);
        case ')':
          nextChar();
          return new Token(TokenType.CLOSED_P, ")", position);
        case '[':
          nextChar();
          return new Token(TokenType.OPEN_BRACKET, "[", position);
        case ']':
          nextChar();
          return new Token(TokenType.CLOSED_BRACKET, "]", position);
        default:
          if (eof) {
            return new Token(TokenType.EOF, "", position);
          }
          return new Token(TokenType.ERROR, "", position);
      }
    }
  }

  private boolean isLetter() {
    return ('a' <= currentChar && currentChar <= 'z')
        || ('A' <= currentChar && currentChar <= 'Z')
        || currentChar == '_';
  }

  private boolean isDigit() {
    return '0' <= currentChar && currentChar <= '9';
  }

  private boolean isWhiteSpace() {
    return currentChar == ' ' | currentChar == eolUnix
        || currentChar == '\t'
        || currentChar == eolWindows;
  }

  private void nextChar() {
    try {
      int c = in.read();
      currentChar = (char) c;
      if (c == -1) {
        eof = true;
      }

      if (c == '\n') {
        position.incrementRow();
      }
      position.incrementRow();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
