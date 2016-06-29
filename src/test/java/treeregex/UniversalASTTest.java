package treeregex;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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


    @Test
    public void testMatching1() throws Exception {
        UniversalAST pstring = UniversalAST.parseSTree("982(%Hello123%)");
        TreeRegexAST pattern = TreeRegexAST.parseTreeRegex("(\\d+)(%([a-zA-Z_]+)(\\d+)%)", true);
        ObjectArrayList matches = pstring.matches(pattern);
        assertEquals("982", matches.get(1));
        assertEquals("Hello", matches.get(2));
        assertEquals("123", matches.get(3));
    }

    @Test
    public void testMatching2() throws Exception {
        UniversalAST pstring = UniversalAST.parseSTree("982(%Hello(%World%)123%)");
        TreeRegexAST pattern = TreeRegexAST.parseTreeRegex("(\\d+)(%([a-zA-Z_]+)@(\\d+)%)", true);
        ObjectArrayList matches = pstring.matches(pattern);
        assertEquals("982", matches.get(1));
        assertEquals("Hello", matches.get(2));
        assertEquals("World", matches.get(3)+"");
        assertEquals("123", matches.get(4));
    }

    @Test
    public void testMatching3() throws Exception {
        UniversalAST pstring = UniversalAST.parseSTree("982(%Hello(%World(%novar%) 123 (%var%)%)123%)");
        TreeRegexAST pattern = TreeRegexAST.parseTreeRegex("(\\d+)(*(var)*)", true);
        ObjectArrayList matches = pstring.matches(pattern);
        assertEquals("982", matches.get(1));
        assertEquals("var", matches.get(3));
    }

    @Test
    public void testMatching4() throws Exception {
        UniversalAST pstring = UniversalAST.parseSTree("982(%Hello(%World(%novar%)%)(%World(%var%)%)end%)");
        TreeRegexAST pattern = TreeRegexAST.parseTreeRegex("(\\d+)(*(World)(%(var)%)*)", true);
        ObjectArrayList matches = pstring.matches(pattern);
        assertEquals("982", matches.get(1));
        assertEquals("World", matches.get(3));
        assertEquals("var", matches.get(4));
    }

    @Test
    public void testMatching5() throws Exception {
        UniversalAST pstring = UniversalAST.parseSTree("982(%ihb(%Hello(%World(%novar%)%)(%World(%novar%)%)end%)123(%Hello(%World(%novar%)%)(%World(%var%)%)end%)end%)");
        TreeRegexAST pattern = TreeRegexAST.parseTreeRegex("(\\d+)(*(World)(%(var)%)*)", true);
        ObjectArrayList matches = pstring.matches(pattern);
        assertEquals("982", matches.get(1));
        assertEquals("World", matches.get(3));
        assertEquals("var", matches.get(4));
    }

    @Test
    public void testMatching6() throws Exception {
        UniversalAST pstring = UniversalAST.parseSTree("x(%a(%b%)c%)y");
        TreeRegexAST pattern = TreeRegexAST.parseTreeRegex("x(*b*)y", true);
        ObjectArrayList matches = pstring.matches(pattern);
    }
}