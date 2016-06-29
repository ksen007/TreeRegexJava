package treeregex;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/29/16
 * Time: 11:55 AM
 */
public class TRegexLib {
    private TRegexAST tRegexAST;
    private UniversalAST mod;

    public TRegexLib(String pattern, boolean isRegex) {
        this.tRegexAST = TRegexAST.parseTRegex(pattern, isRegex);
    }

    public TRegexLib(String pattern, boolean isRegex, String replacement) {
        this.tRegexAST = TRegexAST.parseTRegex(pattern, isRegex);
        if (replacement != null) this.mod = UniversalAST.parseSTree(replacement);
    }

    public ObjectArrayList matches(String source) {
        UniversalAST stree = UniversalAST.parseSTree(source);
        return stree.matches(this.tRegexAST);
    }

    public UniversalAST replace(String source) {
        UniversalAST stree = UniversalAST.parseSTree(source);
        ObjectArrayList matches = stree.matches(this.tRegexAST);
        UniversalAST mod = this.mod.replace(matches);
        return mod;
    }
}
