package treeregex;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/28/16
 * Time: 9:43 PM
 */
public class TRegexASTTest {

    @Test
    public void testTRegexParse1() throws Exception {
        String s = "(% 3+(%abc%)%)";
        TRegexAST ast = TRegexAST.parseTRegex(s, false);
        assertEquals(s, ast.toString());
    }

    @Test
    public void testTRegexParse2() throws Exception {
        String s = "(% 3+/(/%(%a//bc%)%)";
        TRegexAST ast = TRegexAST.parseTRegex(s, false);
        assertEquals(s, ast.toString());
    }

    @Test(expected=java.lang.Error.class)
    public void testTRegexParse3() throws Exception {
        String s = "(% 3+/(%a//bc%)%)cde";
        TRegexAST ast = TRegexAST.parseTRegex(s, false);
        assertEquals(s, ast.toString());
    }

    @Test(expected=java.lang.Error.class)
    public void testTRegexParse4() throws Exception {
        String s = "(% 3+/(%a//(%bc%)cde";
        TRegexAST ast = TRegexAST.parseTRegex(s, false);
        assertEquals(s, ast.toString());
    }

    @Test
    public void testTRegexParse5() throws Exception {
        String s = "(%(*3@*)+(%abc%)%)";
        TRegexAST ast = TRegexAST.parseTRegex(s, false);
        assertEquals(s, ast.toString());
    }

    @Test
    public void testTRegexParse6() throws Exception {
        String s = "(%(*/@3@*)+(%a/(/*)/*/)bc%)%)";
        TRegexAST ast = TRegexAST.parseTRegex(s, false);
        assertEquals(s, ast.toString());
    }

    @Test(expected=java.lang.Error.class)
    public void testTRegexParse7() throws Exception {
        String s = "(% 3+/(*a//bc*)%)cde";
        TRegexAST ast = TRegexAST.parseTRegex(s, false);
        assertEquals(s, ast.toString());
    }

    @Test(expected=java.lang.Error.class)
    public void testTRegexParse8() throws Exception {
        String s = "(% 3+/(%a//(*bc%)cde";
        TRegexAST ast = TRegexAST.parseTRegex(s, false);
        assertEquals(s, ast.toString());
    }


}