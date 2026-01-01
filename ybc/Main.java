import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

public class Main {
    public static void main(String[] args)
        throws LexerException, IOException, FileNotFoundException
    {
        File file = new File("main.yb");
        try (var fileIn = new FileReader(file)) {
            var reader = new BufferedReader(fileIn);
            var lexer = new Lexer(reader);
            Optional<Token> token;
            do {
                token = lexer.getNextToken();
                if (token.isEmpty())
                    break;
                System.out.println(token.get().toString());
            } while (true);
        } catch (LexerException e) {
            System.out.println("Parsing error: " + e.getMessage());
        } catch (Exception e) { throw e; } // yes.
    }
}
