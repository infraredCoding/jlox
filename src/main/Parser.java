package main;

import enums.TokenType;

import java.util.List;

import static enums.TokenType.*;

public class Parser {
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokenList;
    private int current = 0;

    Parser(List<Token> tokenList) {
        this.tokenList = tokenList;
    }

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression(){
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)){
            Token op = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, op, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
            Token op = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, op, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(PLUS, MINUS)){
            Token op = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, op, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(SLASH, STAR)){
            Token op = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, op, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)){
            Token op = previous();
            Expr right = unary();
            return new Expr.Unary(op, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING)){
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)){
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ) after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(), "Expect expression.");
    }

    private Token consume(TokenType tokenType, String msg) {
        if (check(tokenType)) return advance();

        throw error(peek(), msg);
    }

    private ParseError error(Token token, String msg) {
        Lox.error(token, msg);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
          if (previous().type == SEMICOLON) return;

          switch (peek().type) {
            case CLASS:
            case FUN:
            case VAR:
            case FOR:
            case IF:
            case WHILE:
            case PRINT:
            case RETURN:
              return;
          }

          advance();
        }
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token advance() {
        if(!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokenList.get(current);
    }

    private Token previous() {
        return tokenList.get(current-1);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }
}
