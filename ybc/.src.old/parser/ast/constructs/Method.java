package src.parser.ast.constructs;

import java.util.ArrayList;
import src.parser.ast.expr.TypeExpr;
import src.parser.ast.literal.IdentifierLiteral;

public class Method {
    public boolean isStatic;
    public IdentifierLiteral name;
    public ArrayList<Pair<IdentifierLiteral, TypeExpr>> args;
    public TypeExpr returnType;

    public Method() {}

    @Override
    public String toString() {
        String s = (isStatic ? "" : ".") + name + "(";
        for (var arg : args)
            s += arg.a + ": " + arg.b + ", ";
        if (args.size() > 0)
            s = s.substring(0, s.length() - 2);
        s += ") " + returnType + ";";
        return s;
    }
}
