package src.lexer.token;

public class CharacterToken extends Token {
    public char literal;
    public CharacterToken(Token tok) {
        literal = tok.value.charAt(0);
        type = TokenType.CharacterLiteral;
        line = tok.line;
        column = tok.column;
        value = '`' + tok.value;
    }
}
