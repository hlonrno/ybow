package src.parser;

public class Parser {
    private static final Trie trie = new Trie();

    // TODO: Parser

    {
        // AST.OpType
        String[] operators = new String[] { 
            "@", ",", ";", "{", "}", "->", ":", "=",
            "||=", "!||=", "&&=", "!&&=", "^^=", "!^^=",
            "**=", "*=", "/=", "+=", "-=", "|=",
            "~|=", "&=", "~&=", "^=", "~^=",
            "<<=", ">>=", "|<<=", "|>>=",
            "<<<=", ">>>=", "|<<<=", "|>>>=",
            "(", ")", ".", "::", "[", "]",
            "++", "--", "!", "~", "is",
            "==", "!=", "<=", ">=", "<", ">",
            "||", "!||", "&&", "!&&", "^^", "!^^",
            "**", "*", "/", "+", "-",
            "|", "~|", "&", "~&", "^", "~^",
            "<<", ">>", "|<<", "|>>",
            "<<<", ">>>", "|<<<", "|>>>"
        };

        for (var op : operators)
            trie.add(op);
        System.out.println(trie);
    }

    public static void main(String[] args) {
        new Parser();
    }
}
