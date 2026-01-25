package src.parser.ast.stmt;

import src.parser.ast.constructs.Statement;
import src.parser.ast.NodeType;

public class ReturnStmt extends Statement {
    public Expression value;

    public ReturnStmt(Expression value) {
        type = NodeType.ImplicitReturnStmt;
        this.value = value;
    }

    @Override
    public String toString() {
        return "return " + value;
    }
}
