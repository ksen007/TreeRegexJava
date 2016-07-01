package treeregex;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/29/16
 * Time: 1:09 PM
 */
public interface Modifier {
    public Object[] apply(Object[] matches, Object2ObjectMap<String, Object> state, Object2ObjectMap<String, Object> args, Object2ObjectMap<String, Object> childArgs);
}
