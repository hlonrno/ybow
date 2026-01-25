package src;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import src.lexer.Lexer;
import src.parser.Parser;

public class Main {
    public static void main(String[] args) {
        File file = new File("main.yb");
        try (var fileReader = new FileReader(file)) {
            var lexer = new Lexer(new BufferedReader(fileReader));
            // var token = lexer.getNextToken();
            // while (token.isPresent()) {
            //     System.out.println(token.get());
            //     token = lexer.getNextToken();
            // }

            Parser parser = new Parser(lexer);
            System.out.println(parser.getAST());

            // Tsoding "Packing game assets into a binary file" 53:30
            //     Q("did people really trust slower programs more than fast ones")
        } catch (Exception e) {
            System.err.println("\33[91merror\33[0m: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
        }
    }
}
