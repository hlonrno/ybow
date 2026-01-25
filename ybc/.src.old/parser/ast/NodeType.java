package src.parser.ast;

public enum NodeType {
    None, // default type, to avoid NPEs
    OpExpr,      // !true
    BinOpExpr,   // 1 == 1
    BlockExpr,   // { ...; ... }
    IfExpr,
    SwitchExpr,
    LoopExpr,
    TypeExpr,    // int; Entry<K, V>[][,]*;
    MethodExpr,
    // stmt
    StructStmt,
    ClassStmt,
    EnumStmt,
    InterfaceStmt,
    ImplementStmt,
    ContinuIfeStmt,
    ImplicitReturnStmt,
    ReturnStmt,

    // literals
    IdentifierLiteral,
    StringLiteral,
    NumberLiteral,
    CharacterLiteral,
    ArrayLiteral,     // [ 1 2 3 ]
}
