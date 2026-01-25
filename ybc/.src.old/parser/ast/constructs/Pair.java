package src.parser.ast.constructs;

public class Pair<A, B> {
    public A a;
    public B b;

    @Override
    public String toString() {
        return a.toString() + " " + b.toString();
    }
}
