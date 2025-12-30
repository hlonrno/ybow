import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args)
        throws LexerException, IOException, FileNotFoundException
    {
        File file = new File("../main.yb");
        System.out.println(file.exists());

    }
}
