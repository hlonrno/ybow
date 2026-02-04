package src.lexer;

public class Token {
    protected TokenType type;
    protected String image;
    protected int line, column;

    protected Token() {}

    public TokenType getType() { return type; }
    public int getLine() { return line; }
    public int getColumn() { return column; }
    public String getImage() { return image; }

    public Token cloneToken() {
        Token tok = new Token();
        tok.type = type;
        tok.image = image;
        tok.line = line;
        tok.column = column;
        return tok;
    }

    @Override
    public String toString() {
        return "Token[%s:%d:%d: %s]"
            .formatted(type.toString(), line, column, image);
    }
}
