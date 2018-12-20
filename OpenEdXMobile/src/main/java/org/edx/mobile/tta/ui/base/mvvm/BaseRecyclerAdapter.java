package org.edx.mobile.tta.ui.base.mvvm;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.edx.mobile.tta.ui.interfaces.BaseAdapter;
import org.edx.mobile.tta.ui.interfaces.OnRecyclerItemClickListener;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<BaseViewHolder>
        implements BaseAdapter<T> {

    private List<T> mList;
    private OnRecyclerItemClickListener<T> mListener;
    private Context mContext;

    public BaseRecyclerAdapter(Context context) {
        this.mContext = context;
        this.mList = new ArrayList<>();
    }

    @Override
    public Context getContext() {
        return mContext;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, viewType, parent, false);
        BaseViewHolder holder = new BaseViewHolder(binding);
        onCreatingHolder(binding, holder);
        return holder;
    }

    public void onCreatingHolder(@NonNull ViewDataBinding binding, @NonNull BaseViewHolder holder) {

    }

    public abstract void onBind(@NonNull ViewDataBinding binding, @NonNull T model, @Nullable OnRecyclerItemClickListener<T> listener);

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        onBind(holder.getBinding(), getItem(position), mListener);
    }

    @Override
    public int getItemViewType(int position) {
        return getItemLayout(position);
    }

    public abstract int getItemLayout(int position);

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
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
        return mList.get(position);
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
        return mList == null || mList.isEmpty();
    }
}
