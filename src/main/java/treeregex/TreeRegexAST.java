package treeregex;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.regex.Pattern;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/28/16
 * Time: 10:02 AM
 */
public class TreeRegexAST extends UniversalAST {
    public boolean isStar;
    public boolean isAlternation;
    public boolean isAt;
    public boolean isContext;

    public TreeRegexAST(boolean isContext, boolean isAt, boolean isStar, boolean isAlternation) {
        this.isContext = isContext;
        this.isAt = isAt;
        this.isStar = isStar;
        this.isAlternation = isAlternation;
    }

    public static int LT, RT, LL, RL, LA, RA, AT;
    public static String LTS, RTS, LLS, RLS, LAS, RAS, ATS;
    public static StringTokenScanner treeRegexScanner;
    public static TreeRegexAST ATNode;
    public static StringTokenScanner treeRegexASTEscaper;

    static {
        TreeRegexAST.LT = --cntr;
        --cntr;
        TreeRegexAST.RT = --cntr;
        --cntr;
        TreeRegexAST.LL = --cntr;
        --cntr;
        TreeRegexAST.RL = --cntr;
        --cntr;
        TreeRegexAST.LA = --cntr;
        --cntr;
        TreeRegexAST.RA = --cntr;
        --cntr;
        TreeRegexAST.AT = --cntr;
        --cntr;
        TreeRegexAST.LTS = "(*";
        TreeRegexAST.RTS = "*)";
        TreeRegexAST.LLS = "(**";
        TreeRegexAST.RLS = "**)";
        TreeRegexAST.LAS = "(|";
        TreeRegexAST.RAS = "|)";
        TreeRegexAST.ATS = "@";
        TreeRegexAST.treeRegexScanner = new StringTokenScanner(UniversalAST.FSS);
        TreeRegexAST.treeRegexScanner.addString(UniversalAST.LB, UniversalAST.LBS, null);
        TreeRegexAST.treeRegexScanner.addString(UniversalAST.RB, UniversalAST.RBS, null);
        TreeRegexAST.treeRegexScanner.addString(TreeRegexAST.LT, TreeRegexAST.LTS, null);
        TreeRegexAST.treeRegexScanner.addString(TreeRegexAST.RT, TreeRegexAST.RTS, null);
        TreeRegexAST.treeRegexScanner.addString(TreeRegexAST.LL, TreeRegexAST.LLS, null);
        TreeRegexAST.treeRegexScanner.addString(TreeRegexAST.RL, TreeRegexAST.RLS, null);
        TreeRegexAST.treeRegexScanner.addString(TreeRegexAST.LA, TreeRegexAST.LAS, null);
        TreeRegexAST.treeRegexScanner.addString(TreeRegexAST.RA, TreeRegexAST.RAS, null);
        TreeRegexAST.treeRegexScanner.addString(TreeRegexAST.AT, TreeRegexAST.ATS, null);
        TreeRegexAST.ATNode = new TreeRegexAST(false, true, false, false);

        treeRegexASTEscaper = new StringTokenScanner();
        treeRegexASTEscaper.addString(UniversalAST.LB, UniversalAST.LBS, injectEscapeChar(UniversalAST.FSS,UniversalAST.LBS));
        treeRegexASTEscaper.addString(UniversalAST.RB, UniversalAST.RBS, injectEscapeChar(UniversalAST.FSS,UniversalAST.RBS));
        treeRegexASTEscaper.addString(UniversalAST.FS, "" + UniversalAST.FSS, injectEscapeChar(UniversalAST.FSS, "" + UniversalAST.FSS));
        treeRegexASTEscaper.addString(TreeRegexAST.LT, TreeRegexAST.LTS, injectEscapeChar(UniversalAST.FSS, TreeRegexAST.LTS));
        treeRegexASTEscaper.addString(TreeRegexAST.RT, TreeRegexAST.RTS, injectEscapeChar(UniversalAST.FSS, TreeRegexAST.RTS));
        treeRegexASTEscaper.addString(TreeRegexAST.LL, TreeRegexAST.LLS, injectEscapeChar(UniversalAST.FSS, TreeRegexAST.LLS));
        treeRegexASTEscaper.addString(TreeRegexAST.RL, TreeRegexAST.RLS, injectEscapeChar(UniversalAST.FSS, TreeRegexAST.RLS));
        treeRegexASTEscaper.addString(TreeRegexAST.LA, TreeRegexAST.LAS, injectEscapeChar(UniversalAST.FSS, TreeRegexAST.LAS));
        treeRegexASTEscaper.addString(TreeRegexAST.RA, TreeRegexAST.RAS, injectEscapeChar(UniversalAST.FSS, TreeRegexAST.RAS));
        treeRegexASTEscaper.addString(TreeRegexAST.AT, TreeRegexAST.ATS, injectEscapeChar(UniversalAST.FSS, TreeRegexAST.ATS));

    }

    private static void pushString(TreeRegexAST current, StringBuilder sb, boolean isRegex) {
        if (sb.length() > 0) {
            current.addChild(isRegex ? (Pattern.compile(sb.toString())) : (sb.toString()));
            sb.delete(0, sb.length());
        }
    }


    public static TreeRegexAST parseTreeRegex(String source, boolean isRegex) {
        TreeRegexAST current;
        TreeRegexAST root = current = new TreeRegexAST(false, false, false, false);
        StringBuilder sb = new StringBuilder();
        Stack<TreeRegexAST> stack = new ObjectArrayList<>();
        TreeRegexAST prev;

        TreeRegexAST.treeRegexScanner.setStream(source);
        int token = TreeRegexAST.treeRegexScanner.nextToken();
        while (token != StringTokenScanner.EOF) {
            if (token == UniversalAST.LB) {
                pushString(current, sb, isRegex);
                stack.push(current);
                current = new TreeRegexAST(false, false, false, false);
            } else if (token == UniversalAST.RB) {
                if (stack.isEmpty() || current.isContext) {
                    throw new Error("Unbalanced " + UniversalAST.RBS + " after " + TreeRegexAST.treeRegexScanner.scannedPrefix());
                }
                pushString(current, sb, isRegex);
                prev = current;
                current = stack.pop();
                current.addChild(prev);
            } else if (token == TreeRegexAST.LT) {
                pushString(current, sb, isRegex);
                stack.push(current);
                current = new TreeRegexAST(true, false, false, false);
            } else if (token == TreeRegexAST.RT) {
                if (stack.isEmpty() || !current.isContext) {
                    throw new Error("Unbalanced " + TreeRegexAST.RTS + " after " + TreeRegexAST.treeRegexScanner.scannedPrefix());
                }
                pushString(current, sb, isRegex);
                prev = current;
                current = stack.pop();
                current.addChild(prev);
            } else if (token == TreeRegexAST.LL) {
                pushString(current, sb, isRegex);
                stack.push(current);
                current = new TreeRegexAST(false, false, true, false);
            } else if (token == TreeRegexAST.RL) {
                if (stack.isEmpty() || !current.isStar) {
                    throw new Error("Unbalanced " + TreeRegexAST.RLS + " after " + TreeRegexAST.treeRegexScanner.scannedPrefix());
                }
                pushString(current, sb, isRegex);
                prev = current;
                current = stack.pop();
                current.addChild(prev);
            } else if (token == TreeRegexAST.LA) {
                pushString(current, sb, isRegex);
                stack.push(current);
                current = new TreeRegexAST(false, false, false, true);
            } else if (token == TreeRegexAST.RA) {
                if (stack.isEmpty() || !current.isAlternation) {
                    throw new Error("Unbalanced " + TreeRegexAST.RAS + " after " + TreeRegexAST.treeRegexScanner.scannedPrefix());
                }
                pushString(current, sb, isRegex);
                prev = current;
                current = stack.pop();
                current.addChild(prev);
            } else if (token == TreeRegexAST.AT) {
                pushString(current, sb, isRegex);
                current.addChild(TreeRegexAST.ATNode);
            } else {
                sb.append(TreeRegexAST.treeRegexScanner.lexeme);
            }
            token = TreeRegexAST.treeRegexScanner.nextToken();
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
            if (child instanceof TreeRegexAST && !((TreeRegexAST) child).isAt) {
                ret.append(((TreeRegexAST) child).isContext ? TreeRegexAST.LTS : UniversalAST.LBS);
                ret.append(child.toString());
                ret.append(((TreeRegexAST) child).isContext ? TreeRegexAST.RTS : UniversalAST.RBS);
            } else if (child instanceof TreeRegexAST && ((TreeRegexAST) child).isAt) {
                ret.append("@");
            } else {
                ret.append(treeRegexASTEscape(child + ""));
            }
        }
        return ret.toString();
    }

    public static String treeRegexASTEscape(String str) {
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
