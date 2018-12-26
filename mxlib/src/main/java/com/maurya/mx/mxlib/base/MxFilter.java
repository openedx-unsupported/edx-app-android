package com.maurya.mx.mxlib.base;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mukesh on 1/6/18.
 */

public abstract class MxFilter<T> extends Filter {
    private List<T> items;
    private List<T> filteredItems;
    public MxFilter(List<T> itemsToFilter){
        this.items=itemsToFilter;
        filteredItems= new ArrayList<>();

    }
    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        String query = constraint.toString().toLowerCase().trim();
        FilterResults filterResults = new FilterResults();
        if (query.isEmpty()){   //Empty case
            filterResults.values=items;
            filterResults.count=0;
        }else{
            for (T t:items){
                doFilter(t,query);
            }
            filterResults.values=filteredItems;
            filterResults.count=1;
        }
        return filterResults;
    }

    protected abstract void doFilter(T t, String query);

    protected void addFilterItem(T t){
        filteredItems.add(t);
    }

//    @Override
//    protected void publishResults(CharSequence constraint, FilterResults results) {
//        adapter.setFilteredItems((List<T>) results.values);
//    }
}
