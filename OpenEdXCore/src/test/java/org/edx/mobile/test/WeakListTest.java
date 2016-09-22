package org.edx.mobile.test;

import org.edx.mobile.util.WeakList;
import org.junit.Before;
import org.junit.Test;

import java.lang.ref.WeakReference;

import static junit.framework.TestCase.assertTrue;

/**
 *
  */
public class WeakListTest {

    @Before
    public  void setUp() throws Exception {

    }

    @Test
    public void testAdd() throws Exception {
        WeakList<Object> list = new WeakList<>();
        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();
        list.add(obj1);
        assertTrue("add op failed", list.size() == 1);
        list.add(obj3);
        list.add(1, obj2);
        assertTrue("add at index op failed", list.get(1) == obj2 );
        assertTrue("size() op failed", list.size() == 3 );
    }

    @Test
    public void testRemoveReleased() throws Exception {
        WeakList<Object> list = new WeakList<>();
        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();
        WeakReference<Object> obj1Wrapper = list.add(obj1);
        WeakReference<Object> obj2Wrapper = list.add(obj2);
        WeakReference<Object> obj3Wrapper = list.add(obj3);
        assertTrue("size() op failed", list.size() == 3 );
        obj2Wrapper.clear();
        list.removeReleased();
        assertTrue("removeReleased() op failed", list.size() == 2 );
        obj1Wrapper.clear();
        obj3Wrapper.clear();
        list.removeReleased();
        assertTrue("removeReleased() op failed", list.size() == 0 );
    }

    @Test
    public void testGetFirstValid() throws Exception {
        WeakList<Object> list = new WeakList<>();
        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();
        WeakReference<Object> obj1Wrapper = list.add(obj1);
        WeakReference<Object> obj2Wrapper = list.add(obj2);
        WeakReference<Object> obj3Wrapper = list.add(obj3);
        obj1Wrapper.clear();

        assertTrue("GetFirstValid() op failed", list.getFirstValid() == obj2 );

        obj3Wrapper.clear();
        assertTrue("GetFirstValid() op failed", list.getFirstValid() == obj2 );
    }

    @Test
    public void testGetLastValid() throws Exception {
        WeakList<Object> list = new WeakList<>();
        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();
        WeakReference<Object> obj1Wrapper = list.add(obj1);
        WeakReference<Object> obj2Wrapper = list.add(obj2);
        WeakReference<Object> obj3Wrapper = list.add(obj3);
        obj1Wrapper.clear();

        assertTrue("getLastValid() op failed", list.getLastValid() == obj3 );

        obj3Wrapper.clear();
        assertTrue("getLastValid() op failed", list.getLastValid() == obj2 );
    }

    @Test
    public void testHas() throws Exception {
        WeakList<Object> list = new WeakList<>();
        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();
        WeakReference<Object> obj1Wrapper = list.add(obj1);
        WeakReference<Object> obj2Wrapper = list.add(obj2);

        assertTrue("has() op failed", list.has(obj1) );
        assertTrue("has() op failed", !list.has(obj3) );
    }

    @Test
    public void testRemove() throws Exception {
        WeakList<Object> list = new WeakList<>();
        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();
        WeakReference<Object> obj1Wrapper = list.add(obj1);
        WeakReference<Object> obj2Wrapper = list.add(obj2);

        list.remove(obj1);
        assertTrue("remove() op failed", list.size() == 1 );
        list.remove(obj3);
        assertTrue("remove() op failed", list.size() == 1);
    }
}
