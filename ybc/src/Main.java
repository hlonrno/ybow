package src;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Scanner;

import src.lexer.Lexer;

public class Main {
    static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Yb REPL");
        String in = "";
        while (!in.equals("q!")) {
            System.out.print("> ");
            in = sc.nextLine();
            // var parser = new Parser(new StringReader(in), "<stdin>");
            // System.out.println(parser.getAST());
            var lexer = new Lexer(new StringReader(in), "<stdin>");
            while (lexer.hasNext()) {
                var res = lexer.next();
                if (res.hasError())
                    System.err.println(res.getError());
                else
                    System.out.println(res.getValue());
            }
        }
        sc.close();
    }

    static void main1(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("USAGE: <cmd> main.yb");
            System.exit(1);
        }
        var main = new File(args[0]);
        if (!main.exists()) {
            System.err.println("file does not exist or could not be found");
            System.exit(1);
        }
        var reader = new FileReader(main);
        var bufReader = new BufferedReader(reader);
        var lexer = new Lexer(bufReader, main.getName());
        while (lexer.hasNext()) {
            var res = lexer.next();
            if (res.hasError())
                System.err.println(res.getError());
            else
                System.out.println(res.getValue());
        }
        reader.close();
    }
}
