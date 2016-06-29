package treeregex;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/29/16
 * Time: 11:55 AM
 */
public class TreeRegexLib {
    private TreeRegexAST treeRegexAST;
    private SerializedTree mod;

    public TreeRegexLib(String pattern, boolean isRegex) {
        this.treeRegexAST = TreeRegexAST.parseTreeRegex(pattern, isRegex);
    }

    public TreeRegexLib(String pattern, boolean isRegex, String replacement) {
        this.treeRegexAST = TreeRegexAST.parseTreeRegex(pattern, isRegex);
        if (replacement != null) this.mod = SerializedTree.parseSTree(replacement);
    }

    public ObjectArrayList matches(String source) {
        SerializedTree stree = SerializedTree.parseSTree(source);
        return stree.matches(this.treeRegexAST);
    }

    public SerializedTree replace(String source) {
        SerializedTree stree = SerializedTree.parseSTree(source);
        ObjectArrayList matches = stree.matches(this.treeRegexAST);
        SerializedTree mod = this.mod.replace(matches);
        return mod;
    }
}
