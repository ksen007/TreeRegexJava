package examples;

import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import org.json.JSONObject;
import treeregex.Modifier;
import treeregex.Predicate;
import treeregex.Transformer;
import treeregex.TransformerModifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Created by ksen on 7/1/16.
 */
public class JSLint {


    public static void main(String[] args) throws FileNotFoundException {
        String content = new Scanner(new File(args[0])).useDelimiter("\\Z").next();
        Transformer t = new Transformer();
        t.addTransformer(null, "L\\s+\\{\\s*@\\s*\\}\\s*", new TransformerModifier(1, new Predicate() {
            @Override
            public boolean apply(Object2ObjectRBTreeMap<String, Object> state, Object2ObjectRBTreeMap<String, Object> args) {
                return !args.containsKey("first");
            }
        }, "(%propertyAssignment\\s+(%\\w+\\s+(\\S+)\\s*%)\\s*:\\s*@\\s*%)", new Modifier() {
            @Override
            public Object[] apply(Object[] matches, Object2ObjectRBTreeMap<String, Object> state, Object2ObjectRBTreeMap<String, Object> args, Object2ObjectRBTreeMap<String, Object> ret) {
                if (matches != null) {
                    JSONObject json = new JSONObject("{\"x\" : " + matches[1] + "}");
                    String s = json.get("x") + "";
                    if (state.containsKey(s)) {
                        System.out.println("Found duplicate key " + s);
                    } else {
                        state.put(s, s);
                    }
                }
                ret.put("first", true);
                return matches;
            }
        }, null, true, true, true, null, null), null, true, true);
        t.modify(content, null, null);
        //t.addTransformer(true, toRegexString("L(00) \\{ (**(%(L|V)00 (....)%) : @ , **) (**(%(L|V)00 (....)%) : @**) \\} "), checkDuplicates, null, true, true);

    }
}
