package src.parser.ast.literal;

import src.lexer.token.StringToken;
import src.parser.ast.expr.Literal;
import src.parser.ast.NodeType;

public class StringLiteral extends Literal {
    public StringToken value;

    public StringLiteral(StringToken value) {
        type = NodeType.StringLiteral;
        this.value = value;
    }

    @Override
    public String toString() {
        return value.value;
    }
}
