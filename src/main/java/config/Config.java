package config;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.Tree;

import java.util.List;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/22/16
 * Time: 9:39 AM
 */
public abstract class Config {
    abstract public String visitRuleBegin(List<String> ruleName, RuleContext rule);
    abstract public String visitRuleEnd(List<String> ruleName, RuleContext rule);

    public String getRuleName(List<String> ruleNames, Tree t) {
        int ruleIndex = ((RuleNode) t).getRuleContext().getRuleIndex();
        return ruleNames.get(ruleIndex);
    }
}
