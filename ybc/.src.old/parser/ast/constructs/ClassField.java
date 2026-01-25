package src.parser.ast.constructs;

import src.parser.ast.expr.TypeExpr;
import src.parser.ast.stmt.Expression;
import src.parser.ast.literal.IdentifierLiteral;

public class ClassField {
    public boolean isStatic;
    public Visibility visibility;
    public IdentifierLiteral name;
    public TypeExpr type;
    public Expression init;

    public ClassField() {}
    public ClassField(IdentifierLiteral name, TypeExpr type, Expression init) {
        this.name = name;
        this.type = type;
        this.init = init;
    }

    @Override
    public String toString() {
        String s = (isStatic ? "" : ".") + visibility + " " + name + ":";
        if (type != null)
            s += " " + type;
        if (init != null)
            s += " = " + init;
        s += ";";
        return s;
    }
}
