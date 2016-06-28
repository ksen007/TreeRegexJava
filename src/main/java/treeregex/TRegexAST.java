package treeregex;

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
}
