package src.parser.ast.expr;

import src.parser.ast.constructs.OperationType;
import src.parser.ast.stmt.Expression;
import src.parser.ast.NodeType;

public class BinOpExpr extends Expression {
    public OperationType operation;
    public Expression left;
    public Expression right;

    public BinOpExpr(OperationType op, Expression left, Expression right) {
        type = NodeType.BinOpExpr;
        operation = op;
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return "(" + left + " " + operation + " " + right + ")";
    }
}
