package miniJava.SyntacticAnalyzer;

import miniJava.ErrorReporter;

import java.io.IOException;
import java.io.InputStream;

public class Scanner {
    InputStream in;
    ErrorReporter reporter;
    char currentChar;
    boolean eof = false;

    static final char eolUnix = '\n';
    static final char eolWindows = '\r';

    public Scanner(InputStream inputStream, ErrorReporter errorReporter) {
        in = inputStream;
        reporter = errorReporter;
        nextChar();
    }

    public Token scan() {
        while (isWhiteSpace()) {
            if (eof) {
                return new Token(TokenType.EOF, "");
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
                    return new Token(TokenType.CLASS, "class");
                case "void":
                    return new Token(TokenType.VOID, "void");
                case "public":
                    return new Token(TokenType.PUBLIC, "public");
                case "private":
                    return new Token(TokenType.PRIVATE, "private");
                case "static":
                    return new Token(TokenType.STATIC, "static");
                case "int":
                    return new Token(TokenType.INT, "int");
                case "boolean":
                    return new Token(TokenType.BOOLEAN, "boolean");
                case "this":
                    return new Token(TokenType.THIS, "this");
                case "true":
                    return new Token(TokenType.TRUE, "true");
                case "false":
                    return new Token(TokenType.FALSE, "false");
                case "new":
                    return new Token(TokenType.NEW, "new");
                case "return":
                    return new Token(TokenType.RETURN, "return");
                case "if":
                    return new Token(TokenType.IF, "if");
                case "else":
                    return new Token(TokenType.ELSE, "else");
                case "while":
                    return new Token(TokenType.WHILE, "while");
                default:
                    char firstChar = builder.charAt(0);
                    if (firstChar == '_' || ('0' <= firstChar && firstChar <= '9')) {
                        return new Token(TokenType.ERROR, "");
                    } else {
                        return new Token(TokenType.ID, builder.toString());
                    }
            }
        } else if (isDigit()) {
            while (isDigit()) {
                builder.append(currentChar);
                nextChar();
            }
            return new Token(TokenType.INT_LITERAL, builder.toString());
        } else {
            switch (currentChar) {
                case ';':
                    nextChar();
                    return new Token(TokenType.SEMICOLON, ";");
                case '.':
                    nextChar();
                    return new Token(TokenType.PERIOD, ".");
                case ',':
                    nextChar();
                    return new Token(TokenType.COMMA, ",");
                case '/':
                    nextChar();
                    if (currentChar == '/') {
                        while (currentChar != eolUnix && currentChar != eolWindows) {
                            if (eof) {
                                return new Token(TokenType.EOF, "");
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
                        return new Token(TokenType.ERROR, "");
                    } else {
                        nextChar();
                        return new Token(TokenType.OPERATOR, "/");
                    }
                case '+':
                    nextChar();
                    return new Token(TokenType.OPERATOR, "+");
                case '-':
                    nextChar();
                    return new Token(TokenType.OPERATOR, "-");
                case '*':
                    nextChar();
                    return new Token(TokenType.OPERATOR, "*");
                case '&':
                    nextChar();
                    if (currentChar == '&') {
                        nextChar();
                        return new Token(TokenType.OPERATOR, "&&");
                    }
                    return new Token(TokenType.ERROR, "");
                case '|':
                    nextChar();
                    if (currentChar == '|') {
                        nextChar();
                        return new Token(TokenType.OPERATOR, "||");
                    }
                    return new Token(TokenType.ERROR, "");
                case '!':
                    nextChar();
                    if (currentChar == '=') {
                        nextChar();
                        return new Token(TokenType.OPERATOR, "!=");
                    }
                    return new Token(TokenType.OPERATOR, "!");
                case '>':
                    nextChar();
                    if (currentChar == '=') {
                        nextChar();
                        return new Token(TokenType.OPERATOR, ">=");
                    }
                    return new Token(TokenType.OPERATOR, ">");
                case '<':
                    nextChar();
                    if (currentChar == '=') {
                        nextChar();
                        return new Token(TokenType.OPERATOR, "<=");
                    }
                    return new Token(TokenType.OPERATOR, "<");
                case '=':
                    nextChar();
                    if (currentChar == '=') {
                        nextChar();
                        return new Token(TokenType.OPERATOR, "==");
                    }
                    return new Token(TokenType.EQUAL, "=");
                case '{':
                    nextChar();
                    return new Token(TokenType.OPEN_BRACE, "{");
                case '}':
                    nextChar();
                    return new Token(TokenType.CLOSED_BRACE, "}");
                case '(':
                    nextChar();
                    return new Token(TokenType.OPEN_P, "(");
                case ')':
                    nextChar();
                    return new Token(TokenType.CLOSED_P, ")");
                case '[':
                    nextChar();
                    return new Token(TokenType.OPEN_BRACKET, "[");
                case ']':
                    nextChar();
                    return new Token(TokenType.CLOSED_BRACKET, "]");
                default:
                    if (eof) {
                        return new Token(TokenType.EOF, "");
                    }
                    return new Token(TokenType.ERROR, "");
            }
        }
    }

    private boolean isLetter() {
        return ('a' <= currentChar && currentChar <= 'z') || ('A' <= currentChar && currentChar <= 'Z') || currentChar == '_';
    }

    private boolean isDigit() {
        return '0' <= currentChar && currentChar <= '9';
    }

    private boolean isWhiteSpace() {
        return currentChar == ' ' | currentChar == eolUnix || currentChar == '\t' || currentChar == eolWindows;
    }

    private void nextChar() {
        try {
            int c = in.read();
            currentChar = (char) c;
            if (c == -1) {
                eof = true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
