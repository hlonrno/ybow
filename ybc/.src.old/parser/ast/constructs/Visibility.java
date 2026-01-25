package src.parser.ast.constructs;

public enum Visibility {
    Pub ("pub"),  // public
    Priv("priv"), // private
    Prot("prot"); // protected
   
    private final String keyword;
    private Visibility(final String keyword) {
        this.keyword = keyword;
    }

    @Override
    public String toString() {
        return keyword;
    }
}
