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
public class UniversalAST {
    public Object[] children;
    private ObjectArrayList tmpChildren;


    public UniversalAST() {
        tmpChildren = new ObjectArrayList<>();
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
            if (child instanceof UniversalAST) {
                ((UniversalAST) child).finalizeAST();
            }
        }
    }

    ;


    public String toString() {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < this.children.length; i++) {
            Object child = this.children[i];
            if (child instanceof UniversalAST) {
                ret.append(UniversalAST.LBS);
                ret.append(child.toString());
                ret.append(UniversalAST.RBS);
            } else {
                ret.append(universalASTEscape(child + ""));
            }
        }
        return ret.toString();
    }

    ;

    public String toSourceString() {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < this.children.length; i++) {
            Object child = this.children[i];
            if (child instanceof UniversalAST) {
                ret.append(((UniversalAST) child).toSourceString());
            } else {
                ret.append(child + "");
            }
        }
        return ret.toString();
    }

    ;

    public String toSourceStringClipped() {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < this.children.length; i++) {
            Object child = this.children[i];
            if (child instanceof UniversalAST) {
                String tmp = ((UniversalAST) child).toSourceStringClipped();
                ret.append(tmp.substring(tmp.indexOf(' ') + 1));
            } else {
                ret.append((child + ""));
            }
        }
        return ret.toString();
    }

    ;

    public void addChild(Object child) {
        this.tmpChildren.push(child);
    }

    ;


    public boolean matchContext(TreeRegexAST patternStar, ObjectArrayList ret, UniversalAST top, UniversalAST parent, int index) {
        int oldlen = ret.size();
        ret.push(new UniversalASTWithHole(top, parent, index));
        boolean ret2 = this.matchExact(patternStar, ret);
        if (ret2) {
            return true;
        }
        ret.removeElements(oldlen, ret.size());

        for (int i = 0; i < this.children.length; i++) {
            if (this.children[i] instanceof UniversalAST) {
                ret2 = ((UniversalAST) this.children[i]).matchContext(patternStar, ret, top, this, i);
                if (ret2) {
                    return true;
                }
                ret.removeElements(oldlen, ret.size());
            }
        }
        return false;
    }

    public int matchStar(TreeRegexAST pattern, ObjectArrayList ret, int k) {
        int ktmp = k;
        do {
            k = ktmp;
            ktmp = this.matchList(pattern, ret, k);
        } while (ktmp != -1);
        return k;
    }

    public int matchAlternation(TreeRegexAST pattern, ObjectArrayList ret, int k) {
        for (int i = 0; i < pattern.children.length; i++) {
            Object o = pattern.children[i];
            int ktmp = this.matchList((TreeRegexAST) o, ret, k); // @todo type cast could fail
            if (ktmp != -1) {
                return ktmp;
            }
        }
        return -1;
    }

    public int matchList(TreeRegexAST pattern, ObjectArrayList ret, int k) {
        int oldlen = ret.size();

        for (int i = 0; i < pattern.children.length && k != -1; i++) {
            Object o = pattern.children[i];
            if (o instanceof Pattern) {
                String str;
                if (this.children[k] instanceof UniversalAST || k >= this.children.length) {
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
            } else if (o instanceof TreeRegexAST) {
                TreeRegexAST t = (TreeRegexAST) o;
                if (t.isStar) {
                    k = this.matchStar(t, ret, k);
                } else if (t.isAlternation) {
                    k = this.matchAlternation(t, ret, k);
                } else if (this.children[k] instanceof UniversalAST) {
                    UniversalAST ast = (UniversalAST) this.children[k];
                    if (t.isAt) {
                        ret.push(ast);
                        k++;
                    } else if (t.isContext) {
                        boolean res = ast.matchContext(t, ret, ast, null, 0);
                        if (res) k++;
                        else k = -1;
                    } else {
                        boolean res = ast.matchExact(t, ret);
                        if (res) k++;
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

    public boolean matchExact(TreeRegexAST pattern, ObjectArrayList ret) {
        int k = 0;
        int oldlen = ret.size();

        k = this.matchList(pattern, ret, k);
        if (this.children.length != k) {
            ret.removeElements(oldlen, ret.size());
            return false;
        }
        return true;
    }

    public ObjectArrayList matches(TreeRegexAST pattern) {
        ObjectArrayList ret = new ObjectArrayList();
        ret.push(null);
        if (this.matchExact(pattern, ret)) {
            return ret;
        } else {
            return null;
        }
    }


    public void replaceInString(String template, ObjectArrayList subs, ObjectArrayList stack) {
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
                        if (num >= subs.size() || num < 0) {
                            throw new Error("Capture at index " + (number.toString() + 1) + " not found in " + subs);
                        } else {
                            Object sub = subs.get(num);
                            if (sub instanceof UniversalAST || sub instanceof UniversalASTWithHole) {
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


    public UniversalAST replace(ObjectArrayList subs) {
        ObjectArrayList stack = new ObjectArrayList();
        int count = 0;
        for (int i = 0; i < this.children.length; i++) {
            Object o = this.children[i];
            if (o instanceof UniversalAST) {
                stack.push(((UniversalAST)o).replace(subs));
            } else {
                this.replaceInString((String)o, subs, stack);
            }
        }
        for (int j = stack.size() - 1; j >= 0; j--) {
            Object stackj = stack.get(j);
            if (stackj instanceof UniversalASTWithHole) {
                UniversalASTWithHole hole = (UniversalASTWithHole)stackj;
                if (j == stack.size() - 1) {
                    throw new Error("No more serialized tree left to fill a hole.");
                }
                if (!(stack.get(j+1) instanceof UniversalAST)) {
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
        UniversalAST tmp = new UniversalAST();
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

    private static String universalASTEscape(String str) {
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
        UniversalAST.LBS = "(%";
        UniversalAST.RBS = "%)";
        UniversalAST.FSS = '/';
        UniversalAST.LB = cntr;
        --cntr;
        UniversalAST.RB = --cntr;
        --cntr;
        UniversalAST.FS = --cntr;
        --cntr;
        UniversalAST.sTreeScanner = new StringTokenScanner(UniversalAST.FSS);
        UniversalAST.sTreeScanner.addString(UniversalAST.LB, UniversalAST.LBS, null);
        UniversalAST.sTreeScanner.addString(UniversalAST.RB, UniversalAST.RBS, null);
        universalASTEscaper.addString(UniversalAST.LB, UniversalAST.LBS, injectEscapeChar(UniversalAST.FSS, UniversalAST.LBS));
        universalASTEscaper.addString(UniversalAST.RB, UniversalAST.RBS, injectEscapeChar(UniversalAST.FSS, UniversalAST.RBS));
        universalASTEscaper.addString(UniversalAST.FS, "" + UniversalAST.FSS, injectEscapeChar(UniversalAST.FSS, "" + UniversalAST.FSS));
    }

    private static void addString(UniversalAST current, StringBuilder sb) {
        if (sb.length() > 0) {
            current.addChild(sb.toString());
            sb.delete(0, sb.length());
        }
    }

    public static UniversalAST parseSTree(String source) {
        UniversalAST current;
        UniversalAST root = current = new UniversalAST();
        StringBuilder sb = new StringBuilder();
        Stack<UniversalAST> stack = new ObjectArrayList<>();

        UniversalAST.sTreeScanner.setStream(source);
        int token = UniversalAST.sTreeScanner.nextToken();
        while (token != StringTokenScanner.EOF) {
            if (token == UniversalAST.LB) {
                addString(current, sb);
                stack.push(current);
                current = new UniversalAST();
            } else if (token == UniversalAST.RB) {
                if (stack.isEmpty()) {
                    throw new Error("Unbalanced %) in " + UniversalAST.sTreeScanner.scannedPrefix());
                }
                addString(current, sb);
                UniversalAST prev = current;
                current = stack.pop();
                current.addChild(prev);
            } else {
                sb.append(UniversalAST.sTreeScanner.lexeme);
            }
            token = UniversalAST.sTreeScanner.nextToken();
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
