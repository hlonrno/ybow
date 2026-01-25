package src.parser.ast.type;

import src.parser.ast.constructs.TypeType;
import src.parser.ast.expr.TypeExpr;
import src.parser.ast.NodeType;

public class FreeType extends TypeExpr {
    public TypeExpr child;

    public FreeType(TypeExpr child) {
        typeType = TypeType.Free;
        type = NodeType.TypeExpr;
        this.child = child;
    }

    @Override
    public String toString() {
        return child + "*";
    }
}
