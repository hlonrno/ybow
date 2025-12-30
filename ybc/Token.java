
public class Token {
    TokenType type;
    int beginColumn;
    int endColumn;
    int beginLine;
    int endLine;
    String value;

    @Override
    public String toString() {
        return String.format("Token:%d-%d:%d-%d: (%s) %s",
                beginLine, endLine, beginColumn, endColumn,
                type.getDeclaringClass().getName(), value);
    }
}
