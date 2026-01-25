package src.parser.ast.constructs;

/*
 * TODO: c'mon: parse precedence;
 * I don't think precedence is gonna be implemented soon.
 * boolean/bitwise operations follow:
     * or < and < xor 
 *
 * numbered, and ordered by precedence:

5_: assign
4_: bool
33: .Is
32: .Call
31: .MemberOf
30: .ArrayGet
2_: compare
1_: mul + pow
1_: add + inc/dec
_: bitwise
    logic
    shift + roll
*/

public enum OperationType {
    Is(33),     // obj is @Array<int> // aka instanceof
    Call(32),
    MemberOf(31),
    ArrayGet(30),
    // arith
    Add(10),
    Sub(10),
    // 11: inc/dec
    Mul(12),
    Div(12),
    Pow(13),
    // compare
    Eq(20),
    NEq(20),
    Lt(20),
    Gt(20),
    LtEq(20),
    GtEq(20),
    // bitwise
    BwNot(0),
    BwOr(1),  BwNor(1),
    BwAnd(2), BwNand(2),
    BwXor(3), BwXnor(3),
    LShift(4),
    RShift(4),
    LRoll(4),
    RRoll(4),
    LShiftSignless(4),
    RShiftSignless(4),
    LRollSignless(4),
    RRollSignless(4),
    // "bool"
    Not(40),
    Or(41), Nor(41),
    And(42), Nand(42),
    Xor(43), Xnor(43),
    // inc, dec
    PostInc(11),
    PostDec(11),
    PreInc(11),
    PreDec(11),
    // vars
    VarDec(51), // i: int
    VarDef(50), // i = 10i
    // vars/arith
    VarDefAdd(50),
    VarDefSub(50),
    VarDefMul(50),
    VarDefPow(50),
    VarDefDiv(50),
    // vars/bitwise
    VarDefBwAnd(50),
    VarDefBwOr(50),
    VarDefBwXor(50),
    VarDefBwNand(50),
    VarDefBwNor(50),
    VarDefBwXnor(50),
    VarDefLShift(50),
    VarDefRShift(50),
    VarDefLRoll(50),
    VarDefRRoll(50),
    VarDefLShiftSignless(50),
    VarDefRShiftSignless(50),
    VarDefLRollSignless(50),
    VarDefRRollSignless(50),
    // vars/"bool"
    VarDefAnd(50),
    VarDefOr(50),
    VarDefXor(50),
    VarDefNand(50),
    VarDefNor(50),
    VarDefXnor(50),
    ;

    public final int precedence;

    private OperationType(int precedence) {
        this.precedence = precedence;
    }
}
