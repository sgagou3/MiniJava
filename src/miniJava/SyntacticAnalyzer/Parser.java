package miniJava.SyntacticAnalyzer;

import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.ErrorReporter;

import java.util.Objects;

public class Parser {
  Scanner scanner;
  ErrorReporter reporter;
  Token token;

  boolean showPositionTrace = false;

  public Parser(Scanner newScanner, ErrorReporter errorReporter) {
    scanner = newScanner;
    reporter = errorReporter;
    token = scanner.scan();
  }

  private void debug(String debugMessage) {
    System.out.println("[DEBUG]: " + debugMessage + "@" + token.getTokenPosition());
  }

  public Package parse() {
    try {
      return parseProgram();
    } catch (ParseException e) {
      String trace = e.toString();
      if (showPositionTrace) {
        trace += "@" + token.getTokenPosition();
      }
      reporter.reportError(trace);
      return null;
    }
  }

  private Package parseProgram() throws ParseException {
    ClassDeclList classDeclList = new ClassDeclList();
    while (token.getTokenType() != TokenType.EOF && token.getTokenType() != TokenType.ERROR) {
      classDeclList.add(parseClassDeclaration());
      if (token.getTokenType() == TokenType.ERROR) {
        throw new ParseException();
      }
    }
    return new Package(classDeclList, token.getTokenPosition());
  }

  private ClassDecl parseClassDeclaration() throws ParseException {
    FieldDeclList fieldDeclList = new FieldDeclList();
    MethodDeclList methodDeclList = new MethodDeclList();
    acceptToken(TokenType.CLASS);
    String className = token.getTokenText();
    acceptToken(TokenType.ID);
    acceptToken(TokenType.OPEN_BRACE);
    while (!canAcceptToken(TokenType.CLOSED_BRACE)) {
      MemberDecl memberDecl = parseMemberDeclaration();
      if (memberDecl instanceof MethodDecl) {
        methodDeclList.add((MethodDecl) memberDecl);
      } else if (memberDecl instanceof FieldDecl) {
        fieldDeclList.add((FieldDecl) memberDecl);
      }
    }
    acceptToken(TokenType.CLOSED_BRACE);
    return new ClassDecl(className, fieldDeclList, methodDeclList, token.getTokenPosition());
  }

  private MemberDecl parseMemberDeclaration() throws ParseException {
    boolean isPrivate = parseVisibility();
    boolean isStatic = parseAccess();
    if (canAcceptToken(TokenType.VOID)) {
      acceptToken(TokenType.VOID);
      String memberId = token.getTokenText();
      acceptToken(TokenType.ID);
      ParameterDeclList parameterDeclList = parseOptionalParameterList();
      StatementList statementList = parseMethodDeclarationBody();
      return new MethodDecl(
          new FieldDecl(
              isPrivate,
              isStatic,
              new BaseType(TypeKind.VOID, token.getTokenPosition()),
              memberId,
              token.getTokenPosition()),
          parameterDeclList,
          statementList,
          token.getTokenPosition());
    } else {
      TypeDenoter typeDenoter = parseType();
      String memberId = token.getTokenText();
      acceptToken(TokenType.ID);
      if (canAcceptToken(TokenType.SEMICOLON)) {
        acceptToken(TokenType.SEMICOLON);
        return new FieldDecl(isPrivate, isStatic, typeDenoter, memberId, token.getTokenPosition());
      } else {
        ParameterDeclList parameterDeclList = parseOptionalParameterList();
        StatementList statementList = parseMethodDeclarationBody();
        return new MethodDecl(
            new FieldDecl(isPrivate, isStatic, typeDenoter, memberId, token.getTokenPosition()),
            parameterDeclList,
            statementList,
            token.getTokenPosition());
      }
    }
  }

  private boolean parseVisibility() throws ParseException {
    if (canAcceptToken(TokenType.PUBLIC)) {
      acceptToken(TokenType.PUBLIC);
      return false;
    } else if (canAcceptToken(TokenType.PRIVATE)) {
      acceptToken(TokenType.PRIVATE);
      return true;
    }
    return false;
  }

  private StatementList parseMethodDeclarationBody() throws ParseException {
    StatementList statementList = new StatementList();
    acceptToken(TokenType.OPEN_BRACE);
    while (!canAcceptToken(TokenType.CLOSED_BRACE)) {
      Statement statement = parseStatement();
      statementList.add(statement);
    }
    acceptToken(TokenType.CLOSED_BRACE);
    return statementList;
  }

  private Statement parseStatement() throws ParseException {
    if (canAcceptToken(TokenType.OPEN_BRACE)) {
      StatementList statementList = new StatementList();
      acceptToken(TokenType.OPEN_BRACE);
      while (!canAcceptToken(TokenType.CLOSED_BRACE)) {
        Statement statement = parseStatement();
        statementList.add(statement);
      }
      acceptToken(TokenType.CLOSED_BRACE);
      return new BlockStmt(statementList, token.getTokenPosition());
    } else if (canAcceptToken(TokenType.RETURN)) {
      acceptToken(TokenType.RETURN);
      Expression expression = null;
      if (!canAcceptToken(TokenType.SEMICOLON)) {
        expression = parseExpression();
      }
      acceptToken(TokenType.SEMICOLON);
      return new ReturnStmt(expression, token.getTokenPosition());
    } else if (canAcceptToken(TokenType.IF)) {
      acceptToken(TokenType.IF);
      acceptToken(TokenType.OPEN_P);
      Expression expression = parseExpression();
      acceptToken(TokenType.CLOSED_P);
      Statement ifStatement = parseStatement();
      if (canAcceptToken(TokenType.ELSE)) {
        acceptToken(TokenType.ELSE);
        Statement elseStatement = parseStatement();
        return new IfStmt(expression, ifStatement, elseStatement, token.getTokenPosition());
      }
      return new IfStmt(expression, ifStatement, token.getTokenPosition());
    } else if (canAcceptToken(TokenType.WHILE)) {
      acceptToken(TokenType.WHILE);
      acceptToken(TokenType.OPEN_P);
      Expression expression = parseExpression();
      acceptToken(TokenType.CLOSED_P);
      Statement statement = parseStatement();
      return new WhileStmt(expression, statement, token.getTokenPosition());
    } else if (canAcceptToken(TokenType.INT) || canAcceptToken(TokenType.BOOLEAN)) {
      TypeDenoter typeDenoter = parseType();
      String id = token.getTokenText();
      acceptToken(TokenType.ID);
      acceptToken(TokenType.EQUAL);
      Expression expression = parseExpression();
      acceptToken(TokenType.SEMICOLON);
      return new VarDeclStmt(
          new VarDecl(typeDenoter, id, token.getTokenPosition()),
          expression,
          token.getTokenPosition());
    } else if (canAcceptToken(TokenType.THIS)) {
      Reference reference = parseReference();
      return parseStatementBullShit(reference);
    } else if (canAcceptToken(TokenType.ID)) {
      IdRef idRef = new IdRef(new Identifier(token), token.getTokenPosition());
      Token currentToken =
          new Token(token.getTokenType(), token.getTokenText(), token.getTokenPosition());
      acceptToken(TokenType.ID);
      if (canAcceptToken(TokenType.PERIOD)) {
        QualRef qualRef = new QualRef(idRef, new Identifier(token), token.getTokenPosition());
        while (canAcceptToken(TokenType.PERIOD)) {
          acceptToken(TokenType.PERIOD);
          qualRef = new QualRef(idRef, new Identifier(token), token.getTokenPosition());
          acceptToken(TokenType.ID);
        }
        return parseStatementBullShit(qualRef);
      } else if (canAcceptToken(TokenType.OPEN_BRACKET)) {
        acceptToken(TokenType.OPEN_BRACKET);
        if (canAcceptToken(TokenType.CLOSED_BRACKET)) {
          acceptToken(TokenType.CLOSED_BRACKET);
          String id = token.getTokenText();
          acceptToken(TokenType.ID);
          acceptToken(TokenType.EQUAL);
          Expression expression = parseExpression();
          acceptToken(TokenType.SEMICOLON);
          return new VarDeclStmt(
              new VarDecl(
                  new ArrayType(
                      new ClassType(new Identifier(currentToken), token.getTokenPosition()),
                      token.getTokenPosition()),
                  id,
                  token.getTokenPosition()),
              expression,
              token.getTokenPosition());
        } else {
          Expression firstExpression = parseExpression();
          acceptToken(TokenType.CLOSED_BRACKET);
          acceptToken(TokenType.EQUAL);
          Expression secondExpression = parseExpression();
          acceptToken(TokenType.SEMICOLON);
          return new IxAssignStmt(
              idRef, firstExpression, secondExpression, token.getTokenPosition());
        }
      } else if (canAcceptToken(TokenType.EQUAL)) {
        acceptToken(TokenType.EQUAL);
        Expression expression = parseExpression();
        acceptToken(TokenType.SEMICOLON);
        return new AssignStmt(idRef, expression, token.getTokenPosition());
      } else if (canAcceptToken(TokenType.OPEN_P)) {
        ExprList exprList = parseOptionalArgumentList();
        acceptToken(TokenType.SEMICOLON);
        return new CallStmt(idRef, exprList, token.getTokenPosition());
      } else if (canAcceptToken(TokenType.ID)) {
        String id = token.getTokenText();
        acceptToken(TokenType.ID);
        acceptToken(TokenType.EQUAL);
        Expression expression = parseExpression();
        acceptToken(TokenType.SEMICOLON);
        return new VarDeclStmt(
            new VarDecl(
                new ClassType(new Identifier(currentToken), token.getTokenPosition()),
                id,
                token.getTokenPosition()),
            expression,
            token.getTokenPosition());
      } else {
        throw new ParseException();
      }
    } else {
      throw new ParseException();
    }
  }

  private Statement parseStatementBullShit(Reference reference) throws ParseException {
    if (canAcceptToken(TokenType.EQUAL)) {
      acceptToken(TokenType.EQUAL);
      Expression expression = parseExpression();
      acceptToken(TokenType.SEMICOLON);
      return new AssignStmt(reference, expression, token.getTokenPosition());
    } else if (canAcceptToken(TokenType.OPEN_BRACKET)) {
      acceptToken(TokenType.OPEN_BRACKET);
      Expression firstExpression = parseExpression();
      acceptToken(TokenType.CLOSED_BRACKET);
      acceptToken(TokenType.EQUAL);
      Expression secondExpression = parseExpression();
      acceptToken(TokenType.SEMICOLON);
      return new IxAssignStmt(
          reference, firstExpression, secondExpression, token.getTokenPosition());
    } else if (canAcceptToken(TokenType.OPEN_P)) {
      ExprList exprList = parseOptionalArgumentList();
      acceptToken(TokenType.SEMICOLON);
      return new CallStmt(reference, exprList, token.getTokenPosition());
    } else {
      throw new ParseException();
    }
  }

  private Expression parseExpression() throws ParseException {
    return parseDisjunction();
  }

  private Expression parseUnary() throws ParseException {
    if (canAcceptToken(TokenType.OPERATOR)
        && (Objects.equals(token.getTokenText(), "-")
            || Objects.equals(token.getTokenText(), "!"))) {
      Token currentToken =
          new Token(token.getTokenType(), token.getTokenText(), token.getTokenPosition());
      acceptToken(TokenType.OPERATOR);
      Expression nextUnary = parseUnary();
      return new UnaryExpr(new Operator(currentToken), nextUnary, token.getTokenPosition());
    } else {
      return parseNextExpression();
    }
  }

  private Expression parseMultiplicative() throws ParseException {
    Expression firstExpression = parseUnary();
    while (canAcceptToken(TokenType.OPERATOR)
        && (Objects.equals(token.getTokenText(), "/")
            || Objects.equals(token.getTokenText(), "*"))) {
      Token currentToken =
          new Token(token.getTokenType(), token.getTokenText(), token.getTokenPosition());
      acceptToken(TokenType.OPERATOR);
      Expression secondExpression = parseUnary();
      firstExpression =
          new BinaryExpr(
              new Operator(currentToken),
              firstExpression,
              secondExpression,
              token.getTokenPosition());
    }
    return firstExpression;
  }

  private Expression parseAdditive() throws ParseException {
    Expression firstExpression = parseMultiplicative();
    while (canAcceptToken(TokenType.OPERATOR)
        && (Objects.equals(token.getTokenText(), "+")
            || Objects.equals(token.getTokenText(), "-"))) {
      Token currentToken =
          new Token(token.getTokenType(), token.getTokenText(), token.getTokenPosition());
      acceptToken(TokenType.OPERATOR);
      Expression secondExpression = parseMultiplicative();
      firstExpression =
          new BinaryExpr(
              new Operator(currentToken),
              firstExpression,
              secondExpression,
              token.getTokenPosition());
    }
    return firstExpression;
  }

  private Expression parseRelational() throws ParseException {
    Expression firstExpression = parseAdditive();
    while (canAcceptToken(TokenType.OPERATOR)
        && (Objects.equals(token.getTokenText(), "<=")
            || Objects.equals(token.getTokenText(), ">=")
            || Objects.equals(token.getTokenText(), ">")
            || Objects.equals(token.getTokenText(), "<"))) {
      Token currentToken =
          new Token(token.getTokenType(), token.getTokenText(), token.getTokenPosition());
      acceptToken(TokenType.OPERATOR);
      Expression secondExpression = parseAdditive();
      firstExpression =
          new BinaryExpr(
              new Operator(currentToken),
              firstExpression,
              secondExpression,
              token.getTokenPosition());
    }
    return firstExpression;
  }

  private Expression parseEquality() throws ParseException {
    Expression firstExpression = parseRelational();
    while (canAcceptToken(TokenType.OPERATOR)
        && (Objects.equals(token.getTokenText(), "==")
            || Objects.equals(token.getTokenText(), "!="))) {
      Token currentToken =
          new Token(token.getTokenType(), token.getTokenText(), token.getTokenPosition());
      acceptToken(TokenType.OPERATOR);
      Expression secondExpression = parseRelational();
      firstExpression =
          new BinaryExpr(
              new Operator(currentToken),
              firstExpression,
              secondExpression,
              token.getTokenPosition());
    }
    return firstExpression;
  }

  private Expression parseConjunction() throws ParseException {
    Expression firstExpression = parseEquality();
    while (canAcceptToken(TokenType.OPERATOR) && Objects.equals(token.getTokenText(), "&&")) {
      Token currentToken =
          new Token(token.getTokenType(), token.getTokenText(), token.getTokenPosition());
      acceptToken(TokenType.OPERATOR);
      Expression secondExpression = parseEquality();
      firstExpression =
          new BinaryExpr(
              new Operator(currentToken),
              firstExpression,
              secondExpression,
              token.getTokenPosition());
    }
    return firstExpression;
  }

  private Expression parseDisjunction() throws ParseException {
    Expression firstExpression = parseConjunction();
    while (canAcceptToken(TokenType.OPERATOR) && Objects.equals(token.getTokenText(), "||")) {
      Token currentToken =
          new Token(token.getTokenType(), token.getTokenText(), token.getTokenPosition());
      acceptToken(TokenType.OPERATOR);
      Expression secondExpression = parseConjunction();
      firstExpression =
          new BinaryExpr(
              new Operator(currentToken),
              firstExpression,
              secondExpression,
              token.getTokenPosition());
    }
    return firstExpression;
  }

  private Expression parseNextExpression() throws ParseException {
    if (canAcceptToken(TokenType.NEW)) {
      acceptToken(TokenType.NEW);
      if (canAcceptToken(TokenType.ID)) {
        Token currentToken =
            new Token(token.getTokenType(), token.getTokenText(), token.getTokenPosition());
        acceptToken(TokenType.ID);
        if (canAcceptToken(TokenType.OPEN_P)) {
          acceptToken(TokenType.OPEN_P);
          acceptToken(TokenType.CLOSED_P);
          return new NewObjectExpr(
              new ClassType(new Identifier(currentToken), token.getTokenPosition()),
              token.getTokenPosition());
        } else if (canAcceptToken(TokenType.OPEN_BRACKET)) {
          acceptToken(TokenType.OPEN_BRACKET);
          Expression expression = parseExpression();
          acceptToken(TokenType.CLOSED_BRACKET);
          return new NewArrayExpr(
              new ClassType(new Identifier(currentToken), token.getTokenPosition()),
              expression,
              token.getTokenPosition());
        } else {
          throw new ParseException();
        }
      } else if (canAcceptToken(TokenType.INT)) {
        acceptToken(TokenType.INT);
        acceptToken(TokenType.OPEN_BRACKET);
        Expression expression = parseExpression();
        acceptToken(TokenType.CLOSED_BRACKET);
        return new NewArrayExpr(
            new BaseType(TypeKind.INT, token.getTokenPosition()),
            expression,
            token.getTokenPosition());
      }
    } else if (canAcceptToken(TokenType.INT_LITERAL)) {
      Token currentToken =
          new Token(token.getTokenType(), token.getTokenText(), token.getTokenPosition());
      acceptToken(TokenType.INT_LITERAL);
      return new LiteralExpr(new IntLiteral(currentToken), token.getTokenPosition());
    } else if (canAcceptToken(TokenType.TRUE)) {
      Token currentToken =
          new Token(token.getTokenType(), token.getTokenText(), token.getTokenPosition());
      acceptToken(TokenType.TRUE);
      return new LiteralExpr(new BooleanLiteral(currentToken), token.getTokenPosition());
    } else if (canAcceptToken(TokenType.FALSE)) {
      Token currentToken =
          new Token(token.getTokenType(), token.getTokenText(), token.getTokenPosition());
      acceptToken(TokenType.FALSE);
      return new LiteralExpr(new BooleanLiteral(currentToken), token.getTokenPosition());
    } else if (canAcceptToken(TokenType.OPEN_P)) {
      acceptToken(TokenType.OPEN_P);
      Expression expression = parseExpression();
      acceptToken(TokenType.CLOSED_P);
      return expression;
    } else if (canAcceptToken(TokenType.THIS) || canAcceptToken(TokenType.ID)) {
      Reference reference = parseReference();
      if (canAcceptToken(TokenType.OPEN_BRACKET)) {
        acceptToken(TokenType.OPEN_BRACKET);
        Expression expression = parseExpression();
        acceptToken(TokenType.CLOSED_BRACKET);
        return new IxExpr(reference, expression, token.getTokenPosition());
      } else if (canAcceptToken(TokenType.OPEN_P)) {
        ExprList exprList = parseOptionalArgumentList();
        return new CallExpr(reference, exprList, token.getTokenPosition());
      } else {
        return new RefExpr(reference, token.getTokenPosition());
      }
    } else {
      throw new ParseException();
    }
    return null;
  }

  private ParameterDeclList parseOptionalParameterList() throws ParseException {
    ParameterDeclList parameterDeclList = new ParameterDeclList();
    acceptToken(TokenType.OPEN_P);
    while (!canAcceptToken(TokenType.CLOSED_P)) {
      TypeDenoter typeDenoter = parseType();
      String memberName = token.getTokenText();
      acceptToken(TokenType.ID);
      parameterDeclList.add(new ParameterDecl(typeDenoter, memberName, token.getTokenPosition()));
      while (canAcceptToken(TokenType.COMMA)) {
        acceptToken(TokenType.COMMA);
        TypeDenoter nextTypeDenoter = parseType();
        String nextMemberName = token.getTokenText();
        parameterDeclList.add(
            new ParameterDecl(nextTypeDenoter, nextMemberName, token.getTokenPosition()));
        acceptToken(TokenType.ID);
      }
    }
    acceptToken(TokenType.CLOSED_P);
    return parameterDeclList;
  }

  private ExprList parseOptionalArgumentList() throws ParseException {
    ExprList exprList = new ExprList();
    acceptToken(TokenType.OPEN_P);
    while (!canAcceptToken(TokenType.CLOSED_P)) {
      Expression expression = parseExpression();
      exprList.add(expression);
      while (canAcceptToken(TokenType.COMMA)) {
        acceptToken(TokenType.COMMA);
        Expression additionalExpression = parseExpression();
        exprList.add(additionalExpression);
      }
    }
    acceptToken(TokenType.CLOSED_P);
    return exprList;
  }

  private Reference parseReference() throws ParseException {
    Reference reference = null;
    if (canAcceptToken(TokenType.ID)) {
      reference = new IdRef(new Identifier(token), token.getTokenPosition());
      acceptToken(TokenType.ID);
    } else if (canAcceptToken(TokenType.THIS)) {
      reference = new ThisRef(token.getTokenPosition());
      acceptToken(TokenType.THIS);
    }

    while (canAcceptToken(TokenType.PERIOD)) {
      acceptToken(TokenType.PERIOD);
      reference = new QualRef(reference, new Identifier(token), token.getTokenPosition());
      acceptToken(TokenType.ID);
    }
    return reference;
  }

  private TypeDenoter parseType() throws ParseException {
    if (canAcceptToken(TokenType.INT)) {
      acceptToken(TokenType.INT);
      if (canAcceptToken(TokenType.OPEN_BRACKET)) {
        acceptToken(TokenType.OPEN_BRACKET);
        acceptToken(TokenType.CLOSED_BRACKET);
        return new ArrayType(
            new BaseType(TypeKind.INT, token.getTokenPosition()), token.getTokenPosition());
      }
      return new BaseType(TypeKind.INT, token.getTokenPosition());
    } else if (canAcceptToken(TokenType.BOOLEAN)) {
      acceptToken(TokenType.BOOLEAN);
      return new BaseType(TypeKind.BOOLEAN, token.getTokenPosition());
    } else if (canAcceptToken(TokenType.ID)) {
      Token currentToken =
          new Token(token.getTokenType(), token.getTokenText(), token.getTokenPosition());
      acceptToken(TokenType.ID);
      if (canAcceptToken(TokenType.OPEN_BRACKET)) {
        acceptToken(TokenType.OPEN_BRACKET);
        acceptToken(TokenType.CLOSED_BRACKET);
        return new ArrayType(
            new ClassType(new Identifier(currentToken), token.getTokenPosition()),
            token.getTokenPosition());
      }
      return new ClassType(new Identifier(currentToken), token.getTokenPosition());
    } else {
      throw new ParseException();
    }
  }

  private boolean parseAccess() throws ParseException {
    if (canAcceptToken(TokenType.STATIC)) {
      acceptToken(TokenType.STATIC);
      return true;
    }
    return false;
  }

  private boolean canAcceptToken(TokenType expectedType) {
    return token.getTokenType() == expectedType;
  }

  private void acceptToken(TokenType expectedType) throws ParseException {
    if (canAcceptToken(expectedType)) {
      token = scanner.scan();
    } else {
      throw new ParseException(
          String.format("expected %s but got %s", expectedType, token.getTokenType()));
    }
  }
}
