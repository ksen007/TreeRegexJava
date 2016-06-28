package treeregex;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/28/16
 * Time: 8:13 AM
 */
public class UniversalASTWithHole {
    UniversalAST top;
    UniversalAST parent;
    int i;

    public UniversalASTWithHole(UniversalAST top, UniversalAST parent, int i) {
        this.parent = parent;
        this.top = top;
        this.i = i;
    }

    public String toString() {
        if (this.parent != null) {
            return this.top + " - " + this.parent.children[this.i];
        } else {
            return "Empty";
        }
    }


}
