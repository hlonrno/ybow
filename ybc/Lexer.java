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

    public Optional<Token> getNextToken() throws LexerException, IOException {
        Token tok = new Token();
        tok.beginColumn = tok.endChar = column;
        tok.beginLine = tok.endLine = line;
        while (" \n\t\b\r/\f".indexOf(cc) > -1) {
            // parse // comments
            if (cc == '/') {
                advance();
                if (cc != '/' || eof) {
                    tok.type = TokenType.SpecialChar;
                    tok.value = "/";
                    return Optional.of(tok);
                }
                // completely ignore comments.
                while (!(cc == '\n' || eof)) {
                    advance();
                }
            }
            advance();
        }
        if (eof) {
            return Optional.empty();
        }
        
        if (cc == '`')
            return Optional.of(parseCharacterLiteral(tok));
        if (cc == '"')
            return Optional.of(parseStringLiteral(tok));
        if ("0123456789".indexOf(cc) > -1)
            return Optional.of(parseNumberLiteral(tok));
        if (SPECIAL_CHARS.indexOf(cc) > -1)
            return Optional.of(parseSpecialCharacter(tok));
        return Optional.of(parseIdentifier(tok));
    }

    private String parseStringEscapeSequence() throws LexerException, IOException {
        if (cc != '\\')
            return String.valueOf(cc);
        advance();
        StringBuilder str = new StringBuilder('\\');
        if ("ntbrfs\\\"\n".indexOf(cc) > -1) {
            str.append(cc);
        } else if ("xob0123456789".indexOf(cc) > -1) {
            String chars = switch (cc) {
                case 'b' -> "01";
                case 'o' -> "01234567";
                case 'x' -> "0123456789abcdefABCDEF";
                default -> "0123456789";
            };
            int count = 0;
            int maxcount = switch (cc) {
                case 'b' -> 8;
                case 'o' -> 4;
                case 'x' -> 2;
                default -> 2;  // 3 // 1st char doesn't get counted 
            };
            str.append(cc);
            advance();
            while (chars.indexOf(cc) > -1 && count++ < maxcount) {
                str.append(cc);
                advance();
            }
        } else {
            throw new StringParsingException("Unknown escape sequence.");
        }
        return str.toString();
    }

    private Token parseCharacterLiteral(Token tok) throws LexerException, IOException {
        tok.type = TokenType.CharacterLiteral;
        advance();
        tok.value = parseStringEscapeSequence();
        return tok;
    }

    private Token parseStringLiteral(Token tok) throws LexerException, IOException {
        tok.type = TokenType.StringLiteral;
        advance();
        StringBuilder str = new StringBuilder();
        while (!(cc == '"' || eof)) {
            str.append(parseStringEscapeSequence());
            advance();
        }
        advance();
        return tok;
    }

    private Token parseNumberLiteral(Token tok) throws IOException {
        tok.type = TokenType.NumberLiteral;
        return tok;
    }

    private Token parseSpecialCharacter(Token tok) throws IOException {
        tok.type = TokenType.SpecialChar;
        return tok;
    }

    private Token parseIdentifier(Token tok) throws IOException {
        tok.type = TokenType.Identifier;
        return tok;
    }
}
