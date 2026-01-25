package src.lexer.exception;

import src.lexer.Token;

public class UnkownEscapeSequenceException extends LexerException {
    public UnkownEscapeSequenceException(String message, String source, Token tok) {
        super(message, source, tok);
    }
}
