package src.parser.ast.constructs;

import java.util.ArrayList;

import src.parser.ast.expr.BlockExpr;
import src.parser.ast.expr.TypeExpr;
import src.parser.ast.stmt.Expression;
import src.parser.ast.literal.IdentifierLiteral;
import src.parser.ast.literal.StringLiteral;

public class ClassMethod {
    public boolean isStatic;
    public Visibility visibility;
    public boolean isFinal;
    public boolean isNative;
    public String abi = null;
    public StringLiteral nativeName = null, nativeLib = null;
    public IdentifierLiteral name;
    public ArrayList<Pair<IdentifierLiteral, TypeExpr>> args;
    public TypeExpr returnType;
    public Expression exec;

    public ClassMethod() {}

    @Override
    public String toString() {
        String s = (isStatic ? "" : ".") + visibility;
        if (isNative) {
            s += " native";
            if (abi != null)
                s += " (" + abi + ")";
            if (nativeName != null)
                s += " " + nativeName;
            if (nativeLib != null)
                s += " : " + nativeLib;
        }
        s += " " + name + "(";
        for (var arg : args)
            s += arg.a + ": " + arg.b + ", ";
        if (args.size() > 0)
            s = s.substring(0, s.length() - 2);
        s += ") " + returnType + " " + exec + (exec instanceof BlockExpr ? "" : ";");
        return s;
    }
}
