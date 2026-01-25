package src.parser.ast.expr;

import java.util.ArrayList;

import src.parser.ast.constructs.OperationType;
import src.parser.ast.constructs.Pair;
import src.parser.ast.stmt.Expression;
import src.parser.ast.NodeType;

public class SwitchExpr extends Expression {
    public Expression match;
    // what a monstrosity
    public ArrayList<Pair<Pair<OperationType, ArrayList<Expression>>, Expression>> cases;

    public SwitchExpr(Expression match) {
        type = NodeType.SwitchExpr;
        this.match = match;
    }

    /*
     * hopefully:
       switch (expr) {
            == `a, `b, `c -> {
                i = 0b;
            };
            != `a, `b, `c -> 1b;
            <= `a, `b, `c -> 1b;
       }
    */
    @Override
    public String toString() {
        String s = "switch (" + match + ") {\n";
        for (var c : cases) {
            String cs = c.a.a + " ";
            for (var m : c.a.b)
                cs += m.toString() + ", ";
            cs += "-> " + c.b + ";";
            s += cs.indent(4);
        }
        s += "}";
        return s;
    }
}
