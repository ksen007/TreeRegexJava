package treeregex;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/28/16
 * Time: 2:34 PM
 */
public class UniversalASTTest {

    @Test
    public void testParsingUniversalAST1() throws Exception {
        String s = "(% 3+(%abc%)%)";
        UniversalAST ast = UniversalAST.parseSTree(s);
        assertEquals(s, ast.toString());
    }

    @Test
    public void testParsingUniversalAST2() throws Exception {
        String s = "(% 3+/%/)/(/%(%a//bc%)%)";
        UniversalAST ast = UniversalAST.parseSTree(s);
        assertEquals(s, ast.toString());
    }

    @Test
    public void testParsingUniversalAST3() throws Exception {
        String s = "3+/(/%(%a//bc%) 78";
        UniversalAST ast = UniversalAST.parseSTree(s);
        assertEquals(s, ast.toString());
    }

    @Test(expected=java.lang.Error.class)
    public void testParsingUniversalAST4() throws Exception {
        String s = "(% 3+/(%a//bc%)%)cde";
        UniversalAST ast = UniversalAST.parseSTree(s);
    }

    @Test(expected=java.lang.Error.class)
    public void testParsingUniversalAST5() throws Exception {
        String s = "(% 3+/(%a//(%bc%)cde";
        UniversalAST ast = UniversalAST.parseSTree(s);
    }

}