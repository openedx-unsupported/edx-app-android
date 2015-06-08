package org.edx.mobile.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simple list wich holds only weak references to the original objects.
 */
public class WeakList<T>{

    private List<WeakReference<T>> items;

    public WeakList() {
        items = new ArrayList();
    }

    public void add(int index, T element) {
        items.add(index, new WeakReference(element));
    }

    public void add(T element) {
        items.add(new WeakReference(element));
    }

    public int size() {
        removeReleased();
        return items.size();
    }

    public void removeFirst(){
        if ( items.size() > 0)
            items.remove(0);
    }

    public T getFirstValid(){
        for (Iterator it = items.iterator(); it.hasNext(); ) {
            WeakReference<T> ref = (WeakReference) it.next();
            if (ref.get() == null)
                items.remove(ref);
            else
                return ref.get();
        }
        return null;
    }

    public T get(int index) {
        return  items.get(index).get();
    }

    public boolean remove(T object){
        for (Iterator it = items.iterator(); it.hasNext(); ) {
            WeakReference ref = (WeakReference) it.next();
            if (ref.get() == object ){
                items.remove(ref);
                return true;
            }
        }
        return false;
    }

    public void removeReleased() {
        for (Iterator it = items.iterator(); it.hasNext(); ) {
            WeakReference ref = (WeakReference) it.next();
            if (ref.get() == null) items.remove(ref);
        }
    }

}