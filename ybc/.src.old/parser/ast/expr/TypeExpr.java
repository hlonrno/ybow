package src.parser.ast.expr;

import src.parser.ast.constructs.TypeType;
import src.parser.ast.stmt.Expression;
import src.parser.ast.NodeType;

public abstract class TypeExpr extends Expression {
    public TypeType typeType;

    public TypeExpr() {
        type = NodeType.TypeExpr;
    }
}
