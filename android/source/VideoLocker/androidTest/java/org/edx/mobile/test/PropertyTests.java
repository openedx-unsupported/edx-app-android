package org.edx.mobile.test;

import org.edx.mobile.util.PropertyUtil;

public class PropertyTests extends BaseTestCase {

    public void testGetDisplayVersionName() throws Exception {
        String name = PropertyUtil.getDisplayVersionName(getInstrumentation().getTargetContext());
        assertTrue("failed to read property file", name != null);
        print("display name = " + name);
    }
}
