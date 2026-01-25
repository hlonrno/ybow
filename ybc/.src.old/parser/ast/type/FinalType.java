package src.parser.ast.type;

import src.parser.ast.constructs.TypeType;
import src.parser.ast.expr.TypeExpr;
import src.parser.ast.literal.IdentifierLiteral;
import src.parser.ast.NodeType;

public class FinalType extends TypeExpr {
    public TypeExpr child;

    public FinalType(TypeExpr child) {
        typeType = TypeType.Final;
        type = NodeType.TypeExpr;
        this.child = child;
    }

    @Override
    public String toString() {
        return "final " + child;
    }
}
