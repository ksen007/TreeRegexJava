package treeregex;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/28/16
 * Time: 2:34 PM
 */
public class SerializedTreeTest {

    @Test
    public void testParsingUniversalAST1() throws Exception {
        String s = "(% 3+(%abc%)%)";
        SerializedTree ast = SerializedTree.parse(s);
        assertEquals(s, ast.toString());
    }

    @Test
    public void testParsingUniversalAST2() throws Exception {
        String s = "(% 3+/%/)/(/%(%a//bc%)%)";
        SerializedTree ast = SerializedTree.parse(s);
        assertEquals(s, ast.toString());
    }

    @Test
    public void testParsingUniversalAST3() throws Exception {
        String s = "3+/(/%(%a//bc%) 78";
        SerializedTree ast = SerializedTree.parse(s);
        assertEquals(s, ast.toString());
    }

    @Test(expected=java.lang.Error.class)
    public void testParsingUniversalAST4() throws Exception {
        String s = "(% 3+/(%a//bc%)%)cde";
        SerializedTree ast = SerializedTree.parse(s);
    }

    @Test(expected=java.lang.Error.class)
    public void testParsingUniversalAST5() throws Exception {
        String s = "(% 3+/(%a//(%bc%)cde";
        SerializedTree ast = SerializedTree.parse(s);
    }


    @Test
    public void testMatching1() throws Exception {
        String pstring = "982(%Hello123%)";
        TreeRegexLib pattern = new TreeRegexLib("(\\d+)(%([a-zA-Z_]+)(\\d+)%)", true);
        Object[] matches = pattern.matches(pstring);
        assertEquals("982", matches[1]);
        assertEquals("Hello", matches[2]);
        assertEquals("123", matches[3]);
    }

    @Test
    public void testMatching2() throws Exception {
        String pstring = "982(%Hello(%World%)123%)";
        TreeRegexLib pattern = new TreeRegexLib("(\\d+)(%([a-zA-Z_]+)@(\\d+)%)", true);
        Object[] matches = pattern.matches(pstring);
        assertEquals("982", matches[1]);
        assertEquals("Hello", matches[2]);
        assertEquals("World", matches[3]+"");
        assertEquals("123", matches[4]);
    }

    @Test
    public void testMatching3() throws Exception {
        String pstring = "982(%Hello(%World(%novar%) 123 (%var%)%)123%)";
        TreeRegexLib pattern = new TreeRegexLib("(\\d+)(*(var)*)", true);
        Object[] matches = pattern.matches(pstring);
        assertEquals("982", matches[1]);
        assertEquals("var", matches[3]);
    }

    @Test
    public void testMatching4() throws Exception {
        String pstring = "982(%Hello(%World(%novar%)%)(%World(%var%)%)end%)";
        TreeRegexLib pattern = new TreeRegexLib("(\\d+)(*(World)(%(var)%)*)", true);
        Object[] matches = pattern.matches(pstring);
        assertEquals("982", matches[1]);
        assertEquals("World", matches[3]);
        assertEquals("var", matches[4]);
    }

    @Test
    public void testMatching5() throws Exception {
        String pstring = "982(%ihb(%Hello(%World(%novar%)%)(%World(%novar%)%)end%)123(%Hello(%World(%novar%)%)(%World(%var%)%)end%)end%)";
        TreeRegexLib pattern = new TreeRegexLib("(\\d+)(*(World)(%(var)%)*)", true);
        Object[] matches = pattern.matches(pstring);
        assertEquals("982", matches[1]);
        assertEquals("World", matches[3]);
        assertEquals("var", matches[4]);
    }

    @Test
    public void testMatching6() throws Exception {
        String pstring = "x(%a(%b%)c%)y";
        TreeRegexLib pattern = new TreeRegexLib("x(*b*)y", true);
        Object[] matches = pattern.matches(pstring);
        System.out.println(matches[1]+"");
        assertEquals("a(%b%)c - b", matches[1]+"");
    }

    @Test
    public void testReplace1() throws Exception {
        String pstring = "982(%Hello123%)";
        TreeRegexLib treeregex = new TreeRegexLib("(\\d+)(%([a-zA-Z_]+)(\\d+)%)", true, "$3(%$2$1%)");
        SerializedTree mod = treeregex.replace(pstring);
        assertEquals("123(%Hello982%)", mod + "");
    }

    @Test
    public void testReplace2() throws Exception {
        String pstring = "982(%Hello(%World%)123%)";
        TreeRegexLib treeregex = new TreeRegexLib("(\\d+)(%([a-zA-Z_]+)@(\\d+)%)", true, "$3(%$2$1%)");
        SerializedTree mod = treeregex.replace(pstring);
        assertEquals("(%World%)(%Hello982%)", mod + "");
    }

    @Test
    public void testReplace3() throws Exception {
        String pstring = "982(%ihb(%Hello(%World(%novar%)%)(%World(%novar%)%)end%)123(%Hello(%World(%var%)%)(%World(%novar%)%)end%)end%)";
        TreeRegexLib treeregex = new TreeRegexLib("(\\d+)(*(World)(%(var)%)*)", true, "$1$2(%Universe(%$4%)%)");
        SerializedTree mod = treeregex.replace(pstring);
        assertEquals("982(%ihb(%Hello(%World(%novar%)%)(%World(%novar%)%)end%)123(%Hello(%Universe(%var%)%)(%World(%novar%)%)end%)end%)", mod + "");
    }
}