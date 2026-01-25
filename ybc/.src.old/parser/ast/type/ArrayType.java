package src.parser.ast.type;

import src.parser.ast.constructs.TypeType;
import src.parser.ast.expr.TypeExpr;
import src.parser.ast.literal.NumberLiteral;
import src.parser.ast.NodeType;

public class ArrayType extends TypeExpr {
    public TypeExpr child;
    public NumberLiteral size;

    public ArrayType() {
        typeType = TypeType.Array;
        type = NodeType.TypeExpr;
    }

    @Override
    public String toString() {
        return child + "[" + (size != null ? size.toString() : "") + "]";
    }
}
