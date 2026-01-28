package src.parser;

import src.lexer.Token;

// TODO: format and toString() all of these
// TODO: figure out how to make the type system
public interface Ast {
    public static sealed interface Node permits Expr, Stmt, Program {}
    public static sealed interface Expr extends Node permits
        LiteralExpr, TypeExpr,
        UnaryOp, BinOp, IfExpr, IfElseExpr, SwitchExpr, LoopExpr, BlockExpr,
        BreakIfExpr, ContinueIfExpr, ReturnIfExpr {}
    public static sealed interface Stmt extends Node permits
        Class, Interface, Impl, Import, Assert, If, IfElse, Switch, Loop, LoopCond,
        Block, Break, BreakWith, Continue, Return, BreakIf, ContinueIf, ReturnIf,
        ImplicitReturn, EmptyStmt, ExprStmt {}

    // Node
    public static record Program(
        String source,
        Stmt[] body
    ) implements Node {}

    // Expr
    public static record UnaryOp(
        OpType op,
        Expr expr
    ) implements Expr {}
    
    public static record BinOp(
        OpType op,
        Expr left,
        Expr right
    ) implements Expr {}
    public static record IfExpr(
        Expr condition,
        Expr ifBody
    ) implements Expr {}
    
    public static record IfElseExpr(
        Expr condition,
        Expr ifBody,
        Expr elseBody
    ) implements Expr {}
    public static record SwitchExpr(
        Expr cmp,
        OpType op,
        SwitchExprCase[] cases
    ) implements Expr {}
    public static record LoopExpr(
        Expr condition,
        Expr body
    ) implements Expr {}
    public static record BlockExpr(
        Stmt[] stmts
    ) implements Expr {}
    public static record BreakIfExpr(
        Expr condition,
        Expr value
    ) implements Expr {}
    public static record ContinueIfExpr(
        Expr condition,
        Expr value
    ) implements Expr {}
    public static record ReturnIfExpr(
        Expr condition,
        Expr value
    ) implements Expr {}

    // Expr/LiteralExpr
    public static sealed interface LiteralExpr extends Expr permits
        LNull, LBool, LNumber, LCharacter, LString, LIdentifier, LArray {}
    public static record LNull(
        Token tok
    ) implements LiteralExpr {}
    public static record LBool(
        Token tok,
        boolean bool
    ) implements LiteralExpr {}
    public static record LNumber(
        Token tok,
        Number number
    ) implements LiteralExpr {}
    public static record LCharacter(
        Token tok,
        char chr
    ) implements LiteralExpr {}
    public static record LString(
        Token tok,
        String str
    ) implements LiteralExpr {}
    public static record LIdentifier(
        Token tok,
        String ident
    ) implements LiteralExpr {}
    public static record LArray(
        Expr[] items
    ) implements LiteralExpr {}

    // Expr/TypeExpr
    public static sealed interface TypeExpr extends Expr permits
        TFinal, TBase, TGeneric, TArray, TSizedArray, TFree, TExtends,
        TImplements {}
    public static record TFinal(
        TypeExpr child
    ) implements TypeExpr {}
    public static record TBase(
        Token tok
    ) implements TypeExpr {}
    public static record TGeneric(
        TypeExpr child,
        TypeExpr[] generics
    ) implements TypeExpr {}
    public static record TArray(
        TypeExpr child
    ) implements TypeExpr {}
    public static record TSizedArray(
        TypeExpr child,
        LNumber size
    ) implements TypeExpr {}
    public static record TFree(
        TypeExpr child
    ) implements TypeExpr {}
    public static record TExtends(
        TypeExpr left,
        TypeExpr right
    ) implements TypeExpr {}
    public static record TImplements(
        TypeExpr left,
        TypeExpr right
    ) implements TypeExpr {}

    // Stmt
    public static record Class(
        Visibility vis,
        TypeExpr name,
        ClassField[] fields,
        ClassMethod[] methods
    ) implements Stmt {}
    public static record Interface(
        Visibility vis,
        TypeExpr name,
        Method[] methods
    ) implements Stmt {}
    public static record Impl(
        TypeExpr interf,
        TypeExpr clasz,
        ClassMethod[] methods
    ) implements Stmt {}
    public static record Import(
        String path
    ) implements Stmt {}
    public static record Assert(
        Expr condition,
        Expr message
    ) implements Stmt {}
    public static record If(
        Expr condition,
        Stmt ifBody
    ) implements Stmt {}
    public static record IfElse(
        Expr condition,
        Stmt ifBody,
        Stmt elseBody
    ) implements Stmt {}
    public static record Switch(
        Expr cmp,
        OpType op,
        SwitchCase[] cases
    ) implements Stmt {}
    public static record Loop(
        Stmt body
    ) implements Stmt {}
    public static record LoopCond(
        Expr condition,
        Stmt body
    ) implements Stmt {}
    public static record Block(
        Stmt[] stmts
    ) implements Stmt {}
    public static record Break(
    ) implements Stmt {}
    public static record BreakWith(
        Expr value
    ) implements Stmt {}
    public static record Continue(
    ) implements Stmt {}
    public static record Return(
        Expr value
    ) implements Stmt {}
    public static record BreakIf(
        Expr condition,
        Expr value
    ) implements Stmt {}
    public static record ContinueIf(
        Expr condition,
        Expr value
    ) implements Stmt {}
    public static record ReturnIf(
        Expr condition,
        Expr value
    ) implements Stmt {}
    public static record ImplicitReturn(
        Expr ret
    ) implements Stmt {}
    public static record EmptyStmt(
    ) implements Stmt {}
    public static record ExprStmt(
        Expr expr
    ) implements Stmt {}

    // Constructs
    public static record SwitchExprCase(
        Expr match,
        Expr expr
    ) {}
    public static record SwitchCase(
        Expr match,
        Stmt body
    ) {}
    public static sealed interface Number
        permits NByte, NShort, NInt, NLong, NFloat, NDouble {}
    public static record NByte  (byte num)   implements Number {}
    public static record NShort (short num)  implements Number {}
    public static record NInt   (int num)    implements Number {}
    public static record NLong  (long num)   implements Number {}
    public static record NFloat (float num)  implements Number {}
    public static record NDouble(double num) implements Number {}

    public static record ClassField(
        boolean isStatic,
        Visibility vis,
        LIdentifier name,
        TypeExpr type,
        Expr init
    ) {}
    public static sealed interface ClassMethod
        permits NormalClassMethod, NativeClassMethod {}
    public static record NormalClassMethod(
        boolean isStatic,
        Visibility vis,
        LIdentifier name,
        MethodArg[] args,
        TypeExpr returnType,
        Stmt body
    ) implements ClassMethod {}
    public static record NativeClassMethod(
        Visibility vis,
        ABI abi,
        LString aName,
        LString aModule,
        LIdentifier name,
        TypeExpr[] args,
        TypeExpr returnType
    ) implements ClassMethod {}

    public static record Method(
        boolean isStatic,
        LIdentifier name,
        MethodArg[] args,
        TypeExpr returnType
    ) {}
    public static record MethodArg(
        LIdentifier name,
        TypeExpr type
    ) {}

    public static enum OpType {
        Declare,  // (): ()
        Define,   // () = ()
        DefOr,   // () ||= ()
        DefNor,  // () !||= ()
        DefAnd,  // () &&= ()
        DefNand, // () !&&= ()
        DefXor,  // () ^^= ()
        DefXnor, // () !^^= ()
        DefPow, // () **= ()
        DefMul, // () *= ()
        DefDiv, // () /= ()
        DefAdd, // () += ()
        DefSub, // () -= ()
        DefBwOr,   // () |= ()
        DefBwNor,  // () ~|= ()
        DefBwAnd,  // () &= ()
        DefBwNand, // () ~&= ()
        DefBwXor,  // () ^= ()
        DefBwXnor, // () ~^= ()
        DefLShift,   // () <<= ()
        DefRShift,   // () >>= ()
        DefLRoll,    // () |<<= ()
        DefRRoll,    // () |>>= ()
        DefLShiftUnsigned, // () <<<= ()
        DefRShiftUnsigned, // () >>>= ()
        DefLRollUnsigned,  // () |<<<= ()
        DefRRollUnsigned,  // () |>>>= ()
        Call,     // ()(...)
        Member,   // ().()
        Method,   // ()::()
        ArrIndex, // ()[()]
        Error,    // ()!
        PostInc,  // ()++
        PostDec,  // ()--
        Negative, // -()
        Pass,     // +()
        Not,      // !()
        BwNot,    // ~()
        PreInc,   // ++()
        PreDec,   // --()
        Is, // () is (type)
        E,  // () == ()
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
        LShift,   // () << ()
        RShift,   // () >> ()
        LRoll,    // () |<< ()
        RRoll,    // () |>> ()
        LShiftUnsigned, // () <<< ()
        RShiftUnsigned, // () >>> ()
        LRollUnsigned,  // () |<<< ()
        RRollUnsigned,  // () |>>> ()
    }

    public static enum Visibility {
        Public,
        Private,
        Protected;
    }

    public static enum ABI {
        C;
    }
}
