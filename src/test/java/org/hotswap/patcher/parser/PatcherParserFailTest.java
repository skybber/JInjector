package org.hotswap.patcher.parser;

import java.io.IOException;
import java.io.InputStream;

import org.hotswap.patcher.patch.Patch;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class PatcherParserFailTest
{
	private static Patch createPatch(String name) {
		try (InputStream is = PatcherParserFailTest.class.getResourceAsStream(name)) {
			PatchParser patchParser = new PatchParser();
			return patchParser.parseStream(is);
		} catch (IOException ignore) {
		}
		return null;
	}

	@Test
	public void failSectionTest() {
		Patch patch = createPatch("failSection.hswp");
		assertNull(patch);
	}

	@Test
	public void failTranfClassTest() {
		Patch patch = createPatch("failTransfClass.hswp");
		assertNull(patch);
	}

	@Test
	public void failUnknownElemTest() {
		Patch patch = createPatch("failUnknownElem.hswp");
		assertNull(patch);
	}

	@Test
	public void failUnknownFieldOpTest() {
		Patch patch = createPatch("failUnknownFieldOp.hswp");
		assertNull(patch);
	}

	@Test
	public void failFieldFieldNewBodyTest() {
		Patch patch = createPatch("failFieldNewBody.hswp");
		assertNull(patch);
	}

	@Test
	public void failFieldMethodSelectorTest() {
		Patch patch = createPatch("failMethodSelector.hswp");
		assertNull(patch);
	}

	@Test
	public void failFieldMethodSelectorTypeTest() {
		Patch patch = createPatch("failMethodSelectorType.hswp");
		assertNull(patch);
	}

	@Test
	public void failMethodBodyTest() {
		Patch patch = createPatch("failMethodBody.hswp");
		assertNull(patch);
	}

	@Test
	public void failMethodNewBodyTest() {
		Patch patch = createPatch("failMethodNewBody.hswp");
		assertNull(patch);
	}

	@Test
	public void failCreateClass() {
		Patch patch = createPatch("failCreateClass.hswp");
		assertNull(patch);
	}

	@Test
	public void failCreateBody() {
		Patch patch = createPatch("failCreateBody.hswp");
		assertNull(patch);
	}

}
