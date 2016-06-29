package treeregex;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/29/16
 * Time: 1:09 PM
 */
public class ATransformer {
    Predicate predicate;
    TreeRegexAST pattern;
    Modifier modifier;
    SerializedTree replacer;

    public ATransformer(Predicate predicate, String pattern, Modifier modifier, String replacer, boolean isRegex) {
        this.predicate = predicate;
        this.pattern = TreeRegexAST.parseTreeRegex(pattern, isRegex);
        this.modifier = modifier;
        if (replacer != null)
            this.replacer = SerializedTree.parseSTree(replacer);
    }
}
