package src.parser.ast.stmt;

import java.util.ArrayList;
import src.parser.ast.constructs.Statement;
import src.parser.ast.constructs.ClassMethod;
import src.parser.ast.expr.TypeExpr;
import src.parser.ast.NodeType;

public class ImplementStmt extends Statement {
    public TypeExpr name;
    public TypeExpr interf;
    public ArrayList<ClassMethod> methods;

    public ImplementStmt() {
        type = NodeType.ImplementStmt;
    }

    @Override
    public String toString() {
        String s = "implement " + name + " for " + interf + " {\n";
        for (var m : methods)
            s += m.toString().indent(4);
        s += "}";
        return s;
    }
}
