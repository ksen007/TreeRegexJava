package treeregex;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/30/16
 * Time: 2:00 PM
 */
public class TransformerModifier implements Modifier {
    Int2ObjectArrayMap<Transformer> transformers;
    Modifier pre;
    Modifier post;

    public TransformerModifier(Modifier pre, Modifier post) {
        transformers = new Int2ObjectArrayMap<Transformer>();
        this.pre = pre;
        this.post = post;
    }

    public TransformerModifier(int i, Predicate predicate, String pattern, Modifier modifier, String replacer, boolean isRegex, boolean isPre, Modifier pre, Modifier post) {
        transformers = new Int2ObjectArrayMap<Transformer>();
        this.pre = pre;
        this.post = post;
        addTransformerAtIndex(i, predicate, pattern, modifier, replacer, isRegex, false, isPre);
    }

    public TransformerModifier(int i, Predicate predicate, String pattern, Modifier modifier, String replacer, boolean isRegex, boolean isPartial, boolean isPre, Modifier pre, Modifier post) {
        transformers = new Int2ObjectArrayMap<Transformer>();
        this.pre = pre;
        this.post = post;
        addTransformerAtIndex(i, predicate, pattern, modifier, replacer, isRegex, isPartial, isPre);
    }

    public void addTransformerAtIndex(int i, Predicate predicate, String pattern, Modifier modifier, String replacer, boolean isRegex, boolean isPartial, boolean isPre) {
        Transformer tmp = transformers.get(i);
        if (tmp == null) {
            tmp = new Transformer();
            transformers.put(i, tmp);
        }
        tmp.addTransformer(predicate, pattern, modifier, replacer, isRegex, isPartial, isPre);
    }

    @Override
    public Object[] apply(Object[] matches, Object2ObjectRBTreeMap<String, Object> state, Object2ObjectRBTreeMap<String, Object> args, Object2ObjectRBTreeMap<String, Object> ret) {
        if (pre != null) {
            matches = pre.apply(matches, state, args, ret);
        }
        int i = 0;
        for (Object match : matches) {
            Transformer tmp = transformers.get(i);
            if (tmp != null) {
                if (match instanceof SerializedTree) {
                    matches[i] = tmp.modify(match, state, ret);
                } else {
                    throw new RuntimeException("Cannot apply modifier to non-serialized tree "+match);
                }
            }
            i++;
        }
        if (post != null) {
            matches = post.apply(matches, state, args, ret);
        }
        return matches;
    }
}
