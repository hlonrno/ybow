package src.parser.ast.stmt;

import java.util.ArrayList;
import src.parser.ast.constructs.Statement;
import src.parser.ast.constructs.Visibility;
import src.parser.ast.constructs.Method;
import src.parser.ast.expr.TypeExpr;
import src.parser.ast.NodeType;

public class InterfaceStmt extends Statement {
    public Visibility visibility;
    public TypeExpr name;
    public ArrayList<Method> methods;

    public InterfaceStmt() {
        type = NodeType.InterfaceStmt;
    }

    @Override
    public String toString() {
        String s = "interface " + visibility + " "+ name + " {\n";
        for (var m : methods)
            s += m.toString().indent(4);
        s += "}";
        return s;
    }
}
