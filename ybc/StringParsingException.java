
public class StringParsingException extends LexerException {
    private String message;

    public StringParsingException(String message) {
        this.message = message;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
