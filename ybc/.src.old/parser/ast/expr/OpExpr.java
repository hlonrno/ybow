package src.parser.ast.expr;

import src.parser.ast.constructs.OperationType;
import src.parser.ast.stmt.Expression;
import src.parser.ast.NodeType;

public class OpExpr extends Expression {
    public OperationType operation;
    public Expression value;

    public OpExpr(OperationType op, Expression value) {
        type = NodeType.OpExpr;
        operation = op;
        this.value = value;
    }

    @Override
    public String toString() {
        return "(" + operation + " " + value + ")";
    }
}
