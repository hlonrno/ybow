package src.parser.ast.stmt;

import src.parser.ast.constructs.Statement;
import src.parser.ast.NodeType;

public class ContinueIfStmt extends Statement {
    public Expression condition;
    public Expression value;

    public ContinueIfStmt(Expression condition, Expression value) {
        type = NodeType.ContinuIfeStmt;
        this.condition = condition;
        this.value = value;
    }

    @Override
    public String toString() {
        return condition == null ? "continue " + value + ";"
            : "continueif (" + condition + ") " + value + ";";
    }
}
