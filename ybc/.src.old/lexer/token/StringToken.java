package src.lexer.token;

import src.lexer.Lexer;

public class StringToken extends Token {
    public String literal;
    public StringToken(Token tok) {
        this.literal = Lexer.literalizeString(tok.value);
        type = TokenType.StringLiteral;
        line = tok.line;
        column = tok.column;
        value = '"' + tok.value + '"';
    }

    // @Override
    // public String toString() {
    //     return String.format("Soken %3d:%-3d %-16s %s '%s'",
    //         line, column, type.toString(), value, literal);
    // }
}

