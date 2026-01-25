package src.lexer;

public class UnknownOperatorException extends LexerException {
    private String message;

    public UnknownOperatorException(String message) {
        this.message = message;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
