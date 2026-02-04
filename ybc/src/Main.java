package src;

import java.io.StringReader;
import java.util.Scanner;
import src.lexer.Lexer;
import src.parser.Parser;

public class Main {
    static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Yb REPL");
        String in = "";
        while (!in.equals("q!")) {
            System.out.print("> ");
            in = sc.nextLine();
            try {
                var res = new Parser(new StringReader(in), "<stdin>")
                    .parseProgram();
                if (res.hasError())
                    res.getError().printStackTrace();
                    // System.err.println(res.getError());
                else
                    System.out.println(res.getValue());
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
            /*var lexer = new Lexer(new StringReader(in), "<stdin>");
            while (lexer.hasNext()) {
                var res = lexer.next();
                if (res.hasError())
                    System.err.println(res.getError());
                else
                    System.out.println(res.getValue());
            }*/
        }
        sc.close();
    }
}
