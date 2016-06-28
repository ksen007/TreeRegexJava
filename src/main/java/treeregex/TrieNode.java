package treeregex;

import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/28/16
 * Time: 7:11 AM
 */
public class TrieNode {
    public boolean isSet = false;
    private Int2ObjectRBTreeMap<TrieNode> children = null;
    public int ID;
    public String s;


    public TrieNode get(int c) {
        TrieNode ret;
        if (this.children == null)
            return null;
        else if ((ret = this.children.get(c)) == null)
            return null;
        else
            return ret;
    }

    public TrieNode getOrCreate(int c) {
        TrieNode ret;
        if (this.children == null) {
            this.children = new Int2ObjectRBTreeMap<>();
        }
        if ((ret = this.children.get(c)) == null) {
            ret = new TrieNode();
            this.children.put(c, ret);
        }
        return ret;
    }
}

