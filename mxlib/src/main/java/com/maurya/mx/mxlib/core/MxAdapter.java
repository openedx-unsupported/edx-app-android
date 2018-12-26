package com.maurya.mx.mxlib.core;

import android.content.Context;

import java.util.List;

/**
 * Created by mukesh on 8/9/18.
 */

public interface MxAdapter<T> {

    public Context getContext();
    /**
     * Set click listener
     *
     * @param listener click listener
     */
    public void setItemClickListener(OnRecyclerItemClickListener<T> listener);

    /**
     * Returns all items from the data set held by the adapter.
     *
     * @return All of items in this adapter.
     */
    public List<T> getItems();

    /**
     * Sets items to the adapter and notifies that data set has been changed.
     *
     * @param items items to set to the adapter
     * @throws IllegalArgumentException in case of setting `null` items
     */
    public void setItems(List<T> items);

    /**
     * Returns an items from the data set at a certain position.
     *
     * @return All of items in this adapter.
     */
    public T getItem(int position);

    /**
     * Adds item to the end of the data set.
     * Notifies that item has been inserted.
     *
     * @param item item which has to be added to the adapter.
     */
    public void add(T item);


    /**
     * Adds list of items to the end of the adapter's data set.
     * Notifies that item has been inserted.
     *
     * @param items items which has to be added to the adapter.
     */
    public void addAll(List<T> items);

    public void removeLastItem();

    /**
     * Clears all the items in the adapter.
     */
    public void clear();

    /**
     * Removes an item from the adapter.
     * Notifies that item has been removed.
     *
     * @param item to be removed
     */
    public void remove(T item);

    /**
     * Returns whether adapter is empty or not.
     *
     * @return `true` if adapter is empty or `false` otherwise
     */
    public boolean isEmpty();

}
