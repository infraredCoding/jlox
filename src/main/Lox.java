package main;

import enums.TokenType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    static boolean hadError = false;
    static boolean hadRuntimeError = false;

    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        }else if (args.length == 1) {
            runFile(args[0]);
        }else {
            runPrompt();
        }
        System.out.println("Hello world!");
    }

    private static void runFile(String fileName) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(fileName));
        run(new String(bytes, Charset.defaultCharset()));

        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }


    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static void run(String src) {
        JScanner sc = new JScanner(src);
        List<Token> tokens = sc.scanTokens();

        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        // Stop if there was a syntax error.
        if (hadError) return;

        interpreter.interpret(expression);
    }

    static void error(int line, String msg){
        report(line, "", msg);
    }

    static void error(Token token, String msg){
        if (token.type == TokenType.EOF){
            report(token.line, " at end", msg);
        }else{
            report(token.line, " at '" + token.lexeme + "'", msg);
        }
    }

    private static void report(int line, String where, String msg) {
        System.err.println("[line " + line + "] Error" + where + ": " + msg);
        hadError = true;
    }

    static void runtimeError(RuntimeError e){
        System.err.println(e.getMessage() + "\n[line " + e.token.line + "]");
        hadRuntimeError = true;
    }
}