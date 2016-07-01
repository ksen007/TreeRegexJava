package treeregex;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/29/16
 * Time: 1:09 PM
 */
public class ATransformer {
    Predicate predicate;
    TreeRegex pattern;
    Modifier modifier;
    SerializedTree replacer;
    boolean isPartial;

    public ATransformer(Predicate predicate, String pattern, Modifier modifier, String replacer, boolean isRegex, boolean isPartial) {
        this.predicate = predicate;
        this.pattern = TreeRegex.parse(pattern, isRegex);
        this.modifier = modifier;
        if (replacer != null)
            this.replacer = SerializedTree.parse(replacer);
        this.isPartial = isPartial;
    }
}
