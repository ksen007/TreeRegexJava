package treeregex;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/28/16
 * Time: 8:15 AM
 */
public class SerializedTree {
    public Object[] children;
    private ObjectArrayList tmpChildren;


    public SerializedTree() {
        tmpChildren = new ObjectArrayList<>();
    }

    public SerializedTree(int nChild) {
        children = new Object[nChild];
    }

    private void finalizeNode() {
        if (tmpChildren != null) {
            children = tmpChildren.toArray();
            tmpChildren = null;
        }
    }

    public void finalizeAST() {
        finalizeNode();
        for (int i = 0; i < this.children.length; i++) {
            Object child = this.children[i];
            if (child instanceof SerializedTree) {
                ((SerializedTree) child).finalizeAST();
            }
        }
    }

    ;


    public String toString() {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < this.children.length; i++) {
            Object child = this.children[i];
            if (child instanceof SerializedTree) {
                ret.append(SerializedTree.LBS);
                ret.append(child.toString());
                ret.append(SerializedTree.RBS);
            } else {
                ret.append(serializedTreeEscape(child + ""));
            }
        }
        return ret.toString();
    }

    ;

    public String toSourceString() {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < this.children.length; i++) {
            Object child = this.children[i];
            if (child instanceof SerializedTree) {
                ret.append(((SerializedTree) child).toSourceString());
            } else {
                ret.append(child + "");
            }
        }
        return ret.toString();
    }

    ;

    private String toSourceStringClipped() {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < this.children.length; i++) {
            Object child = this.children[i];
            if (child instanceof SerializedTree) {
                String tmp = ((SerializedTree) child).toSourceStringClipped();
                ret.append(tmp.substring(tmp.indexOf(' ') + 1));
            } else {
                ret.append((child + ""));
            }
        }
        return ret.toString();
    }

    ;

    void addChild(Object child) {
        this.tmpChildren.push(child);
    }


    private boolean matchContext(TreeRegex patternStar, ObjectArrayList ret, SerializedTree top, SerializedTree parent, int index) {
        int oldlen = ret.size();
        ret.push(new SerializedTreeWithHole(top, parent, index));
        int ret2 = this.matchExactOrPartial(patternStar, 0, true, ret);
        if (ret2 != -1) {
            return true;
        }
        ret.removeElements(oldlen, ret.size());

        for (int i = 0; i < this.children.length; i++) {
            if (this.children[i] instanceof SerializedTree) {
                boolean ret3 = ((SerializedTree) this.children[i]).matchContext(patternStar, ret, top, this, i);
                if (ret3) {
                    return true;
                }
                ret.removeElements(oldlen, ret.size());
            }
        }
        return false;
    }

    private int matchList(TreeRegex pattern, ObjectArrayList ret, int k) {
        int oldlen = ret.size();

        for (int i = 0; i < pattern.children.length && k != -1; i++) {
            Object o = pattern.children[i];
            if (o instanceof Pattern) {
                String str;
                if (k >= this.children.length || this.children[k] instanceof SerializedTree) {
                    k--;
                    str = "";
                } else {
                    str = this.children[k] + "";
                }
                Pattern r = (Pattern) o;
                Matcher matcher = r.matcher(str);
                if (matcher.matches()) {
                    int gc = matcher.groupCount() + 1;
                    for (int j = 1; j < gc; j++) {
                        ret.push(matcher.group(j));
                    }
                    k++;
                } else {
                    k = -1;
                }
            } else if (o instanceof TreeRegex) {
                TreeRegex t = (TreeRegex) o;
                if (this.children[k] instanceof SerializedTree) {
                    SerializedTree ast = (SerializedTree) this.children[k];
                    if (t.isAt) {
                        ret.push(ast);
                        k++;
                    } else if (t.isContext) {
                        boolean res = ast.matchContext(t, ret, ast, null, 0);
                        if (res) k++;
                        else k = -1;
                    } else {
                        int res = ast.matchExactOrPartial(t, 0, true, ret);
                        if (res != -1) k++;
                        else k = -1;
                    }
                } else {
                    k = -1;
                }
            } else if (!o.equals(this.children[k])) {
                k = -1;
            }
        }
        if (k == -1) {
            ret.removeElements(oldlen, ret.size());
        }
        return k;
    }

    public int matchExactOrPartial(TreeRegex pattern, int k, boolean checkExact, ObjectArrayList ret) {
        int oldlen = ret.size();

        k = this.matchList(pattern, ret, k);
        if (k == -1 || (checkExact && this.children.length != k)) {
            ret.removeElements(oldlen, ret.size());
            return -1;
        }
        return k;
    }

    public int length() {
        return children.length;
    }

    public Object[] matches(TreeRegex pattern) {
        ObjectArrayList ret = new ObjectArrayList();
        ret.push(null);
        if (this.matchExactOrPartial(pattern, 0, true, ret)!= -1) {
            return ret.toArray();
        } else {
            return null;
        }
    }


    public void replaceInString(String template, Object[] subs, ObjectArrayList stack) {
        StringBuilder sb = new StringBuilder();
        int len = template.length();
        int prev = 0;
        char c;
        for (int i = 0; i < len; i++) {
            c = template.charAt(i);
            if (prev == '$') {
                if (c == '$') {
                    sb.append(c);
                    prev = 0;
                } else {
                    StringBuilder number = new StringBuilder();
                    while (c >= '0' && c <= '9') {
                        number.append(c);
                        i++;
                        if (i < len)
                            c = template.charAt(i);
                        else
                            c = 0;
                    }
                    i--;
                    if (number.length() > 0) {
                        int num = Integer.parseInt(number.toString());
                        if (num >= subs.length || num < 0) {
                            throw new Error("Capture at index " + (number.toString() + 1) + " not found in " + subs);
                        } else {
                            Object sub = subs[num];
                            if (sub instanceof SerializedTree || sub instanceof SerializedTreeWithHole) {
                                if (sb.length() > 0) {
                                    stack.push(sb.toString());
                                    sb.delete(0, sb.length());
                                }
                                stack.push(sub);
                            } else {
                                sb.append(sub);
                            }
                        }
                        prev = 0;
                    } else {
                        throw new Error("Expecting number or $ after $ in replacement string \"" + template + "\"");
                    }
                }
            } else if (c == '$') {
                prev = c;
            } else {
                sb.append(c);
                prev = 0;
            }
        }
        if (sb.length() > 0) {
            stack.push(sb.toString());
            sb.delete(0, sb.length());
        }
    }


    public SerializedTree replace(Object[] subs) {
        ObjectArrayList stack = new ObjectArrayList();
        int count = 0;
        for (int i = 0; i < this.children.length; i++) {
            Object o = this.children[i];
            if (o instanceof SerializedTree) {
                stack.push(((SerializedTree)o).replace(subs));
            } else {
                this.replaceInString((String)o, subs, stack);
            }
        }
        for (int j = stack.size() - 1; j >= 0; j--) {
            Object stackj = stack.get(j);
            if (stackj instanceof SerializedTreeWithHole) {
                SerializedTreeWithHole hole = (SerializedTreeWithHole)stackj;
                if (j == stack.size() - 1) {
                    throw new Error("No more serialized tree left to fill a hole.");
                }
                if (!(stack.get(j+1) instanceof SerializedTree)) {
                    throw new Error("A hole cannot be filled with " + stack.get(j + 1));
                }
                if (hole.parent != null) {
                    stack.set(j,hole.top);
                    hole.parent.children[hole.i] = stack.get(j+1);
                } else {
                    stack.set(j, stack.get(j + 1));
                }
                stack.set(j + 1, null);
            } else {
                count++;
            }
        }
        Object stack2[] = new Object[count];
        int i = 0;
        for (int j = 0; j < stack.size(); j++) {
            if (stack.get(j) != null) {
                stack2[i] = stack.get(j);
                i++;
            }
        }
        SerializedTree tmp = new SerializedTree();
        tmp.children = stack2;
        tmp.tmpChildren = null;
        return tmp;
    }

    public static int LB, RB, FS;
    public static String LBS, RBS;
    public static char FSS;
    public static StringTokenScanner sTreeScanner;
    public static int cntr = -2;
    public static StringTokenScanner universalASTEscaper = new StringTokenScanner();

    private static String serializedTreeEscape(String str) {
        universalASTEscaper.setStream(str);
        int token = universalASTEscaper.nextToken();
        StringBuilder sb = new StringBuilder();
        while (token != StringTokenScanner.EOF) {
            sb.append(universalASTEscaper.lexeme);
            token = universalASTEscaper.nextToken();
        }
        return sb.toString();
    }

    public static String injectEscapeChar(char escapeChar, String source) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < source.length(); i++) {
            sb.append(escapeChar).append(source.charAt(i));
        }
        return sb.toString();
    }

    static {
        SerializedTree.LBS = "(%";
        SerializedTree.RBS = "%)";
        SerializedTree.FSS = '/';
        SerializedTree.LB = cntr;
        --cntr;
        SerializedTree.RB = --cntr;
        --cntr;
        SerializedTree.FS = --cntr;
        --cntr;
        SerializedTree.sTreeScanner = new StringTokenScanner(SerializedTree.FSS);
        SerializedTree.sTreeScanner.addString(SerializedTree.LB, SerializedTree.LBS, null);
        SerializedTree.sTreeScanner.addString(SerializedTree.RB, SerializedTree.RBS, null);
        universalASTEscaper.addString(SerializedTree.LB, SerializedTree.LBS, injectEscapeChar(SerializedTree.FSS, SerializedTree.LBS));
        universalASTEscaper.addString(SerializedTree.RB, SerializedTree.RBS, injectEscapeChar(SerializedTree.FSS, SerializedTree.RBS));
        universalASTEscaper.addString(SerializedTree.FS, "" + SerializedTree.FSS, injectEscapeChar(SerializedTree.FSS, "" + SerializedTree.FSS));
    }

    private static void addString(SerializedTree current, StringBuilder sb) {
        if (sb.length() > 0) {
            current.addChild(sb.toString());
            sb.delete(0, sb.length());
        }
    }

    public static SerializedTree parse(String source) {
        SerializedTree current;
        SerializedTree root = current = new SerializedTree();
        StringBuilder sb = new StringBuilder();
        Stack<SerializedTree> stack = new ObjectArrayList<>();

        SerializedTree.sTreeScanner.setStream(source);
        int token = SerializedTree.sTreeScanner.nextToken();
        while (token != StringTokenScanner.EOF) {
            if (token == SerializedTree.LB) {
                addString(current, sb);
                stack.push(current);
                current = new SerializedTree();
            } else if (token == SerializedTree.RB) {
                if (stack.isEmpty()) {
                    throw new Error("Unbalanced %) in " + SerializedTree.sTreeScanner.scannedPrefix());
                }
                addString(current, sb);
                SerializedTree prev = current;
                current = stack.pop();
                current.addChild(prev);
            } else {
                sb.append(SerializedTree.sTreeScanner.lexeme);
            }
            token = SerializedTree.sTreeScanner.nextToken();
        }
        addString(current, sb);
        if (root != current) {
            throw new Error("Unbalanced (% in " + source);
        }
        root.finalizeAST();
        return root;
    }

    ;
}
