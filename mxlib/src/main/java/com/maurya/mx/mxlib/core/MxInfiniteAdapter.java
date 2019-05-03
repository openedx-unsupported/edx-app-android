package com.maurya.mx.mxlib.core;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.maurya.mx.mxlib.R;

/**
 * Created by mukesh on 1/9/18.
 */

public abstract class MxInfiniteAdapter<T> extends MxBaseAdapter<T> {
    private boolean isLoading = false;
    private boolean isLoadMoreEnabled = true;
    private EndlessRecyclerViewScrollListener mScrollListener;
    private int mLoadMoreRes, mItemLayout;

    public MxInfiniteAdapter(Context context) {
        super(context);
    }


    @Override
    public int getItemLayout(int position) {
            if (isLoadMoreDisplayable()&&position == getItemCount() - 1) {
                return getLoadingMoreLayout();
            } else return getItemLayout();
    }

    @Override
    public void onBindViewHolder(MxViewHolder holder, int position) {
        if (isLoadMoreDisplayable() &&position == getItemCount()-1)
            return;
        else
            super.onBindViewHolder(holder, position);
    }

    public int getItemLayout() {
        return mItemLayout;
    }

    public void setItemLayout(int itemLayout) {
        this.mItemLayout = itemLayout;
    }

    protected int getLoadingMoreLayout() {
        mLoadMoreRes= mLoadMoreRes == 0 ? R.layout.mx_item_progress_view_layout : mLoadMoreRes;
        return mLoadMoreRes;
    }

    @Override
    public int getItemCount() {
        if (isLoadMoreDisplayable())
            return super.getItemCount() + 1;
        else
            return super.getItemCount();
    }

    protected boolean isLoadMoreDisplayable() {
        return isLoadMoreEnabled && isLoading;
    }

    public void reset(boolean isNotify) {
        if (mScrollListener != null)
            mScrollListener.resetState();
        getItems().clear();
        if (isNotify)
            notifyDataSetChanged();
    }

    public void setLoadingDone() {
        isLoading = false;
        notifyDataSetChanged();
    }

    public void setLoadMoreRes(int loadMoreRes) {
        this.mLoadMoreRes = loadMoreRes;
    }

    public void setOnLoadMoreListener(final RecyclerView recyclerView, final OnLoadMoreListener onLoadMoreListener) {
        isLoadMoreEnabled = true;
        getLoadingMoreLayout();
        if (recyclerView != null && onLoadMoreListener != null) {
            mScrollListener = new EndlessRecyclerViewScrollListener(recyclerView.getLayoutManager()) {

                @Override
                public void onLoadMore(final int page, int totalItemsCount) {
                    recyclerView.post(new Runnable() {
                        @Override
                        public void run() {
                            isLoading = onLoadMoreListener.onLoadMore(page);
                            if (isLoading) {
                                notifyItemChanged(getItemCount() - 1);
                            } else {
                                notifyItemRemoved(getItemCount() - 1);
                            }
                        }
                    });
                }
            };
            recyclerView.addOnScrollListener(mScrollListener);
        }
    }

    public interface OnLoadMoreListener {
        boolean onLoadMore(int page);
    }

}
