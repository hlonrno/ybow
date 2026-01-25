package src.parser.ast.type;

import java.util.ArrayList;
import src.parser.ast.constructs.TypeType;
import src.parser.ast.expr.TypeExpr;
import src.parser.ast.NodeType;

public class GenericType extends TypeExpr {
    public TypeExpr child;
    public ArrayList<TypeExpr> generics;

    public GenericType(TypeExpr child) {
        typeType = TypeType.Generic;
        type = NodeType.TypeExpr;
        this.child =child;
    }

    @Override
    public String toString() {
        String s = child + "<";
        for (int i = 0; i < generics.size() - 1; i++)
            s += generics.get(i) + ", ";
        if (generics.size() > 0)
            s += generics.get(generics.size() - 1) + ">";
        return s;
    }
}
