package src.parser.ast.constructs;

public enum TypeType {
    Base,    // int, float, Array
    Generic, // Array<int>
    Array,   // byte[]
    Free,    // Array*
    Extend,  // Array<? ++ String>
    Final,   // final int
}
