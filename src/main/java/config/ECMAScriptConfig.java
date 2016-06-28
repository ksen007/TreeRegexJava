package config;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/28/16
 * Time: 6:43 AM
 */
public class ECMAScriptConfig extends Config {
    Set<String> skippedNames;


    public ECMAScriptConfig() {
        skippedNames = new HashSet<>();
        skippedNames.add("assignmentOperator");
    }

    public String visitRuleBegin(List<String> ruleNames, RuleContext rule) {
        int n = rule.getChildCount();
        boolean foundLeaf = false;
        for (int i=0; i<n && !foundLeaf; i++) {
            ParseTree tree = rule.getChild(i);
            if (tree instanceof TerminalNodeImpl) {
                foundLeaf = true;
            } else {
                String childRuleName = getRuleName(ruleNames, tree);
                if (skippedNames.contains(childRuleName)) foundLeaf = true;
            }
        }

        String ruleName = getRuleName(ruleNames, rule);
        if (foundLeaf) {
            if (ruleName.contains("Statement")) return "S";
            if (ruleName.contains("Expression")) return "E";
            if (ruleName.equals("literal")) return "L";
            if (skippedNames.contains(ruleName)) return null;
            return ruleName;
        } else {
            return null;
        }
    }

    @Override
    public String visitRuleEnd(List<String> ruleNames, RuleContext rule) {
        return null;
    }
}
