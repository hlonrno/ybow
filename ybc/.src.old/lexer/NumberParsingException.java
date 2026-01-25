package src.lexer;

public class NumberParsingException extends LexerException {
    private String message;

    public NumberParsingException(String message) {
        this.message = message;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
