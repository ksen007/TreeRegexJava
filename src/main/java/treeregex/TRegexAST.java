package treeregex;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.regex.Pattern;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/28/16
 * Time: 10:02 AM
 */
public class TRegexAST extends UniversalAST {
    public boolean isStar;
    public boolean isAlternation;
    public boolean isAt;
    public boolean isContext;

    public TRegexAST(boolean isContext, boolean isAt, boolean isStar, boolean isAlternation) {
        this.isContext = isContext;
        this.isAt = isAt;
        this.isStar = isStar;
        this.isAlternation = isAlternation;
    }

    public static int LT, RT, LL, RL, LA, RA, AT;
    public static String LTS, RTS, LLS, RLS, LAS, RAS, ATS;
    public static StringTokenScanner tregexScanner;
    public static TRegexAST ATNode;
    public static StringTokenScanner tregexASTEscaper;

    static {
        TRegexAST.LT = --cntr;
        --cntr;
        TRegexAST.RT = --cntr;
        --cntr;
        TRegexAST.LL = --cntr;
        --cntr;
        TRegexAST.RL = --cntr;
        --cntr;
        TRegexAST.LA = --cntr;
        --cntr;
        TRegexAST.RA = --cntr;
        --cntr;
        TRegexAST.AT = --cntr;
        --cntr;
        TRegexAST.LTS = "(*";
        TRegexAST.RTS = "*)";
        TRegexAST.LLS = "(**";
        TRegexAST.RLS = "**)";
        TRegexAST.LAS = "(|";
        TRegexAST.RAS = "|)";
        TRegexAST.ATS = "@";
        TRegexAST.tregexScanner = new StringTokenScanner(UniversalAST.FSS);
        TRegexAST.tregexScanner.addString(UniversalAST.LB, UniversalAST.LBS, null);
        TRegexAST.tregexScanner.addString(UniversalAST.RB, UniversalAST.RBS, null);
        TRegexAST.tregexScanner.addString(TRegexAST.LT, TRegexAST.LTS, null);
        TRegexAST.tregexScanner.addString(TRegexAST.RT, TRegexAST.RTS, null);
        TRegexAST.tregexScanner.addString(TRegexAST.LL, TRegexAST.LLS, null);
        TRegexAST.tregexScanner.addString(TRegexAST.RL, TRegexAST.RLS, null);
        TRegexAST.tregexScanner.addString(TRegexAST.LA, TRegexAST.LAS, null);
        TRegexAST.tregexScanner.addString(TRegexAST.RA, TRegexAST.RAS, null);
        TRegexAST.tregexScanner.addString(TRegexAST.AT, TRegexAST.ATS, null);
        TRegexAST.ATNode = new TRegexAST(false, true, false, false);

        tregexASTEscaper = new StringTokenScanner();
        tregexASTEscaper.addString(UniversalAST.LB, UniversalAST.LBS, injectEscapeChar(UniversalAST.FSS,UniversalAST.LBS));
        tregexASTEscaper.addString(UniversalAST.RB, UniversalAST.RBS, injectEscapeChar(UniversalAST.FSS,UniversalAST.RBS));
        tregexASTEscaper.addString(UniversalAST.FS, "" + UniversalAST.FSS, injectEscapeChar(UniversalAST.FSS, "" + UniversalAST.FSS));
        tregexASTEscaper.addString(TRegexAST.LT, TRegexAST.LTS, injectEscapeChar(UniversalAST.FSS, TRegexAST.LTS));
        tregexASTEscaper.addString(TRegexAST.RT, TRegexAST.RTS, injectEscapeChar(UniversalAST.FSS, TRegexAST.RTS));
        tregexASTEscaper.addString(TRegexAST.LL, TRegexAST.LLS, injectEscapeChar(UniversalAST.FSS, TRegexAST.LLS));
        tregexASTEscaper.addString(TRegexAST.RL, TRegexAST.RLS, injectEscapeChar(UniversalAST.FSS, TRegexAST.RLS));
        tregexASTEscaper.addString(TRegexAST.LA, TRegexAST.LAS, injectEscapeChar(UniversalAST.FSS, TRegexAST.LAS));
        tregexASTEscaper.addString(TRegexAST.RA, TRegexAST.RAS, injectEscapeChar(UniversalAST.FSS, TRegexAST.RAS));
        tregexASTEscaper.addString(TRegexAST.AT, TRegexAST.ATS, injectEscapeChar(UniversalAST.FSS, TRegexAST.ATS));

    }

    private static void pushString(TRegexAST current, StringBuilder sb, boolean isRegex) {
        if (sb.length() > 0) {
            current.addChild(isRegex ? (Pattern.compile(sb.toString())) : (sb.toString()));
            sb.delete(0, sb.length());
        }
    }


    public static TRegexAST parseTRegex(String source, boolean isRegex) {
        TRegexAST current;
        TRegexAST root = current = new TRegexAST(false, false, false, false);
        StringBuilder sb = new StringBuilder();
        Stack<TRegexAST> stack = new ObjectArrayList<>();
        TRegexAST prev;

        TRegexAST.tregexScanner.setStream(source);
        int token = TRegexAST.tregexScanner.nextToken();
        while (token != StringTokenScanner.EOF) {
            if (token == UniversalAST.LB) {
                pushString(current, sb, isRegex);
                stack.push(current);
                current = new TRegexAST(false, false, false, false);
            } else if (token == UniversalAST.RB) {
                if (stack.isEmpty() || current.isContext) {
                    throw new Error("Unbalanced " + UniversalAST.RBS + " after " + TRegexAST.tregexScanner.scannedPrefix());
                }
                pushString(current, sb, isRegex);
                prev = current;
                current = stack.pop();
                current.addChild(prev);
            } else if (token == TRegexAST.LT) {
                pushString(current, sb, isRegex);
                stack.push(current);
                current = new TRegexAST(true, false, false, false);
            } else if (token == TRegexAST.RT) {
                if (stack.isEmpty() || !current.isContext) {
                    throw new Error("Unbalanced " + TRegexAST.RTS + " after " + TRegexAST.tregexScanner.scannedPrefix());
                }
                pushString(current, sb, isRegex);
                prev = current;
                current = stack.pop();
                current.addChild(prev);
            } else if (token == TRegexAST.LL) {
                pushString(current, sb, isRegex);
                stack.push(current);
                current = new TRegexAST(false, false, true, false);
            } else if (token == TRegexAST.RL) {
                if (stack.isEmpty() || !current.isStar) {
                    throw new Error("Unbalanced " + TRegexAST.RLS + " after " + TRegexAST.tregexScanner.scannedPrefix());
                }
                pushString(current, sb, isRegex);
                prev = current;
                current = stack.pop();
                current.addChild(prev);
            } else if (token == TRegexAST.LA) {
                pushString(current, sb, isRegex);
                stack.push(current);
                current = new TRegexAST(false, false, false, true);
            } else if (token == TRegexAST.RA) {
                if (stack.isEmpty() || !current.isAlternation) {
                    throw new Error("Unbalanced " + TRegexAST.RAS + " after " + TRegexAST.tregexScanner.scannedPrefix());
                }
                pushString(current, sb, isRegex);
                prev = current;
                current = stack.pop();
                current.addChild(prev);
            } else if (token == TRegexAST.AT) {
                pushString(current, sb, isRegex);
                current.addChild(TRegexAST.ATNode);
            } else {
                sb.append(TRegexAST.tregexScanner.lexeme);
            }
            token = TRegexAST.tregexScanner.nextToken();
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
            if (child instanceof TRegexAST && !((TRegexAST) child).isAt) {
                ret.append(((TRegexAST) child).isContext ? TRegexAST.LTS : UniversalAST.LBS);
                ret.append(child.toString());
                ret.append(((TRegexAST) child).isContext ? TRegexAST.RTS : UniversalAST.RBS);
            } else if (child instanceof TRegexAST && ((TRegexAST) child).isAt) {
                ret.append("@");
            } else {
                ret.append(tregexASTEscape(child + ""));
            }
        }
        return ret.toString();
    }

    public static String tregexASTEscape(String str) {
        tregexASTEscaper.setStream(str);
        int token = tregexASTEscaper.nextToken();
        StringBuilder sb = new StringBuilder();
        while (token !=  StringTokenScanner.EOF) {
            sb.append(tregexASTEscaper.lexeme);
            token = tregexASTEscaper.nextToken();
        }
        return sb.toString();
    }

}
