package src.parser.ast.stmt;

import java.util.ArrayList;
import src.parser.ast.constructs.Statement;
import src.parser.ast.constructs.Visibility;
import src.parser.ast.constructs.Field;
import src.parser.ast.expr.TypeExpr;
import src.parser.ast.NodeType;

public class StructStmt extends Statement {
    public Visibility visibility;
    public TypeExpr name;
    public ArrayList<Field> fields;

    public StructStmt() {
        type = NodeType.StructStmt;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("struct ");
        sb.append(visibility).append(' ')
            .append(name).append(" {\n");
        for (var f : fields)
            sb.append(f.toString().indent(4));
        sb.append("}");
        return sb.toString();
    }
}
