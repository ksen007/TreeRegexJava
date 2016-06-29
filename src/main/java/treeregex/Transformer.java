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

    public void addTransformer(Predicate predicate, String pattern, Modifier modifier, String replacer, boolean isRegex, boolean isPre) {
        if (isPre) {
            this.preTransformers.push(new ATransformer(predicate, pattern, modifier, replacer, isRegex));
        } else {
            this.postTransformers.push(new ATransformer(predicate, pattern, modifier, replacer, isRegex));
        }
    }

    public SerializedTree modify(Object src, Object2ObjectRBTreeMap<String, Object> state, Object2ObjectRBTreeMap<String, Object> args) {
        SerializedTree source;
        if (!(src instanceof SerializedTree)) {
            source = SerializedTree.parse(src.toString());
        } else {
            source = (SerializedTree)src;
        }
        if (state == null) {
            state = new Object2ObjectRBTreeMap<>();
        }
        if (args == null) {
            args = new Object2ObjectRBTreeMap<>();
        }
        Object2ObjectRBTreeMap<String, Object> ret = new Object2ObjectRBTreeMap<>();
        for (ATransformer t : this.preTransformers) {
            if (t.predicate == null || t.predicate.apply(state, args)) {
                ObjectArrayList matches = source.matches(t.pattern);
                if (t.modifier != null) {
                    matches = t.modifier.apply(matches, state, args, ret);
                }
                if (t.replacer != null && matches != null) {
                    source = t.replacer.replace(matches);
                }
            }
        }
        int len = source.children.length;
        for (int i = 0; i < len; i++) {
            if (source.children[i] instanceof SerializedTree)
                source.children[i] = this.modify(source.children[i], state, ret);
        }
        for (ATransformer t : this.postTransformers) {
            if (t.predicate == null || t.predicate.apply(state, args)) {
                ObjectArrayList matches = source.matches(t.pattern);
                if (t.modifier != null)
                    matches = t.modifier.apply(matches, state, args, ret);
                if (t.replacer != null && matches != null) {
                    source = t.replacer.replace(matches);
                }
            }
        }
        return source;

    }

}
