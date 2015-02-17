package org.edx.mobile.test;

import org.edx.mobile.util.PropertyUtil;

public class PropertyTests extends BaseTestCase {

    public void testGetDisplayVersionName() throws Exception {
        String name = PropertyUtil.getManifestVersionName(getInstrumentation().getTargetContext());
        assertTrue("failed to read versionName, found=" + name,
                name != null && !name.isEmpty());
    }
}
