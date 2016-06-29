package treeregex;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/28/16
 * Time: 7:44 AM
 */
public class StringTokenScanner {
    private int escapeChar;
    public static int EOF = -1;
    private TrieNode trie;
    private IntStack availableChars;
    private int tokenIndex, charIndex;
    private String stream;
    public String lexeme;
    public int token;

    public StringTokenScanner(int escapeChar) {
        this.escapeChar = escapeChar;
        this.trie = new TrieNode();
    }

    public StringTokenScanner() {
        this.escapeChar = -2;
        this.trie = new TrieNode();
    }

    public void setStream(String stream) {
        this.stream = stream;
        this.tokenIndex = 0;
        this.charIndex = 0;
        this.availableChars = new IntArrayList();
    };

    public void addString(int ID, String lexeme, String actual) {
        this.addStringAux(ID, lexeme, actual);
    }

     private int addStringAux(int ID, String lexeme, String actual) {
        int len = lexeme.length();
        TrieNode current = this.trie;

        for (int i = 0; i < len; i++) {
            char c = lexeme.charAt(i);
            current = current.getOrCreate(c);
        }
        current.ID = ID;
        if (actual != null)
            current.s = actual;
        else
            current.s = lexeme;
        current.isSet = true;
        return ID;
    }


    private int readChar() {
        int inp;
        if (this.availableChars.isEmpty()) {
            if (stream.length() <= this.charIndex) {
                inp = StringTokenScanner.EOF;
            } else {
                inp = this.stream.charAt(this.charIndex);
            }
            this.charIndex++;
        } else {
            inp = this.availableChars.pop();
        }
        return inp;
    }

    private void pushChar(int c) {
        this.availableChars.push(c);
    }

    public int nextToken () {
        int inp;
        this.tokenIndex++;
        inp = this.readChar();
        if (inp == StringTokenScanner.EOF) {
            this.lexeme = "";
            return this.token = inp;
        }
        if (inp == this.escapeChar) {
            inp = this.readChar();
            this.lexeme = ""+(char)inp;
            return this.token = inp;
        }
        int oldCharIndex = this.charIndex;
        int firstInp = inp;
        TrieNode prev = null;
        TrieNode ret = this.trie.get(inp);
        while (ret != null) {
            inp = this.readChar();
            prev = ret;
            ret = ret.get(inp);
        }
        if (prev != null && prev.isSet) {
            this.pushChar(inp);
            this.lexeme = prev.s;
            return this.token = prev.ID;
        } else {
            this.charIndex = oldCharIndex;
            this.lexeme = ""+(char)firstInp;
            return this.token = firstInp;
        }
    }

    public String scannedPrefix() {
        return this.stream.substring(0, this.charIndex - 1);
    }
}
