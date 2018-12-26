package com.maurya.mx.mxlib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.maurya.mx.mxlib.R;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.databinding.CommonRecyclerviewLayoutBinding;

/**
 * Created by mukesh on 10/9/18.
 */

public class MxRecyclerView extends LinearLayout {
    private int mItemLayout;
    private CommonRecyclerviewLayoutBinding mBinding;
    private MxInfiniteAdapter<?> mAdapter;
    private SwipeRefreshLayout.OnRefreshListener mRefreshListener;

    public MxRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public MxRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MxRecyclerView);
        mItemLayout = typedArray.getResourceId(R.styleable.MxRecyclerView_mx_itemLayout, 0);
        typedArray.recycle();
    }

    private void init(Context context) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.common_recyclerview_layout, this, true);
        mBinding.commonSwipeRefresh.setEnabled(false);
    }

    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        mBinding.commonSwipeRefresh.setEnabled(true);
        mBinding.commonSwipeRefresh.setOnRefreshListener(listener);
    }
    public void setRefreshing(boolean refreshing){
        mBinding.commonSwipeRefresh.setRefreshing(refreshing);
    }

    public void setAdapter(MxInfiniteAdapter<?> adapter) {
        this.mAdapter = adapter;
        mAdapter.setItemLayout(getItemLayout());
        mBinding.commonRecycler.setAdapter(mAdapter);
    }
    public int getItemLayout() {
        return mItemLayout;
    }

    public void setItemLayout(int itemLayout) {
        this.mItemLayout = itemLayout;
    }

    public void setLayoutManager(RecyclerView.LayoutManager layoutManager) {
        mBinding.commonRecycler.setLayoutManager(layoutManager);
    }

    public RecyclerView getRecyclerView() {
        return mBinding.commonRecycler;
    }

    public void checkCanShowEmpty() {
        mBinding.commonEmptyView.setVisibility(mAdapter!=null&&mAdapter.isEmpty());
    }

    /**
     * @param b
     */
    public void canShowProgress(boolean b) {
        mBinding.commonProgressBar.setVisibility(b ? View.VISIBLE : View.GONE);
    }
    public void hideAnyLoading(){
        canShowProgress(false);
        if (mAdapter!=null)
        mAdapter.setLoadingDone();
    }
}
