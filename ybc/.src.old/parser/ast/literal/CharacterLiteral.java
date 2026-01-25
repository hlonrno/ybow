package src.parser.ast.literal;

import src.lexer.token.CharacterToken;
import src.parser.ast.expr.Literal;
import src.parser.ast.NodeType;

public class CharacterLiteral extends Literal {
    public CharacterToken value;

    public CharacterLiteral(CharacterToken value) {
        type = NodeType.CharacterLiteral;
        this.value = value;
    }

    @Override
    public String toString() {
        return value.value;
    }
}
