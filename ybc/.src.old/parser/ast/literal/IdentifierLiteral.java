package src.parser.ast.literal;

import src.lexer.token.Token;
import src.parser.ast.expr.Literal;
import src.parser.ast.NodeType;

public class IdentifierLiteral extends Literal {
    public Token value;

    public IdentifierLiteral(Token value) {
        type = NodeType.IdentifierLiteral;
        this.value = value;
    }

    @Override
    public String toString() {
        return value.value;
    }
}
