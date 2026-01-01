
public class Token {
    TokenType type;
    int beginColumn;
    int endColumn;
    int beginLine;
    int endLine;
    String value;

    @Override
    public String toString() {
        return String.format("%d:%d: (%s) '%s'",
                beginLine, beginColumn,
                switch (type) {
                    case CharacterLiteral -> "chr";
                    case StringLiteral    -> "str";
                    case NumberLiteral    -> "num";
                    case SpecialChar      -> "spc";
                    case Identifier       -> "idn";
                }, value);
    }
}
