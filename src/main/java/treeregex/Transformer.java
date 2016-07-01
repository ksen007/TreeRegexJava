package treeregex;

import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/29/16
 * Time: 1:14 PM
 */
public class Transformer {
    ObjectArrayList<ATransformer> preTransformers;
    ObjectArrayList<ATransformer> postTransformers;


    public Transformer() {
        this.preTransformers = new ObjectArrayList<>();
        this.postTransformers = new ObjectArrayList<>();
    }

    public void addTransformer(Predicate predicate, String pattern, Modifier modifier, String replacer, boolean isRegex, boolean isPartial, boolean isPre) {
        if (isPre) {
            this.preTransformers.push(new ATransformer(predicate, pattern, modifier, replacer, isRegex, isPartial));
        } else {
            this.postTransformers.push(new ATransformer(predicate, pattern, modifier, replacer, isRegex, isPartial));
        }
    }

    public void addTransformer(Predicate predicate, String pattern, Modifier modifier, String replacer, boolean isRegex, boolean isPre) {
        if (isPre) {
            this.preTransformers.push(new ATransformer(predicate, pattern, modifier, replacer, isRegex, false));
        } else {
            this.postTransformers.push(new ATransformer(predicate, pattern, modifier, replacer, isRegex, false));
        }
    }

    private SerializedTree matchAndReplace(ObjectArrayList<ATransformer> transformers,
                                           SerializedTree source,
                                           Object2ObjectRBTreeMap<String, Object> state,
                                           Object2ObjectRBTreeMap<String, Object> args,
                                           Object2ObjectRBTreeMap<String, Object> childArgs) {
        for (ATransformer t : transformers) {
            if (t.predicate == null || t.predicate.apply(state, args)) {
                if (t.isPartial) {
                    int slen = source.length();
                    for (int i = 0; i < slen; i++) {
                        ObjectArrayList ret2 = new ObjectArrayList();
                        ret2.push(null);
                        int to = source.matchExactOrPartial(t.pattern, i, false, ret2);
                        Object[] matches = (to != -1) ? ret2.toArray() : null;
                        if (t.modifier != null) {
                            matches = t.modifier.apply(matches, state, args, childArgs);
                        }
                        if (t.replacer != null && matches != null) {
                            SerializedTree tmp = t.replacer.replace(matches);
                            int len = to-i;
                            int len2 = tmp.length();
                            SerializedTree tmp2 = new SerializedTree(slen - len + len2);
                            for (int j = 0; j < i; j++) {
                                tmp2.children[j] = source.children[j];
                            }
                            for (int j = 0; j < len2; j++) {
                                tmp2.children[j + i] = tmp.children[j];
                            }
                            for (int j = to; j < slen; j++) {
                                tmp2.children[j + len2 - len] = source.children[j];
                            }
                            source = tmp2;
                        }
                    }
                } else {
                    Object[] matches = source.matches(t.pattern);
                    if (t.modifier != null)
                        matches = t.modifier.apply(matches, state, args, childArgs);
                    if (t.replacer != null && matches != null) {
                        source = t.replacer.replace(matches);
                    }
                }
            }
        }
        return source;
    }

    public SerializedTree modify(Object src, Object2ObjectRBTreeMap<String, Object> state, Object2ObjectRBTreeMap<String, Object> args) {
        SerializedTree source;
        if (!(src instanceof SerializedTree)) {
            source = SerializedTree.parse(src.toString());
        } else {
            source = (SerializedTree) src;
        }
        if (state == null) {
            state = new Object2ObjectRBTreeMap<>();
        }
        if (args == null) {
            args = new Object2ObjectRBTreeMap<>();
        }
        Object2ObjectRBTreeMap<String, Object> childArgs = new Object2ObjectRBTreeMap<>();
        source = matchAndReplace(this.preTransformers, source, state, args, childArgs);
        int len = source.children.length;
        for (int i = 0; i < len; i++) {
            if (source.children[i] instanceof SerializedTree)
                source.children[i] = this.modify(source.children[i], state, childArgs);
        }
        source = matchAndReplace(this.postTransformers, source, state, args, childArgs);
        return source;

    }

}
