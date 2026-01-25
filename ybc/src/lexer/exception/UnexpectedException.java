package src.lexer.exception;

import src.lexer.Token;

public class UnexpectedException extends LexerException {
    public UnexpectedException(String message, String source, Token tok) {
        super(message, source, tok);
    }
}
