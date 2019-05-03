package com.maurya.mx.mxlib.core;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mukesh on 8/9/18.
 */

public abstract class MxBaseAdapter<T> extends RecyclerView.Adapter<MxViewHolder> implements MxAdapter<T> {
    private List<T> mList;
    private OnRecyclerItemClickListener<T> mListener;
    private Context mContext;

    public MxBaseAdapter(Context context) {
        this.mContext = context;
        this.mList = new ArrayList<>();
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public MxViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //TODO: check if view type not 0
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, viewType, parent, false);
        MxViewHolder mxHolder = new MxViewHolder(binding);
        onCreatingHolder(binding, mxHolder);
        return mxHolder;
    }

    public void onCreatingHolder(@NonNull ViewDataBinding binding, @NonNull MxViewHolder holder) {

    }

    public abstract void onBind(@NonNull ViewDataBinding binding, @NonNull T model, @Nullable OnRecyclerItemClickListener<T> listener);

    @Override
    public void onBindViewHolder(MxViewHolder holder, int position) {
        onBind(holder.getBinding(), getItem(position),mListener);
    }

    @Override
    public int getItemViewType(int position) {
        return getItemLayout(position);
    }

    public abstract int getItemLayout(int position);


    @Override
    public int getItemCount() {
        return mList==null?0:mList.size();
    }

    @Override
    public void setItemClickListener(OnRecyclerItemClickListener<T> listener) {
        this.mListener = listener;
    }

    @Override
    public List<T> getItems() {
        return mList;
    }

    @Override
    public void setItems(List<T> items) {
        if (items == null)
            return;
        this.mList = items;
        notifyDataSetChanged();
    }

    @Override
    public T getItem(int position) {
        try {
            return mList.get(position);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void add(T item) {
        if (item == null)
            return;
        mList.add(item);
        notifyItemInserted(mList.size() - 1);
    }

    @Override
    public void addAll(List<T> items) {
        if (items == null)
            return;
        this.mList.addAll(items);
        notifyItemRangeInserted(this.mList.size() - items.size(), items.size());
    }

    @Override
    public void removeLastItem() {
        this.mList.remove(getItemCount() - 1);
        notifyItemRemoved(getItemCount() - 1);
    }

    @Override
    public void clear() {
        mList.clear();
        notifyDataSetChanged();
    }

    @Override
    public void remove(T item) {
        if (item == null)
            return;
        int position = mList.indexOf(item);
        mList.remove(item);
        notifyItemRemoved(position);
    }

    @Override
    public boolean isEmpty() {
        return mList==null||mList.isEmpty();
    }

    public int getItemPosition(T item){
        return mList.indexOf(item);
    }

    public void add(int index, T item) {
        if (item == null){
            return;
        }
        mList.add(index, item);
        notifyItemInserted(index);
    }
}
