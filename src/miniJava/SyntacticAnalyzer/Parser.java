package miniJava.SyntacticAnalyzer;

import miniJava.ErrorReporter;

import java.util.Objects;

import static miniJava.SyntacticAnalyzer.TokenType.ERROR;

public class Parser {
    Scanner scanner;
    ErrorReporter reporter;
    Token token;

    public Parser(Scanner newScanner, ErrorReporter errorReporter) {
        scanner = newScanner;
        reporter = errorReporter;
        token = scanner.scan();
    }

    public void parse() {
        try {
            parseProgram();
        } catch (ParseException e) {
            reporter.reportError(e.toString());
        }
    }

    private void parseProgram() throws ParseException {
        while (token.getTokenType() != TokenType.EOF && token.getTokenType() != ERROR) {
            parseClassDeclaration();
            if (token.getTokenType() == ERROR) {
                throw new ParseException();
            }
        }
    }

    private void parseClassDeclaration() throws ParseException {
        acceptToken(TokenType.CLASS);
        acceptToken(TokenType.ID);
        acceptToken(TokenType.OPEN_BRACE);
        while (!canAcceptToken(TokenType.CLOSED_BRACE)) {
            parseMemberDeclaration();
        }
        acceptToken(TokenType.CLOSED_BRACE);
    }

    private void parseMemberDeclaration() throws ParseException {
        parseVisibility();
        parseAccess();
        if (canAcceptToken(TokenType.VOID)) {
            acceptToken(TokenType.VOID);
            acceptToken(TokenType.ID);
            parseRemainingMethodDeclaration();
        } else {
            parseType();
            acceptToken(TokenType.ID);
            if (canAcceptToken(TokenType.SEMICOLON)) {
                acceptToken(TokenType.SEMICOLON);
            } else {
                parseRemainingMethodDeclaration();
            }
        }
    }

    private void parseVisibility() throws ParseException {
        if (canAcceptToken(TokenType.PUBLIC)) {
            acceptToken(TokenType.PUBLIC);
        } else if (canAcceptToken(TokenType.PRIVATE)) {
            acceptToken(TokenType.PRIVATE);
        }
    }

    private void parseRemainingMethodDeclaration() throws ParseException {
        parseOptionalParameterList();
        acceptToken(TokenType.OPEN_BRACE);
        while (!canAcceptToken(TokenType.CLOSED_BRACE)) {
            parseStatement();
        }
        acceptToken(TokenType.CLOSED_BRACE);
    }

    private void parseStatement() throws ParseException {
        if (canAcceptToken(TokenType.OPEN_BRACE)) {
            acceptToken(TokenType.OPEN_BRACE);
            while (!canAcceptToken(TokenType.CLOSED_BRACE)) {
                parseStatement();
            }
            acceptToken(TokenType.CLOSED_BRACE);
        } else if (canAcceptToken(TokenType.RETURN)) {
            acceptToken(TokenType.RETURN);
            if (!canAcceptToken(TokenType.SEMICOLON)) {
                parseExpression();
            }
            acceptToken(TokenType.SEMICOLON);
        } else if (canAcceptToken(TokenType.IF)) {
            acceptToken(TokenType.IF);
            acceptToken(TokenType.OPEN_P);
            parseExpression();
            acceptToken(TokenType.CLOSED_P);
            parseStatement();
            if (canAcceptToken(TokenType.ELSE)) {
                acceptToken(TokenType.ELSE);
                parseStatement();
            }
        } else if (canAcceptToken(TokenType.WHILE)) {
            acceptToken(TokenType.WHILE);
            acceptToken(TokenType.OPEN_P);
            parseExpression();
            acceptToken(TokenType.CLOSED_P);
            parseStatement();
        } else if (canAcceptToken(TokenType.INT) || canAcceptToken(TokenType.BOOLEAN)) {
            parseType();
            acceptToken(TokenType.ID);
            acceptToken(TokenType.EQUAL);
            parseExpression();
            acceptToken(TokenType.SEMICOLON);
        } else if (canAcceptToken(TokenType.THIS)) {
            parseReference();
            parseStatementBullShit();
        } else if (canAcceptToken(TokenType.ID)) {
            // Next token must be ID
            acceptToken(TokenType.ID);

            if (canAcceptToken(TokenType.PERIOD)) {
                // Reference = id.*, then parse statement bullshit
                while (canAcceptToken(TokenType.PERIOD)) {
                    acceptToken(TokenType.PERIOD);
                    acceptToken(TokenType.ID);
                }
                parseStatementBullShit();
            } else if (canAcceptToken(TokenType.OPEN_BRACKET)) {
                acceptToken(TokenType.OPEN_BRACKET);
                if (canAcceptToken(TokenType.CLOSED_BRACKET)) {
                    // Type = id[]
                    acceptToken(TokenType.CLOSED_BRACKET);
                    acceptToken(TokenType.ID);
                    acceptToken(TokenType.EQUAL);
                    parseExpression();
                    acceptToken(TokenType.SEMICOLON);
                } else {
                    //Reference = id[Expression]
                    parseExpression();
                    acceptToken(TokenType.CLOSED_BRACKET);
                    acceptToken(TokenType.EQUAL);
                    parseExpression();
                    acceptToken(TokenType.SEMICOLON);
                }
            } else if (canAcceptToken(TokenType.EQUAL)) {
                //Reference = id
                acceptToken(TokenType.EQUAL);
                parseExpression();
                acceptToken(TokenType.SEMICOLON);
            } else if (canAcceptToken(TokenType.OPEN_P)) {
                parseOptionalArgumentList();
                acceptToken(TokenType.SEMICOLON);
            } else if (canAcceptToken(TokenType.ID)) {
                acceptToken(TokenType.ID);
                acceptToken(TokenType.EQUAL);
                parseExpression();
                acceptToken(TokenType.SEMICOLON);
            }else{
                throw new ParseException();
            }
        } else {
            throw new ParseException();
        }
    }

    private void parseStatementBullShit() throws ParseException {
        if (canAcceptToken(TokenType.EQUAL)) {
            acceptToken(TokenType.EQUAL);
            parseExpression();
            acceptToken(TokenType.SEMICOLON);
        } else if (canAcceptToken(TokenType.OPEN_BRACKET)) {
            acceptToken(TokenType.OPEN_BRACKET);
            parseExpression();
            acceptToken(TokenType.CLOSED_BRACKET);
            acceptToken(TokenType.EQUAL);
            parseExpression();
            acceptToken(TokenType.SEMICOLON);
        } else if (canAcceptToken(TokenType.OPEN_P)) {
            parseOptionalArgumentList();
            acceptToken(TokenType.SEMICOLON);
        } else {
	    throw new ParseException();
	}
    }

    private void parseExpression() throws ParseException {
        parseNextExpression();
        if (canAcceptToken(TokenType.OPERATOR) && !Objects.equals(token.getTokenText(), "!")) {
            acceptToken(TokenType.OPERATOR);
            parseExpression();
        }
    }

    private void parseNextExpression() throws ParseException {
        if (canAcceptToken(TokenType.NEW)) {
            acceptToken(TokenType.NEW);
            if (canAcceptToken(TokenType.ID)) {
                acceptToken(TokenType.ID);
                if (canAcceptToken(TokenType.OPEN_P)) {
                    acceptToken(TokenType.OPEN_P);
                    acceptToken(TokenType.CLOSED_P);
                } else if (canAcceptToken(TokenType.OPEN_BRACKET)) {
                    acceptToken(TokenType.OPEN_BRACKET);
                    parseExpression();
                    acceptToken(TokenType.CLOSED_BRACKET);
                }
            } else if (canAcceptToken(TokenType.INT)) {
                acceptToken(TokenType.INT);
                acceptToken(TokenType.OPEN_BRACKET);
                parseExpression();
                acceptToken(TokenType.CLOSED_BRACKET);
            }
        } else if (canAcceptToken(TokenType.INT_LITERAL)) {
            acceptToken(TokenType.INT_LITERAL);
        } else if (canAcceptToken(TokenType.TRUE)) {
            acceptToken(TokenType.TRUE);
        } else if (canAcceptToken(TokenType.FALSE)) {
            acceptToken(TokenType.FALSE);
        } else if (Objects.equals(token.getTokenText(), "-") || Objects.equals(token.getTokenText(), "!")) {
            acceptToken(TokenType.OPERATOR);
            parseExpression();
        } else if (canAcceptToken(TokenType.OPEN_P)) {
            acceptToken(TokenType.OPEN_P);
            parseExpression();
            acceptToken(TokenType.CLOSED_P);
        } else if (canAcceptToken(TokenType.THIS) || canAcceptToken(TokenType.ID)) {
            parseReference();
            if (canAcceptToken(TokenType.OPEN_BRACKET)) {
                acceptToken(TokenType.OPEN_BRACKET);
                parseExpression();
                acceptToken(TokenType.CLOSED_BRACKET);
            } else if (canAcceptToken(TokenType.OPEN_P)) {
                parseOptionalArgumentList();
            }
        } else {
            throw new ParseException();
        }
    }

    private void parseOptionalParameterList() throws ParseException {
        acceptToken(TokenType.OPEN_P);
        while (!canAcceptToken(TokenType.CLOSED_P)) {
            parseType();
            acceptToken(TokenType.ID);
            while (canAcceptToken(TokenType.COMMA)) {
                acceptToken(TokenType.COMMA);
                parseType();
                acceptToken(TokenType.ID);
            }
        }
        acceptToken(TokenType.CLOSED_P);
    }

    private void parseOptionalArgumentList() throws ParseException {
        acceptToken(TokenType.OPEN_P);
        while (!canAcceptToken(TokenType.CLOSED_P)) {
            parseExpression();
            while (canAcceptToken(TokenType.COMMA)) {
                acceptToken(TokenType.COMMA);
                parseExpression();
            }
        }
        acceptToken(TokenType.CLOSED_P);
    }

    private void parseReference() throws ParseException {
        if (canAcceptToken(TokenType.ID)) {
            acceptToken(TokenType.ID);
        } else if (canAcceptToken(TokenType.THIS)) {
            acceptToken(TokenType.THIS);
        } else {
            throw new ParseException();
        }
        while (canAcceptToken(TokenType.PERIOD)) {
            acceptToken(TokenType.PERIOD);
            acceptToken(TokenType.ID);
        }
    }

    private void parseType() throws ParseException {
        if (canAcceptToken(TokenType.INT)) {
            acceptToken(TokenType.INT);
            if (canAcceptToken(TokenType.OPEN_BRACKET)) {
                acceptToken(TokenType.OPEN_BRACKET);
                acceptToken(TokenType.CLOSED_BRACKET);
            }
        } else if (canAcceptToken(TokenType.BOOLEAN)) {
            acceptToken(TokenType.BOOLEAN);
        } else if (canAcceptToken(TokenType.ID)) {
            acceptToken(TokenType.ID);
            if (canAcceptToken(TokenType.OPEN_BRACKET)) {
                acceptToken(TokenType.OPEN_BRACKET);
                acceptToken(TokenType.CLOSED_BRACKET);
            }
        } else {
            throw new ParseException();
        }
    }

    private void parseAccess() throws ParseException {
        if (canAcceptToken(TokenType.STATIC)) {
            acceptToken(TokenType.STATIC);
        }
    }

    private boolean canAcceptToken(TokenType expectedType) {
        return token.getTokenType() == expectedType;
    }

    private void acceptToken(TokenType expectedType) throws ParseException {
        if (canAcceptToken(expectedType)) {
            token = scanner.scan();
        } else {
            throw new ParseException(String.format("expected %s but got %s", expectedType, token.getTokenType()));
        }
    }
}
