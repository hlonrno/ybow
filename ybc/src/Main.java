package src;

import java.io.StringReader;
import java.util.Scanner;
import src.lexer.Lexer;
import src.parser.Parser;

public class Main {
    static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Yb REPL");
        try {
            String in = "";
            while (!in.equals("q!")) {
                System.out.print("> ");
                in = sc.nextLine();
                var res = new Parser(new StringReader(in), "<stdin>")
                    .parseProgram();
                if (res.hasError()) {
                    System.err.println("E " + res.getError());
                    res.getError().printStackTrace();
                } else
                    System.out.println("V " + res.getValue());
                /*var lexer = new Lexer(new StringReader(in), "<stdin>");
                while (lexer.hasNext()) {
                    var res = lexer.next();
                    if (res.hasError())
                        System.err.println(res.getError());
                    else
                        System.out.println(res.getValue());
                }*/
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            sc.close();
        }
    }
}
