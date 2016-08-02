package org.edx.mobile.test;

import org.edx.mobile.util.Sha1Util;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class Sha1UtilTest extends BaseTestCase {
    @Test
    public void testSha1Hashing() {
        assertEquals("94ca247fff5ad413788a1c8d8c80394a246dba1c", Sha1Util.SHA1("khalid"));
        assertEquals("d52f2b07afef758721dd630fcbc15f83fa2e42aa", Sha1Util.SHA1("some_vague_string"));
    }

    @Test
    public void testConvertToHex() {
        assertEquals("6b68616c6964", Sha1Util.convertToHex("khalid".getBytes()));
        assertEquals("736f6d655f76616775655f737472696e67", Sha1Util.convertToHex("some_vague_string".getBytes()));
    }
}
