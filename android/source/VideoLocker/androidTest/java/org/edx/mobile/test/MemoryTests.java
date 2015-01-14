package org.edx.mobile.test;

import org.edx.mobile.util.MemoryUtil;

public class MemoryTests extends BaseTestCase {

    public void testAvailable() throws Exception {
        long bytes = MemoryUtil.getAvailableExternalMemory(getInstrumentation()
                .getTargetContext());
        print(
                "available = "
                        + MemoryUtil.format(getInstrumentation()
                                .getTargetContext(), bytes));
        
        bytes = MemoryUtil.getAvailableInternalMemory(getInstrumentation()
                .getTargetContext());
        print(
                "available = "
                        + MemoryUtil.format(getInstrumentation()
                                .getTargetContext(), bytes));
    }
}
