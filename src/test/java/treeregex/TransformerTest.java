package treeregex;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Author: Koushik Sen (ksen@cs.berkeley.edu)
 * Date: 6/29/16
 * Time: 1:34 PM
 */
public class TransformerTest {
    @Test
    public void testTransformation1() throws Exception {
        Transformer t = new Transformer();
        t.addTransformer(null, "([a-zA-Z_]+)\\s+@([^\\d])@ ", null, "$1 $4$3$2 ", true, false);
        SerializedTree out = t.modify("(%Add (%Multiply (%Number 1 %)*(%Divide (%Number 2 %)//(%Number 3 %) %) %)+(%Divide (%Number 4 %)//(%Multiply (%Number 5 %)*(%Number 6 %) %) %) %)", null, null);
        assertEquals("(%Add (%Divide (%Multiply (%Number 6 %)*(%Number 5 %) %)//(%Number 4 %) %)+(%Multiply (%Divide (%Number 3 %)//(%Number 2 %) %)*(%Number 1 %) %) %)", out+"");
    }
}