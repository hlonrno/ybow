package src.parser.ast.expr;

import src.parser.ast.stmt.Expression;
import src.parser.ast.NodeType;

public class IfExpr extends Expression {
    public Expression condition;
    public Expression body;
    public Expression elseBody;

    public IfExpr(Expression condition, Expression ifTrue, Expression ifFalse) {
        type = NodeType.IfExpr;
        this.condition = condition;
        body = ifTrue;
        elseBody = ifFalse;
    }

    @Override
    public String toString() {
        String s = "if (" + condition + ") ";
        if (body != null)
            s += body;
        if (elseBody != null)
            s += " else " + elseBody;
        return s;
    }
}
