package src.parser.ast.stmt;

import java.util.ArrayList;
import src.parser.ast.constructs.Statement;
import src.parser.ast.constructs.Visibility;
import src.parser.ast.constructs.ClassField;
import src.parser.ast.constructs.ClassMethod;
import src.parser.ast.expr.TypeExpr;
import src.parser.ast.NodeType;

public class ClassStmt extends Statement {
    public Visibility visibility;
    public boolean isFinal;
    public TypeExpr name;
    public ArrayList<ClassField> fields;
    public ArrayList<ClassMethod> methods;

    public ClassStmt() {
        type = NodeType.ClassStmt;
    }

    @Override
    public String toString() {
        var sb = new StringBuilder("class ");
        sb.append(visibility).append(' ')
            .append(isFinal ? "final " : " ")
            .append(name).append(" {\n");
        for (var f : fields)
            sb.append(f.toString().indent(4));
        sb.append("} {\n");
        for (var m : methods)
            sb.append(m.toString().indent(4));
        sb.append("}");
        return sb.toString();
    }
}
