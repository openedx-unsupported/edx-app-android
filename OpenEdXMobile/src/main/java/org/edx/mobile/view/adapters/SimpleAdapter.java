package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by marcashman on 2014-11-14.
 */
public abstract class SimpleAdapter<T> extends BaseAdapter {

    protected List<T> data;
    protected Context context;
    private LayoutInflater inflater;

    public SimpleAdapter(Context context) {

        this.context = context;
        inflater = LayoutInflater.from(context);

    }

    public SimpleAdapter(Context context, List<T> data) {
        this(context);

        this.data = data;
    }

    public void setItems(List<T> data) {

        this.data = data;
        this.notifyDataSetChanged();

    }

    public void appendItems(List<T> data) {

        if (this.data == null) {
            this.data = new ArrayList<T>();
        }
        this.data.addAll(data);
        this.notifyDataSetChanged();

    }

    public void addItem(T item) {

        if (this.data == null) {
            this.data = new ArrayList<T>();
        }
        this.data.add(item);
        this.notifyDataSetChanged();

    }

    public void addItem(int index, T item) {

        if (this.data == null) {
            this.data = new ArrayList<T>();
        }
        this.data.add(index, item);
        this.notifyDataSetChanged();

    }

    public void removeItem(int index) {

        if (this.data != null) {
            this.data.remove(index);
            this.notifyDataSetChanged();
        }

    }

    public void removeItem(T item) {

        if (this.data != null) {
            this.data.remove(item);
            this.notifyDataSetChanged();
        }

    }

    public List<T> getItems() {

        return data;
    }

    protected View inflate(int layout, ViewGroup container) {

        return inflater.inflate(layout, container, false);
    }

    public void clearAll() {

        if (data != null) {
            data.clear();
            this.notifyDataSetChanged();
        }

    }

    @Override
    public int getCount() {

        if (data == null) {
            return 0;
        }

        return data.size();
    }

    @Override
    public T getItem(int position) {

        if (data == null) {
            return null;
        }

        return data.get(position);
    }

    @Override
    public long getItemId(int position) {

        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflate(getRowLayout(), parent);
        }

        setUpView(convertView, getItem(position));

        return convertView;
    }

    protected abstract int getRowLayout();
    protected abstract void setUpView(View view, T item);

}
