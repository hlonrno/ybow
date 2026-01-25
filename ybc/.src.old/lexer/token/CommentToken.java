package src.lexer.token;

public class CommentToken extends Token {
    public String literal;
    public CommentToken(Token tok) {
        value = tok.value;
        type = TokenType.Comment;
        line = tok.line;
        column = tok.column;
        if (value.endsWith("//") && value.length() > 3)
            literal = value.substring(2, value.length() - 2);
        else
            literal = value.substring(2);
    }
}
