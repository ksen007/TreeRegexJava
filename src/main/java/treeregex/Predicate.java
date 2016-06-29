package treeregex;

import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/29/16
 * Time: 1:09 PM
 */
public interface Predicate {
    boolean apply(Object2ObjectRBTreeMap<String, Object> state, Object2ObjectRBTreeMap<String, Object> args);
}
