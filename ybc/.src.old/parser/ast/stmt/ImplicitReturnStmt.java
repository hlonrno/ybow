package src.parser.ast.stmt;

import src.parser.ast.constructs.Statement;
import src.parser.ast.NodeType;

public class ImplicitReturnStmt extends Statement {
    public Expression value;

    public ImplicitReturnStmt(Expression value) {
        type = NodeType.ReturnStmt;
        this.value = value;
    }

    @Override
    public String toString() {
        return "iret " + value;
    }
}
