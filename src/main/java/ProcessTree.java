import config.Config;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/18/16
 * Time: 10:41 AM
 */
public class ProcessTree {
    List<String> ruleNames = null;

    private void setRuleNames(Parser recog) {
        String[] ruleNames = recog != null ? recog.getRuleNames() : null;
        this.ruleNames = ruleNames != null ? Arrays.asList(ruleNames) : null;
    }

    public void serializeFile(String f, String grammarName, String startSymbol) {
        try {
            Class classDefinition;
            Class[] type;
            Object[] obj;

            type = new Class[]{ CharStream.class };
            classDefinition = Class.forName(grammarName+"Lexer");
            Constructor cons = classDefinition .getConstructor(type);
            obj = new Object[]{ new ANTLRFileStream(f) };
            Lexer lexer = (Lexer)cons.newInstance(obj);
            CommonTokenStream tokens = new CommonTokenStream(lexer);

            type = new Class[]{ TokenStream.class };
            classDefinition = Class.forName(grammarName+"Parser");
            cons = classDefinition .getConstructor(type);
            obj = new Object[]{ tokens };
            Parser parser = (Parser)cons.newInstance(obj);

            Method method = parser.getClass().getMethod(startSymbol);
            ParserRuleContext t = (ParserRuleContext)method.invoke(parser);
            parser.setBuildParseTree(false);
            setRuleNames(parser);

            classDefinition = Class.forName("config."+grammarName+"Config");
            cons = classDefinition .getConstructor();
            Config config = (Config)cons.newInstance();

            StringBuilder sb = new StringBuilder();
            getSerializedTree(t, tokens, config, sb);
            appendTokensBeforeOrAfter(t, tokens, false, sb);
            String st = sb.toString();
            System.out.println(st);
            try(  PrintWriter out = new PrintWriter(f+".st")  ){
                out.print(st);
            }
            //System.out.println(t.toStringTree(parser));
        } catch (Exception e) {
            System.err.println("parser exception: " + e);
            e.printStackTrace();   // so we can get stack trace
        }
    }

    private int lastIndexOfToken=-2;
    private static int HIDDEN = 1;

    private void appendTokensBeforeOrAfter(ParseTree tree, CommonTokenStream tokens, boolean isBefore, StringBuilder builder) {
        int old = lastIndexOfToken;
        if (isBefore) {
            old = lastIndexOfToken;
            lastIndexOfToken = ((TerminalNodeImpl) tree).getSymbol().getTokenIndex();
        }
        List<Token> ws = null;
        if (isBefore) {
            if (old != -1) {
                ws = tokens.getHiddenTokensToLeft(lastIndexOfToken, HIDDEN);
            }
        } else if (lastIndexOfToken>=0 || lastIndexOfToken == -2) {
            ws = tokens.getHiddenTokensToRight(lastIndexOfToken<0?0:lastIndexOfToken, HIDDEN);
            if (lastIndexOfToken == -2) {
                ws.add(0, tokens.get(0));
            }
            lastIndexOfToken = -1;
        }

        if (ws != null) {
            for (Token wst : ws) {
                builder.append(wst.getText());
            }
        }
    }

    private void getSerializedTree(RuleContext t, CommonTokenStream tokens, Config config, StringBuilder builder) {
        int n = t.getChildCount();
        if (n == 0) {
            return;
        }

        String thisRuleNameShort = config.visitRuleBegin(ruleNames, t);
        if (thisRuleNameShort != null) {
            appendTokensBeforeOrAfter(t, tokens, false, builder);
            builder.append("(%"+thisRuleNameShort+" ");
        }
        for (int i = 0; i < n; i++) {
            ParseTree tree = t.getChild(i);
            if (tree instanceof TerminalNodeImpl) {
                appendTokensBeforeOrAfter(tree, tokens, true, builder);
                String s = tree.getText();
                builder.append(s);
            } else {
                getSerializedTree((RuleContext) tree, tokens, config, builder);
            }
        }
        if (thisRuleNameShort != null) builder.append("%)");
        config.visitRuleEnd(ruleNames, t);
    }

    public static void main(String args[]) {
        ProcessTree p = new ProcessTree();
        p.serializeFile(args[2], args[0], args[1]);
    }

}
