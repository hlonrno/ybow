package src.parser;

import src.lexer.Token;

public sealed interface Node permits Node.Expr, Node.Stmt, Node.Program {
    public static sealed interface Expr extends Node permits
        LiteralExpr, TypeExpr,
        UnaryOpExpr, BinOpExpr, MethodCallExpr, IfExpr, LoopExpr, LoopElseExpr,
        SwitchExpr, BreakIfExpr, ContinueIfExpr, ReturnIfExpr, FnExpr {}

    public static sealed interface Stmt extends Node permits
        EmptyStmt, ExprStmt, BlockStmt, IfStmt, LoopStmt, BreakStmt,
        ReturnStmt, ContinueStmt, ImplReturnStmt, SwitchStmt, EnumStmt,
        ClassStmt, InterfStmt, PrimitiveStmt, ImplStmt {}

    public static sealed interface LiteralExpr extends Expr permits
        IdentifierL, CharL, StringL, NumberL, ArrayL, BoolL, NullL {}

    public static sealed interface TypeExpr extends Expr permits
        BaseT, TrashT, ArrayT, SizedArrayT, GenericT, ExtendedT, NullableT,
        FnT {}

    public static record Program(String source, Stmt[] body) implements Node {
        public String toString() {
            String s = "Program: source \"" + source + "\", code\n";
            for (int i = 0; i < body.length; i++)
                s += body[i] + "\n";
            s += "end, lines " + (s.lines().count() - 1);
            return s;
        }
    }

    // Expr
    public static record UnaryOpExpr(Op op, Expr expr) implements Expr {
        public String toString() {
            return "(" + op + " " + expr + ")";
        }
    }

    public static record BinOpExpr(Op op, Expr left, Expr right) implements Expr {
        public String toString() {
            return "(" + left + " " + op + " " + right + ")";
        }
    }

    public static record MethodCallExpr(Expr fn, Expr[] args) implements Expr {
        public String toString() {
            String s = fn + " call [";
            for (int i = 0; i < args.length; i++)
                s += args[i] + ", ";
            if (args.length > 0)
                s = s.substring(0, s.length() - 2);
            s += "]";
            return s;
        }
    }

    public static record IfExpr(Expr condition, Expr body, Expr otherwise) implements Expr {
        public String toString() {
            return "if (%s) %s else %s"
                .formatted(condition, body, otherwise);
        }
    }

    public static record LoopExpr(Expr condition, Expr body) implements Expr {
        public String toString() {
            if (condition == null)
                return "loop " + body;
            return "loop!(" + condition + ") " + body;
        }
    }

    public static record LoopElseExpr(Expr condition, Expr body, Expr otherwise) implements Expr {
        public String toString() {
            if (condition == null)
                return "loop %s else %s"
                    .formatted(body, otherwise);
            return "loop!(%s) %s else %s"
                .formatted(condition, body, otherwise);
        }
    }

    public static record SwitchExpr(Expr condition, SwitchExprCase[] cases) implements Expr {
        public final String toString() {
            String s = "switch (" + condition + ") {\n";
            for (int i = 0; i < cases.length; i++)
                s += cases[i].toString().indent(4);
            s += "}";
            return s;
        }
    }

    public static record BreakIfExpr(Expr condition, Expr value) implements Expr {
        public String toString() {
            return "break!(%s) %s"
                .formatted(condition, value);
        }
    }

    public static record ContinueIfExpr(Expr condition, Expr value) implements Expr {
        public String toString() {
            return "continue!(%s) %s"
                .formatted(condition, value);
        }
    }

    public static record ReturnIfExpr(Expr condition, Expr value) implements Expr {
        public String toString() {
            return "return!(%s) %s"
                .formatted(condition, value);
        }
    }

    public static record FnExpr(Arg[] args, TypeExpr rettype, Stmt body) implements Expr {
        public String toString() {
            String s = "fn:(";
            for (var arg : args)
                s += arg + ", ";
            if (args.length > 0)
                s = s.substring(0, s.length() - 2);
            s += ") ";
            if (rettype != null)
                s += rettype + " ";
            s += body;
            return s;
        }
    }

    // Stmt
    public static record EmptyStmt() implements Stmt {
        public String toString() {
            return ";";
        }
    }

    public static record ExprStmt(Expr expr) implements Stmt {
        public String toString() {
            return expr + ";";
        }
    }

    public static record BlockStmt(Stmt[] stmts) implements Stmt {
        public String toString() {
            String s = "{\n";
            for (int i = 0; i < stmts.length; i++)
                s += stmts[i].toString().indent(4);
            s += "}";
            return s;
        }
    }

    public static record IfStmt(Expr condition, Stmt body, Stmt otherwise) implements Stmt {
        public String toString() {
            if (otherwise == null)
                return "if (%s) %s"
                    .formatted(condition, body);
            return "if (%s) %s else %s"
                .formatted(condition, body, otherwise);
        }
    }

    public static record LoopStmt(Expr condition, Stmt body) implements Stmt {
        public String toString() {
            if (condition == null)
                return "loop " + body;
            return "loop!(" + condition + ") " + body;
        }
    }

    public static record BreakStmt(Expr value) implements Stmt {
        public String toString() {
            if (value == null)
                return "break " + value + ";";
            return "break;";
        }
    }

    public static record ReturnStmt(Expr value) implements Stmt {
        public String toString() {
            return "return " + value + ";";
        }
    }

    public static record ContinueStmt(Expr value) implements Stmt {
        public String toString() {
            return "continue " + value + ";";
        }
    }

    public static record ImplReturnStmt(Expr value) implements Stmt {
        public String toString() {
            return "iret " + value;
        }
    }

    public static record SwitchStmt(Expr value, SwitchCase[] cases) implements Stmt {
        public String toString() {
            String s = "switch (" + value + ") {\n";
            for (int i = 0; i < cases.length; i++)
                s += cases[i].toString().indent(4);
            s += "}";
            return s;
        }
    }

    public static record EnumStmt(Modifier[] mods, BaseT name, IdentifierL[] fields) implements Stmt {
        public String toString() {
            String s = "";
            for (var mod : mods)
                s += mod;
            s += "enum " + name + " {\n";
            for (var field : fields)
                s += (field + ";").indent(4);
            s += "}";
            return s;
        }
    }

    public static record ClassStmt(Modifier[] mods, TypeExpr name, ClassField[] fields, ClassMethod[] methods) implements Stmt {
        public String toString() {
            String s = "";
            for (var mod : mods)
                s += mod;
            s += "class " + name + " {\n";
            for (var field : fields)
                s += field.toString().indent(4);
            for (var method : methods)
                s += method.toString().indent(4);
            s += "}";
            return s;
        }
    }

    public static record InterfStmt(Modifier[] mods, TypeExpr name, Method[] methods) implements Stmt {
        public String toString() {
            String s = "";
            for (var mod : mods)
                s += mod;
            s += "interf " + name + " {\n";
            for (var method : methods)
                s += method.toString().indent(4);
            s += "}";
            return s;
        }
    }

    public static record PrimitiveStmt(Modifier[] mods, TypeExpr name, TypeExpr type, ClassMethod[] methods) implements Stmt {
        public String toString() {
            String s = "";
            for (var mod : mods)
                s += mod;
            s += "primitive " + name + "(" + type + ") {\n";
            for (var method : methods)
                s += method.toString().indent(4);
            s += "}";
            return s;
        }
    }

    public static record ImplStmt(Modifier[] mods, TypeExpr clz, TypeExpr interf, ClassMethod[] methods) implements Stmt {
        public String toString() {
            String s = "";
            for (var mod : mods)
                s += mod;
            s += "impl " + clz + " for " + interf + " {\n";
            for (var method : methods)
                s += method.toString().indent(4);
            s += "}";
            return s;
        }
    }

    // LiteralExpr
    public static record IdentifierL(Token tok) implements LiteralExpr {
       public String toString() { return tok.getImage(); }
    }

    public static record CharL(Token tok, char value) implements LiteralExpr {
       public String toString() { return tok.getImage(); }
    }

    public static record StringL(Token tok, String value) implements LiteralExpr {
       public String toString() { return tok.getImage(); }
    }

    public static record NumberL(Token tok) implements LiteralExpr {
       public String toString() { return tok.getImage(); }
    }

    public static record ArrayL(Expr[] value) implements LiteralExpr {
       public String toString() {
           String s = "[ ";
            for (int i = 0; i < value.length; i++)
                s += value + " ";
           s += "]";
           return s;
       }
    }

    public static record BoolL(Token tok, boolean value) implements LiteralExpr {
       public String toString() { return tok.getImage(); }
    }

    public static record NullL(Token tok) implements LiteralExpr {
       public String toString() { return tok.getImage(); }
    }

    // TypeExpr
    public static record BaseT(Token tok) implements TypeExpr {
        public String toString() {
            return tok.getImage();
        }
    }

    public static record TrashT() implements TypeExpr {
        public String toString() {
            return "_";
        }
    }

    public static record ArrayT(TypeExpr child) implements TypeExpr {
        public String toString() {
            return child + "[]";
        }
    }

    public static record SizedArrayT(TypeExpr child, NumberL size) implements TypeExpr {
        public String toString() {
            return "%s[%s]"
                .formatted(child, size);
        }
    }

    public static record GenericT(TypeExpr child, TypeExpr[] generics) implements TypeExpr {
        public String toString() {
            String s = child + "<";
            for (int i = 0; i < generics.length; i++)
                s += generics[i] + ", ";
            if (generics.length > 0)
                s = s.substring(0, s.length() - 2);
            s += ">";
            return s;
        }
    }

    public static record ExtendedT(TypeExpr child, TypeExpr[] extending) implements TypeExpr {
        public String toString() {
            String s = child + ": ";
            for (int i = 0; i < extending.length; i++)
                s += extending[i] + ", ";
            if (extending.length > 0)
                s = s.substring(0, s.length() - 2);
            return s;
        }
    }

    public static record NullableT(TypeExpr child) implements TypeExpr {
        public String toString() {
            return child + "?";
        }
    }

    public static record FnT(Arg[] args, TypeExpr rettype) implements TypeExpr {
        public String toString() {
            String s = "fn:(";
            for (var arg : args)
                s += arg + ", ";
            if (args.length > 0)
                s = s.substring(0, s.length() - 2);
            s += ") " + rettype;
            return s;
        }
    }

    // Constructs
    public static record SwitchExprCase(Expr[] conditions, Expr body) {
        public String toString() {
            String s = "case ";
            for (int i = 0; i < conditions.length; i++)
                s += conditions[i] + ", ";
            if (conditions.length > 0)
                s = s.substring(0, s.length() - 2);
            s += " -> " + body;
            return s;
        }
    }

    public static record SwitchCase(Expr[] conditions, Stmt body) {
        public String toString() {
            String s = "case ";
            for (int i = 0; i < conditions.length; i++)
                s += conditions[i] + ", ";
            if (conditions.length > 0)
                s = s.substring(0, s.length() - 2);
            s += " -> " + body;
            return s;
        }
    }

    public static record ClassField(Modifier[] mods, IdentifierL name, TypeExpr type, Expr init) {
        public String toString() {
            String s = "";
            for (var mod : mods)
                s += mod;
            s += name;
            if (type != null)
                s += ":" + type;
            if (init != null)
                s += " = " + init;
            s += ";";
            return s;
        }
    }

    public static record ClassMethod(Modifier[] mods, IdentifierL name, Arg[] args, TypeExpr rettype, boolean isInline, Stmt body) {
        public String toString() {
            String s = "";
            for (var mod : mods)
                s += mod;
            s += name + "(";
            for (int i = 0; i < args.length; i++)
                s += args[i] + ", ";
            if (args.length > 0)
                s = s.substring(0, s.length() - 2);
            s += ")";
            if (rettype != null)
                s += " " + rettype;
            if (isInline)
                s += ":";
            s += " " + body;
            return s;
        }
    }

    public static record Method(IdentifierL name, Arg[] args, TypeExpr rettype) {
        public String toString() {
            String s = name + "(";
            for (int i = 0; i < args.length; i++)
                s += args[i] + ", ";
            if (args.length > 0)
                s = s.substring(0, s.length() - 2);
            s += ")" + rettype;
            return s;
        }
    }

    public static record Arg(IdentifierL name, TypeExpr type) {
        public String toString() {
            return name + ": " + type;
        }
    }

    // Modifier
    public static sealed interface Modifier permits
        InstanceM, PrivateM, ProtectedM, FinalM, NativeM {}

    public static record InstanceM() implements Modifier {
        public String toString() {
            return ".";
        }
    }

    public static record PrivateM() implements Modifier {
        public String toString() {
            return "priv ";
        }
    }

    public static record ProtectedM() implements Modifier {
        public String toString() {
            return "prot ";
        }
    }

    public static record FinalM() implements Modifier {
        public String toString() {
            return "final ";
        }
    }

    public static record NativeM(ABI abi, StringL name, StringL module) implements Modifier {
        public String toString() {
            return "native (%s) \"%s\" : %s "
                .formatted(abi, name, module);
        }
    }

    public static enum ABI {
        C,
    }

    public static enum Op {
        Declare,    // (): ()
        Assign,     // () = ()
        DeclAssign, // () := ()
        Swap,       // () <=> ()
        OrAssign,   // () ||= ()
        NorAssign,  // () !||= ()
        AndAssign,  // () &&= ()
        NandAssign, // () !&&= ()
        XorAssign,  // () ^^= ()
        XnorAssign, // () !^^= ()
        PowAssign, // () **= ()
        MulAssign, // () *= ()
        DivAssign, // () /= ()
        AddAssign, // () += ()
        SubAssign, // () -= ()
        BwOrAssign,   // () |= ()
        BwNorAssign,  // () ~|= ()
        BwAndAssign,  // () &= ()
        BwNandAssign, // () ~&= ()
        BwXorAssign,  // () ^= ()
        BwXnorAssign, // () ~^= ()
        LeftShiftAssign,  // () <<= ()
        RightShiftAssign, // () >>= ()
        LeftRollAssign,   // () <<<= ()
        RightRollAssign,  // () >>>= ()
        Member,   // ().()
        ArrIndex, // ()[()]
        Error,    // ()!
        PostInc,  // ()++
        PostDec,  // ()--
        Pass,     // +()
        Negative, // -()
        Not,      // !()
        BwNot,    // ~()
        PreInc,   // ++()
        PreDec,   // --()
        Is, // () is (type)
        Eq, // () == ()
        Ne, // () != ()
        Le, // () <= ()
        Ge, // () >= ()
        Lt, // () < ()
        Gt, // () > ()
        Or,   // () || ()
        Nor,  // () !|| ()
        And,  // () && ()
        Nand, // () !&& ()
        Xor,  // () ^^ ()
        Xnor, // () !^^ ()
        Pow, // () ** ()
        Mul, // () * ()
        Div, // () / ()
        Add, // () + ()
        Sub, // () - ()
        BwOr,   // () | ()
        BwNor,  // () ~| ()
        BwAnd,  // () & ()
        BwNand, // () ~& ()
        BwXor,  // () ^ ()
        BwXnor, // () ~^ ()
        LeftShift,  // () << ()
        RightShift, // () >> ()
        LeftRoll,   // () <<< ()
        RightRoll,  // () >>> ()
    }
}
