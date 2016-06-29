package treeregex;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/29/16
 * Time: 11:55 AM
 */
public class TreeRegexLib {
    private TreeRegex treeRegexAST;
    private SerializedTree mod;

    public TreeRegexLib(String pattern, boolean isRegex) {
        this.treeRegexAST = TreeRegex.parse(pattern, isRegex);
    }

    public TreeRegexLib(String pattern, boolean isRegex, String replacement) {
        this.treeRegexAST = TreeRegex.parse(pattern, isRegex);
        if (replacement != null) this.mod = SerializedTree.parse(replacement);
    }

    public ObjectArrayList matches(String source) {
        SerializedTree stree = SerializedTree.parse(source);
        return stree.matches(this.treeRegexAST);
    }

    public SerializedTree replace(String source) {
        SerializedTree stree = SerializedTree.parse(source);
        ObjectArrayList matches = stree.matches(this.treeRegexAST);
        SerializedTree mod = this.mod.replace(matches);
        return mod;
    }
}
