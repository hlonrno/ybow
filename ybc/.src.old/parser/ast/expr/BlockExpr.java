package src.parser.ast.expr;

import java.util.ArrayList;
import src.parser.ast.constructs.Statement;
import src.parser.ast.stmt.Expression;
import src.parser.ast.NodeType;

public class BlockExpr extends Expression {
    public ArrayList<Statement> body;

    public BlockExpr() {
        type = NodeType.BlockExpr;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("{\n");
        for (var s : body)
            sb.append(s.toString().concat(";").indent(4));
        sb.append("}");
        return sb.toString();
    }
}
