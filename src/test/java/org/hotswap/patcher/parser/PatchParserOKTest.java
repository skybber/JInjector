package org.hotswap.patcher.parser;

import org.hotswap.patcher.patch.Patch;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

public class PatchParserOKTest {

    private static Patch createPatch(String name) {
        try (InputStream is = PatchParserOKTest.class.getResourceAsStream(name)) {
            PatchParser patchParser = new PatchParser();
            return patchParser.parseStream(is);
        } catch (IOException ignore) {
        }
        return null;
    }

    @Test
    public void transform1Test() {
        Patch patch = createPatch("transform1.hswp");
        assertNotNull(patch);
    }

    @Test
    public void create1Test() {
        Patch patch = createPatch("create1.hswp");
        assertNotNull(patch);
    }

    @Test
    public void complex1Test() {
        Patch patch = createPatch("complex1.hswp");
        assertNotNull(patch);
    }

}