package examples;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
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
    public static String toRegexString(String str, String old) {
        String ret = str.replaceAll("  ", "\\\\s+").replaceAll(" ", "\\\\s*").replaceAll("\\.\\.\\.\\.", "[\\\\s\\\\S]+").
                replaceAll("\\.\\.\\.", "[\\\\S\\\\s]*").replaceAll("00", "\\\\d+").replaceAll("0", "\\\\d*").replaceAll("WW", "\\\\w+").replace("W", "\\\\w*");
        if (old != null && old.equals(ret)) {
            System.out.println(str + " not expanded to " + old + " and expanded to " + ret);
            throw new RuntimeException(str + " not expanded to " + old + " and expanded to " + ret);
        }
//        System.out.println(ret);
        return ret;
    }



    public static void main(String[] args) throws FileNotFoundException {
        String content = new Scanner(new File(args[0])).useDelimiter("\\Z").next();
        Modifier checkDup = new Modifier() {
            @Override
            public Object[] apply(Object[] matches, Object2ObjectMap<String, Object> state, Object2ObjectMap<String, Object> args, Object2ObjectMap<String, Object> childArgs) {
                if (matches != null) {
                    JSONObject json = new JSONObject("{\"x\" : " + matches[1] + "}");
                    String s = json.get("x") + "";
                    if (state.containsKey(s)) {
                        System.out.println("Found duplicate key " + s);
                    } else {
                        state.put(s, s);
                    }
                }
                childArgs.put("first", true);
                return matches;
            }
        };
        Predicate isFirst = new Predicate() {
            @Override
            public boolean apply(Object2ObjectMap<String, Object> state, Object2ObjectMap<String, Object> args) {
                return !args.containsKey("first");
            }
        };

        Transformer t = new Transformer();
        t.addTransformer(null, toRegexString("L  \\{ @ \\} ", null),
                new TransformerModifier(1, isFirst, toRegexString("(%propertyAssignment  (%WW  (....) %) : @ %)", null), checkDup , null, true, true, true, null, null),
                null, true, true);
        t.modify(content, null, null);

    }
}
