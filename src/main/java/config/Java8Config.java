package config;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/22/16
 * Time: 10:57 AM
 */
public class Java8Config extends Config {
    Set<String> skippedNames;


    public Java8Config() {
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
            if (ruleName.contains("methodInvocation")) return "E";
            if (ruleName.contains("Type")) return "T";
            if (ruleName.equals("methodName")) return "V";
            if (ruleName.equals("typeName")) return "V";
            if (n == 1 && ruleName.equals("expressionName")) return "V";
            if (n > 1 && ruleName.equals("expressionName")) return "V";
            if (n == 1 && ruleName.equals("ambiguousName")) return "V";
            if (n > 1 && ruleName.equals("ambiguousName")) return "E";
            if (ruleName.equals("literal")) return "L";
            if (ruleName.equals("packageDeclaration")) return "PD";
            if (ruleName.equals("importDeclaration")) return "ID";
            if (ruleName.equals("typeDeclaration")) return "TD";
            if (ruleName.equals("methodDeclaration")) return "MD";
            if (ruleName.equals("fieldDeclaration")) return "FD";
            if (ruleName.equals("variableDeclaratorId")) return "V";
            if (ruleName.equals("methodDeclarator")) return "MN";
            if (ruleName.contains("Access")) return "E";
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
