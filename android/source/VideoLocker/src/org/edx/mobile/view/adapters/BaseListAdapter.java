package org.edx.mobile.view.adapters;

import android.content.Context;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import org.edx.mobile.logger.Logger;

public abstract class BaseListAdapter<T> extends BaseAdapter implements OnItemClickListener {

    // constants that define selection state of list rows
    public static final int STATE_NOT_SELECTED = 0;
    public static final int STATE_SELECTED = 1;
    protected Context context;
    private ArrayList<T> items = new ArrayList<T>();
    private SparseIntArray selection = new SparseIntArray(); 
    public static final long MIN_CLICK_INTERVAL = 1000; //in millis
    protected final Logger logger = new Logger(getClass().getName());
    
    public BaseListAdapter(Context context) {
        this.context = context;
    }
    
    /**
     * Selects the list row at specified position.
     * @param position
     */
    public void select(int position) {
        selection.put(position, STATE_SELECTED);
    }
    
    /**
     * De-selects the list row at specified position.
     * @param position
     */
    public void unselect(int position) {
        selection.put(position, STATE_NOT_SELECTED);
    }
    
    /**
     * Selects all the items in this adapter.
     */
    public void selectAll() {
        for (int i=0; i<getCount(); i++) {
            select(i);
        }
    }
    
    /**
     * De-selects all the items from this adapter.
     */
    public void unselectAll() {
        selection.clear();
    }
    
    /**
     * Returns true if list row at specified position is selected, false otherwise.
     * @param position
     * @return
     */
    public boolean isSelected(int position) {
        Integer val = selection.get(position);
        if (val == null)
            return false;
        return (val == STATE_SELECTED);
    }
    
    /**
     * Returns list of selected items.
     * @return
     */
    public ArrayList<T> getSelectedItems() {
        ArrayList<T> selectedItems = new ArrayList<T>();
        for (int i=0; i<getCount(); i++) {
            if (isSelected(i)) {
                selectedItems.add(getItem(i));
            }
        }
        return selectedItems;
    }

    /**
     * Adds given item to this adapter.
     * @param item
     */
    public void add(T item) {
        items.add(item);
    }
    
    /**
     * Removes specified object from the adapter and returns success.
     * @param item
     * @return
     */
    public boolean remove(T item) {
        return items.remove(item);
    }
    
    /**
     * Clears existing items from the adapter and sets given list as the data. 
     * If null is provided, this method clears the existing values. 
     * This avoids null value errors. 
     * @param items
     */
    public void setItems(ArrayList<T> items) {
        if (items == null) {
            this.items.clear();
        } else {
            this.items = items;
        }
        this.selection.clear();
    }
    
    /**
     * Clears all items from this adapter.
     */
    public void clear() {
        items.clear();
        selection.clear();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public T getItem(int index) {
        //Check if the size of items is greater than the index
        if(index >= 0 && items.size()>index)
            return items.get(index);
        return null;
    }

    public int getPosition(T item) {
        int pos = items.indexOf(item);
        return pos;
    }
    
    @Override
    public long getItemId(int index) {
        return index;
    }
    
    public ArrayList<T> getItems() {
        return items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup adapter) {
        try {
            if (convertView == null) {
                // create list row
                LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflator.inflate(getListItemLayoutResId(), null);
                
                // apply a tag to this list row
                BaseViewHolder tag = getTag(convertView);
                convertView.setTag(tag);
            }
    
            // get the tag for this list row
            BaseViewHolder tag = (BaseViewHolder) convertView.getTag();
            // put position into the holder object
            tag.position = position;
            
            // get model data for this list row
            T model = items.get(position);
            
            // now render data for this list row
            render(tag, model);
            
            return convertView;
        } catch(Exception ex) {
            logger.error(ex);
        }
        
        return convertView;
    }
    
    

    /**
     * Sub-class should override this method to render model's data to ViewHolder tag.
     * @param tag
     * @param model
     */
    public abstract void render(BaseViewHolder tag, T model);

    /**
     * Sub-class should override this method and return ViewHolder tag that
     * is to be applied to the given convertView.
     * @param convertView
     * @return
     */
    public abstract BaseViewHolder getTag(View convertView);

    /**
     * Sub-class should override this method to return layoutId of list item layout.
     * @return
     */
    public abstract int getListItemLayoutResId();

    /**
     * Base class for ViewHolders in individual adapters.
     */
    public static class BaseViewHolder {
        public int position;
        public String videoId;
    }
    
}
