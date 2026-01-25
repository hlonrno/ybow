package src.parser.ast.expr;

import src.parser.ast.stmt.Expression;
import src.parser.ast.NodeType;

public class LoopExpr extends Expression {
    public Expression condition;
    public Expression body;

    public LoopExpr(Expression condition, Expression body) {
        type = NodeType.LoopExpr;
        this.condition = condition;
        this.body = body;
    }

    @Override
    public String toString() {
        String s = "loop ";
        if (condition != null)
            s += "(" + condition + ") ";
        if (body != null) {
            s += body;
            if (body.type != NodeType.BlockExpr)
                s += ";";
        }
        return s;
    }
}
