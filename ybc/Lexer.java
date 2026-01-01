import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

public class Lexer {
    private static final String SPECIAL_CHARS = "()[]{}.,+-*/%&|~!:=;<>";
    private static final String WHITESPACE_CHARS = " \n\t\r\b\f";
    private static final String IDENTIFIER_CHARS = SPECIAL_CHARS + WHITESPACE_CHARS + "\"`";
    private Reader stream;
    private int column = 0;
    private int line = 1;
    private boolean eof = false;
    private char cc;

    public Lexer(Reader stream) throws IOException {
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
        tok.beginColumn = tok.endColumn = column;
        tok.beginLine = tok.endLine = line;
        while (WHITESPACE_CHARS.indexOf(cc) > -1 || cc == '/') {
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
            parseCharacterLiteral(tok);
        else if (cc == '"')
            parseStringLiteral(tok);
        else if ("0123456789".indexOf(cc) > -1)
            parseNumberLiteral(tok);
        else if (SPECIAL_CHARS.indexOf(cc) > -1)
            parseSpecialCharacter(tok);
        else
            parseIdentifier(tok);
        tok.endLine = line;
        tok.endColumn = Math.max(1, column - 1);
        return Optional.of(tok);
    }

    private String parseStringEscapeSequence() throws LexerException, IOException {
        if (cc != '\\')
            return String.valueOf(cc);
        advance();
        StringBuilder str = new StringBuilder('\\');
        str.append('\\');
        if ("ntbrfs\\\"\n".indexOf(cc) > -1) {
            str.append(cc);
        } else if ("xob0123456789".indexOf(cc) > -1) {
            String chars = switch (cc) {
                case 'b' -> "01";
                case 'o' -> "01234567";
                case 'x' -> "0123456789abcdefABCDEF";
                default -> "0123456789";
            };
            int maxcount = switch (cc) {
                case 'b' -> 8;
                case 'o' -> 4;
                case 'x' -> 2;
                default -> 3;
            };
            str.append(cc);
            advance();
            for (int i = 0; i < maxcount && chars.indexOf(cc) > -1; i++) {
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
        advance();
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
        tok.value = str.toString();
        return tok;
    }

    private Token parseNumberLiteral(Token tok) throws IOException {
        tok.type = TokenType.NumberLiteral;
        byte dotCount = 0;
        StringBuilder str = new StringBuilder();
        // TODO: eg. 1e-5, 0x1b, 0o34, 0b11110
        while ("0123456789.".indexOf(cc) > -1) {
            if (cc == '.') {
                dotCount++;
                if (dotCount > 1)
                    break;
            }
            str.append(cc);
            advance();
        }
        tok.value = str.toString();
        return tok;
    }

    private Token parseSpecialCharacter(Token tok) throws IOException {
        tok.type = TokenType.SpecialChar;
        StringBuilder str = new StringBuilder();
        while (!(SPECIAL_CHARS.indexOf(cc) < 0 || eof)) {
            str.append(cc);
            advance();
        }
        tok.value = str.toString();
        return tok;
    }

    private Token parseIdentifier(Token tok) throws IOException {
        tok.type = TokenType.Identifier;
        StringBuilder str = new StringBuilder();
        while (!(IDENTIFIER_CHARS.indexOf(cc) > -1 || eof)) {
            str.append(cc);
            advance();
        }
        tok.value = str.toString();
        return tok;
    }

    private boolean isValidOperator(StringBuilder str) {
        if ("()[]{}.,".indexOf(str.charAt(0)) > -1)
            return true;
        return false;
    }
}
