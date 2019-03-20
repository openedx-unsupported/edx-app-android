package org.edx.mobile.tta.ui.base;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.maurya.mx.mxlib.core.MxViewHolder;

import org.edx.mobile.tta.ui.interfaces.OnTaItemClickListener;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseRecyclerAdapter<T> extends RecyclerView.Adapter<MxViewHolder> {

    private int headerLayout, footerLayout;
    private OnTaItemClickListener<T> itemClickListener;
    private OnHeaderClickListener headerClickListener;
    private OnFooterClickListener footerClickListener;
    private List<T> items;

    public BaseRecyclerAdapter() {
        items = new ArrayList<>();
    }

    @NonNull
    @Override
    public MxViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, viewType, parent, false);
        return new MxViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MxViewHolder holder, int position) {
        T item = null;
        if (position < getItemCount() - 1){
            if (isHeaderEnabled()){
                if (position > 0) {
                    item = items.get(position - 1);
                }
            } else {
                item = items.get(position);
            }
        } else if (!isFooterEnabled()){
            if (isHeaderEnabled()) {
                if (position > 0) {
                    item = items.get(position-1);
                }
            } else {
                item = items.get(position);
            }
        }
        onBind(holder.getBinding(), item, headerClickListener, footerClickListener, itemClickListener);
    }

    public abstract void onBind(ViewDataBinding binding, T item,
                                OnHeaderClickListener headerClickListener,
                                OnFooterClickListener footerClickListener,
                                OnTaItemClickListener<T> itemClickListener);

    @Override
    public int getItemCount() {
        int count = items.size();
        if (isHeaderEnabled()){
            count++;
        }
        if (isFooterEnabled()){
            count++;
        }
        return count;
    }

    public int getItemPosition(T item){
        if (items == null){
            return -1;
        }
        return items.indexOf(item);
    }

    public void setHeaderLayout(int headerLayout) {
        this.headerLayout = headerLayout;
    }

    public boolean isHeaderEnabled(){
        return headerLayout != 0;
    }

    public void setFooterLayout(int footerLayout) {
        this.footerLayout = footerLayout;
    }

    public boolean isFooterEnabled(){
        return footerLayout != 0;
    }

    public void setItemClickListener(OnTaItemClickListener<T> listener) {
        this.itemClickListener = listener;
    }

    public void setHeaderClickListener(OnHeaderClickListener headerClickListener) {
        this.headerClickListener = headerClickListener;
    }

    public void setFooterClickListener(OnFooterClickListener footerClickListener) {
        this.footerClickListener = footerClickListener;
    }

    public void add(@NonNull T item){
        items.add(item);
        notifyDataSetChanged();
    }

    public void addAll(@NonNull List<T> items){
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void set(@NonNull List<T> items){
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    public void clear(){
        items.clear();
        notifyDataSetChanged();
    }

    public interface OnHeaderClickListener extends View.OnClickListener {}

    public interface OnFooterClickListener extends View.OnClickListener {}
}
