package src.parser.ast.constructs;

import src.parser.ast.expr.TypeExpr;
import src.parser.ast.stmt.Expression;
import src.parser.ast.literal.IdentifierLiteral;

public class Field {
    public IdentifierLiteral name;
    public TypeExpr type;
    public Expression init;

    public Field() {}
    public Field(IdentifierLiteral name, TypeExpr type, Expression init) {
        this.name = name;
        this.type = type;
        this.init = init;
    }

    @Override
    public String toString() {
        String s = "." + name + ":";
        if (type != null)
            s += " " + type;
        if (init != null)
            s += " = " + init;
        s += ";";
        return s;
    }
}
