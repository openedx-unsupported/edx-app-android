package org.edx.mobile.test;

import org.edx.mobile.util.WeakList;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;

/**
 *
  */
public class WeakListTest {

    @Before
    public  void setUp() throws Exception {

    }

    @Test
    public void testWeakList() throws Exception {
        WeakList<Object> list = new WeakList<>();
        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();
        list.add(obj1);
        assertTrue("add op failed", list.size() == 1 );
        list.add(obj3);
        list.add(1, obj2);
        assertTrue("add at index op failed", list.get(1) == obj2 );

        assertTrue("size() op failed", list.size() == 3 );


    }



}
