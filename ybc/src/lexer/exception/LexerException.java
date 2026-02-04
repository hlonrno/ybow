package src.lexer.exception;

import src.lexer.Token;

public class LexerException extends Exception {
    protected final String message;

    public LexerException(String message, String source, Token tok) {
        if (tok == null)
            // exmaple: main.yb: error: unexpected EOF.
            this.message = String.format("%s: \33[91merror\33[0m: %s",
                source, message);
        else 
            // exmaple: main.yb:10:11: error: expected 0x, got unknown "b". [token LNumber |null]
            this.message = String.format("%s:%d:%d: \33[91merror\33[0m: %s [token %s |%s]",
                source, tok.getLine(), tok.getColumn(), message, tok.getType(), tok.getImage());
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}
