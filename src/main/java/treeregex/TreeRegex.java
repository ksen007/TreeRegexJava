package treeregex;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.regex.Pattern;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/28/16
 * Time: 10:02 AM
 */
public class TreeRegex extends SerializedTree {
    public boolean isAt;
    public boolean isContext;

    public TreeRegex(boolean isContext, boolean isAt) {
        this.isContext = isContext;
        this.isAt = isAt;
    }

    public static int LT, RT, AT;
    public static String LTS, RTS, ATS;
    public static StringTokenScanner treeRegexScanner;
    public static TreeRegex ATNode;
    public static StringTokenScanner treeRegexASTEscaper;

    static {
        TreeRegex.LT = --cntr;
        --cntr;
        TreeRegex.RT = --cntr;
        --cntr;
        TreeRegex.AT = --cntr;
        --cntr;
        TreeRegex.LTS = "(*";
        TreeRegex.RTS = "*)";
        TreeRegex.ATS = "@";
        TreeRegex.treeRegexScanner = new StringTokenScanner(SerializedTree.FSS);
        TreeRegex.treeRegexScanner.addString(SerializedTree.LB, SerializedTree.LBS, null);
        TreeRegex.treeRegexScanner.addString(SerializedTree.RB, SerializedTree.RBS, null);
        TreeRegex.treeRegexScanner.addString(TreeRegex.LT, TreeRegex.LTS, null);
        TreeRegex.treeRegexScanner.addString(TreeRegex.RT, TreeRegex.RTS, null);
        TreeRegex.treeRegexScanner.addString(TreeRegex.AT, TreeRegex.ATS, null);
        TreeRegex.ATNode = new TreeRegex(false, true);

        treeRegexASTEscaper = new StringTokenScanner();
        treeRegexASTEscaper.addString(SerializedTree.LB, SerializedTree.LBS, injectEscapeChar(SerializedTree.FSS, SerializedTree.LBS));
        treeRegexASTEscaper.addString(SerializedTree.RB, SerializedTree.RBS, injectEscapeChar(SerializedTree.FSS, SerializedTree.RBS));
        treeRegexASTEscaper.addString(SerializedTree.FS, "" + SerializedTree.FSS, injectEscapeChar(SerializedTree.FSS, "" + SerializedTree.FSS));
        treeRegexASTEscaper.addString(TreeRegex.LT, TreeRegex.LTS, injectEscapeChar(SerializedTree.FSS, TreeRegex.LTS));
        treeRegexASTEscaper.addString(TreeRegex.RT, TreeRegex.RTS, injectEscapeChar(SerializedTree.FSS, TreeRegex.RTS));
        treeRegexASTEscaper.addString(TreeRegex.AT, TreeRegex.ATS, injectEscapeChar(SerializedTree.FSS, TreeRegex.ATS));

    }

    private static void pushString(TreeRegex current, StringBuilder sb, boolean isRegex) {
        if (sb.length() > 0) {
            current.addChild(isRegex ? (Pattern.compile(sb.toString())) : (sb.toString()));
            sb.delete(0, sb.length());
        }
    }


    public static TreeRegex parse(String source, boolean isRegex) {
        TreeRegex current;
        TreeRegex root = current = new TreeRegex(false, false);
        StringBuilder sb = new StringBuilder();
        Stack<TreeRegex> stack = new ObjectArrayList<>();
        TreeRegex prev;

        TreeRegex.treeRegexScanner.setStream(source);
        int token = TreeRegex.treeRegexScanner.nextToken();
        while (token != StringTokenScanner.EOF) {
            if (token == SerializedTree.LB) {
                pushString(current, sb, isRegex);
                stack.push(current);
                current = new TreeRegex(false, false);
            } else if (token == SerializedTree.RB) {
                if (stack.isEmpty() || current.isContext) {
                    throw new Error("Unbalanced " + SerializedTree.RBS + " after " + TreeRegex.treeRegexScanner.scannedPrefix());
                }
                pushString(current, sb, isRegex);
                prev = current;
                current = stack.pop();
                current.addChild(prev);
            } else if (token == TreeRegex.LT) {
                pushString(current, sb, isRegex);
                stack.push(current);
                current = new TreeRegex(true, false);
            } else if (token == TreeRegex.RT) {
                if (stack.isEmpty() || !current.isContext) {
                    throw new Error("Unbalanced " + TreeRegex.RTS + " after " + TreeRegex.treeRegexScanner.scannedPrefix());
                }
                pushString(current, sb, isRegex);
                prev = current;
                current = stack.pop();
                current.addChild(prev);
            } else if (token == TreeRegex.AT) {
                pushString(current, sb, isRegex);
                current.addChild(TreeRegex.ATNode);
            } else {
                sb.append(TreeRegex.treeRegexScanner.lexeme);
            }
            token = TreeRegex.treeRegexScanner.nextToken();
        }
        pushString(current, sb, isRegex);
        if (root != current) {
            throw new Error("Unbalanced (% in " + source);
        }
        root.finalizeAST();
        return root;
    }

    public String toString() {
        StringBuilder ret = new StringBuilder();
        for (int i = 0; i < this.children.length; i++) {
            Object child = this.children[i];
            if (child instanceof TreeRegex && !((TreeRegex) child).isAt) {
                ret.append(((TreeRegex) child).isContext ? TreeRegex.LTS : SerializedTree.LBS);
                ret.append(child.toString());
                ret.append(((TreeRegex) child).isContext ? TreeRegex.RTS : SerializedTree.RBS);
            } else if (child instanceof TreeRegex && ((TreeRegex) child).isAt) {
                ret.append("@");
            } else {
                ret.append(treeRegexEscape(child + ""));
            }
        }
        return ret.toString();
    }

    public static String treeRegexEscape(String str) {
        treeRegexASTEscaper.setStream(str);
        int token = treeRegexASTEscaper.nextToken();
        StringBuilder sb = new StringBuilder();
        while (token !=  StringTokenScanner.EOF) {
            sb.append(treeRegexASTEscaper.lexeme);
            token = treeRegexASTEscaper.nextToken();
        }
        return sb.toString();
    }

}
