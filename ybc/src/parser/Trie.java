package src.parser;

import java.util.ArrayList;

public class Trie {
    private int prefix;
    private boolean matches;
    private ArrayList<Trie> next;

    public Trie() {
        prefix = -1;
        next = new ArrayList<>();
    }

    public void add(String pattern) { add(pattern, 0); }
    public void add(String pattern, int beginIndex) {
        if (pattern.length() == beginIndex) {
            this.matches = true;
            return;
        }
        char prefix = pattern.charAt(beginIndex);
        for (var trie : next)
            if (prefix == trie.prefix) {
                trie.add(pattern, beginIndex + 1);
                return;
            }
        var trie = new Trie();
        trie.prefix = prefix;
        trie.matches = pattern.length() == beginIndex;
        next.add(trie);
        trie.add(pattern, beginIndex + 1);
    }


    public boolean partialMatch(String pattern) { return partialMatch(pattern, 0); }
    public boolean partialMatch(String pattern, int beginIndex) {
        if (pattern.length() == beginIndex)
            return true;
        char prefix = pattern.charAt(beginIndex);
        for (var trie : next)
            if (trie.prefix == prefix)
                return trie.partialMatch(pattern, beginIndex + 1);
        return pattern.length() == beginIndex;
    }

    public boolean match(String pattern) { return match(pattern, 0); }
    public boolean match(String pattern, int beginIndex) {
        if (pattern.length() == beginIndex)
            return matches;
        char prefix = pattern.charAt(beginIndex);
        for (var trie : next)
            if (trie.prefix == prefix)
                return trie.match(pattern, beginIndex + 1);
        return matches;
    }

    @Override
    public String toString() {
        String s = "Trie(" + (char)prefix + ", " + matches + ")\n";
        for (var trie : next)
            s += trie.toString().indent(2);
        return s;
    }
}
