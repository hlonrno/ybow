package src.parser.ast.literal;

import src.lexer.token.NumberToken;
import src.parser.ast.expr.Literal;
import src.parser.ast.NodeType;

public class NumberLiteral extends Literal {
    public NumberToken value;

    public NumberLiteral(NumberToken value) {
        type = NodeType.NumberLiteral;
        this.value = value;
    }

    @Override
    public String toString() {
        return value.value;
    }
}
