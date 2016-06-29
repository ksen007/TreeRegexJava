package treeregex;

import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/29/16
 * Time: 1:09 PM
 */
public interface Modifier {
    ObjectArrayList apply(ObjectArrayList matches, Object2ObjectRBTreeMap<String, Object> state, Object2ObjectRBTreeMap<String, Object> args, Object2ObjectRBTreeMap<String, Object> ret);
}
