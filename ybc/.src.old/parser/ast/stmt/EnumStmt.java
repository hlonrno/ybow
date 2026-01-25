package src.parser.ast.stmt;

import java.util.ArrayList;
import src.parser.ast.constructs.Statement;
import src.parser.ast.constructs.Visibility;
import src.parser.ast.constructs.Field;
import src.parser.ast.expr.TypeExpr;
import src.parser.ast.NodeType;

public class EnumStmt extends Statement {
    public Visibility visibility;
    public TypeExpr name;
    public TypeExpr fieldType;
    public ArrayList<Field> fields;

    public EnumStmt() {
        type = NodeType.EnumStmt;
    }

    @Override
    public String toString() {
        String s = "enum " + visibility + " " + name;
        if (fieldType != null)
            s += "(" + fieldType + ")";
        s += " {\n";
        for (var f : fields)
            s += f.toString().indent(4);
        s += "}";
        return s;
    }
}
