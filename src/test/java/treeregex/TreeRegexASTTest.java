package treeregex;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/28/16
 * Time: 9:43 PM
 */
public class TreeRegexASTTest {

    @Test
    public void testTreeRegexParse1() throws Exception {
        String s = "(% 3+(%abc%)%)";
        TreeRegexAST ast = TreeRegexAST.parseTreeRegex(s, false);
        assertEquals(s, ast.toString());
    }

    @Test
    public void testTreeRegexParse2() throws Exception {
        String s = "(% 3+/(/%(%a//bc%)%)";
        TreeRegexAST ast = TreeRegexAST.parseTreeRegex(s, false);
        assertEquals(s, ast.toString());
    }

    @Test(expected=java.lang.Error.class)
    public void testTreeRegexParse3() throws Exception {
        String s = "(% 3+/(%a//bc%)%)cde";
        TreeRegexAST ast = TreeRegexAST.parseTreeRegex(s, false);
        assertEquals(s, ast.toString());
    }

    @Test(expected=java.lang.Error.class)
    public void testTreeRegexParse4() throws Exception {
        String s = "(% 3+/(%a//(%bc%)cde";
        TreeRegexAST ast = TreeRegexAST.parseTreeRegex(s, false);
        assertEquals(s, ast.toString());
    }

    @Test
    public void testTreeRegexParse5() throws Exception {
        String s = "(%(*3@*)+(%abc%)%)";
        TreeRegexAST ast = TreeRegexAST.parseTreeRegex(s, false);
        assertEquals(s, ast.toString());
    }

    @Test
    public void testTreeRegexParse6() throws Exception {
        String s = "(%(*/@3@*)+(%a/(/*)/*/)bc%)%)";
        TreeRegexAST ast = TreeRegexAST.parseTreeRegex(s, false);
        assertEquals(s, ast.toString());
    }

    @Test(expected=java.lang.Error.class)
    public void testTreeRegexParse7() throws Exception {
        String s = "(% 3+/(*a//bc*)%)cde";
        TreeRegexAST ast = TreeRegexAST.parseTreeRegex(s, false);
        assertEquals(s, ast.toString());
    }

    @Test(expected=java.lang.Error.class)
    public void testTreeRegexParse8() throws Exception {
        String s = "(% 3+/(%a//(*bc%)cde";
        TreeRegexAST ast = TreeRegexAST.parseTreeRegex(s, false);
        assertEquals(s, ast.toString());
    }


}