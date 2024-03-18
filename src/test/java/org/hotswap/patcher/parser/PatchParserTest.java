package org.hotswap.patcher.parser;

import org.junit.Test;

public class PatchParserTest {


    @Test
    public void parseTest() {
        PatchParser patchParser = new PatchParser();
        patchParser.parse(
    "class org.hotswap.patcher.parser.PatchParserTest {\n" +
            "    parse1(*) {\n" +
            "        insertBefore {\n" +
            "            System.out.println(\"hello\");\n" +
            "        }\n" +
            "        insertAfter {\n" +
            "            System.out.println(\"hello1\");/*\n" +
            "            This is comment*/" +
            "            System.out.println(\"hello1\");\n" +
            "        }\n" +
            "    }\n" +
            "    parse2() {\n" +
            "        insertBefore {\n" +
            "        }\n" +
            "        insertAfter {\n" +
            "        }\n" +
            "    }\n" +
            "    parse3(String, int, String[]) {\n" +
            "        insertBefore {\n" +
            "        }\n" +
            "        insertAfter {\n" +
            "        }\n" +
            "    }\n" +
            "}\n"
        );
    }

}