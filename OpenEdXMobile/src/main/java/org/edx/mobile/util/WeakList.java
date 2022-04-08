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

    public void clear(){
        items.clear();
    }

    public WeakReference<T> add(int index, T element) {
        synchronized (items) {
            WeakReference<T> item = new WeakReference(element);
            items.add(index, item);
            return item;
        }
    }

    public WeakReference<T> add(T element) {
        synchronized (items) {
            WeakReference<T> wrapped = new WeakReference(element);
            items.add(wrapped);
            return wrapped;
        }
    }

    public int size() {
        removeReleased();
        return items.size();
    }


    public T getFirstValid(){
        synchronized (items) {
            int size = size();
            if(  size > 0 ){
                WeakReference<T> ref = (WeakReference)items.get(0);
                return ref.get();
            }
            return null;
        }
    }

    public T getLastValid(){
        synchronized (items) {
            int size = size();
            for (int i = size -1; i >= 0; i--) {
                WeakReference<T> ref = (WeakReference)items.get(i);
                if (ref.get() == null) {
                    items.remove(ref);
                    ref.clear();
                }else
                    return ref.get();
            }
            return null;
        }
    }

    public T get(int index) {
        return  items.get(index).get();
    }

    public boolean has(T object){
        if ( object == null )
            return false;
        synchronized (items) {
            for (Iterator it = items.iterator(); it.hasNext(); ) {
                WeakReference ref = (WeakReference) it.next();
                if (ref.get() == object) {
                    return true;
                }
            }
            return false;
        }
    }


    public boolean remove(T object){
        synchronized (items) {
            for (Iterator it = items.iterator(); it.hasNext(); ) {
                WeakReference ref = (WeakReference) it.next();
                if (ref.get() == object) {
                    items.remove(ref);
                    ref.clear();
                    return true;
                }
            }
            return false;
        }
    }

    public void removeReleased() {
        synchronized (items) {
            System.gc(); //just a hint
            int size = items.size();
            for (int i = size -1; i >= 0; i--) {
                WeakReference<T> ref = (WeakReference)items.get(i);
                if (ref.get() == null) {
                    items.remove(ref);
                    ref.clear();
                }
            }
        }
    }

}
