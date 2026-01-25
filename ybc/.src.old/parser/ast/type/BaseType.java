package src.parser.ast.type;

import src.parser.ast.constructs.TypeType;
import src.parser.ast.expr.TypeExpr;
import src.parser.ast.literal.IdentifierLiteral;
import src.parser.ast.NodeType;

public class BaseType extends TypeExpr {
    public IdentifierLiteral name;

    public BaseType(IdentifierLiteral name) {
        typeType = TypeType.Base;
        type = NodeType.TypeExpr;
        this.name = name;
    }

    @Override
    public String toString() {
        return "" + name;
    }
}
