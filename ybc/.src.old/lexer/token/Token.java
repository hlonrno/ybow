package src.lexer.token;

public class Token {
    public TokenType type;
    public int line;
    public int column;
    public String value;

    public Token() {}
    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
        line = column = -1;
    }


    @Override
    public String toString() {
        return String.format("Token %3d:%-3d %-16s %s",
            line, column, type.toString(), value);
    }
}
