package src.parser.ast.type;

import src.parser.ast.constructs.TypeType;
import src.parser.ast.expr.TypeExpr;
import src.parser.ast.NodeType;

public class ExtendType extends TypeExpr {
    public TypeExpr left;
    public TypeExpr right;

    public ExtendType(TypeExpr left, TypeExpr right) {
        typeType = TypeType.Extend;
        type = NodeType.TypeExpr;
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return left + " ++ " + right;
    }
}
