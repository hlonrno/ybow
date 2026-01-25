package src.parser.ast.expr;

import java.util.ArrayList;

import src.parser.ast.NodeType;
import src.parser.ast.stmt.Expression;

public class MethodExpr extends Expression {
    public ArrayList<Expression> args;

    public MethodExpr() {
        type = NodeType.MethodExpr;
    }

    @Override
    public String toString() {
        String s = "(";
        for (var arg : args)
            s += arg + ", ";
        s = s.substring(0, s.length() - 2);
        return s;
    }
}
