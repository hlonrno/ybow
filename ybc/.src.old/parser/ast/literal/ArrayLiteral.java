package src.parser.ast.literal;

import java.util.ArrayList;
import src.parser.ast.stmt.Expression;
import src.parser.ast.expr.Literal;
import src.parser.ast.NodeType;

public class ArrayLiteral extends Literal {
    public ArrayList<Expression> value;

    public ArrayLiteral(ArrayList<Expression> value) {
        type = NodeType.ArrayLiteral;
        this.value = value;
    }

    @Override
    public String toString() {
        String s = "[ ";
        for (var v : value)
            s += v.toString() + " ";
        s += "]";
        return s;
    }
}
