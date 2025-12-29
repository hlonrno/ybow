import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class Lexer {
    private final String SPECIAL_CHARS = "";
    private InputStream stream;
    private int column = 0;
    private int line = 1;
    private boolean eof = false;
    private char cc;

    public Lexer(InputStream stream) throws IOException {
        this.stream = stream;
        advance();
    }

    private void advance() throws IOException {
        int c = stream.read();
        if (c == -1) {
            eof = true;
            this.cc = 0;
            return;
        }
        if (cc == '\n') {
            line += 1;
            column = 0;
        }
        ++column;
        this.cc = (char)c;
    }

    public Optional<Token> getNextToken() throws IOException {
        while (" \n\t\b\r/".indexOf(cc) > -1) {
            if (cc == '/') {
                var tok = new Token();
                tok.beginColumn = tok.endChar = column;
                tok.beginLine = tok.endLine = line;
                advance();
                if (cc != '/' || eof) {
                    tok.type = TokenType.SpecialChar;
                    tok.value = "/";
                    return Optional.of(tok);
                }
            }
            advance();
        }
        if (eof) {
            return Optional.empty();
        }
        
        if (cc == '`')
            return Optional.of(parseCharacterLiteral());
        if (cc == '"')
            return Optional.of(parseStringLiteral());
        if ("0123456789".indexOf(cc) > -1)
            return Optional.of(parseNumberLiteral());
        if (SPECIAL_CHARS.indexOf(cc) > -1)
            return Optional.of(parseSpecialCharacter());
        return Optional.of(parseIdentifier());
    }

    private Token parseCharacterLiteral() {
        // CharacterLiteral;
        Token tok = new Token();
        return tok;
    }

    private Token parseStringLiteral() {
        // StringLiteral;
        Token tok = new Token();
        return tok;
    }

    private Token parseNumberLiteral() {
        // NumberLiteral;
        Token tok = new Token();
        return tok;
    }

    private Token parseSpecialCharacter() {
        // SpecialChar;
        Token tok = new Token();
        return tok;
    }

    private Token parseIdentifier() {
        // Identifier;
        Token tok = new Token();
        return tok;
    }
}
