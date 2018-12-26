package com.maurya.mx.mxlib.core;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mukesh on 5/9/18.
 */
@Deprecated
public abstract class MxRecyclerAdapter<M, B extends ViewDataBinding> extends RecyclerView.Adapter<MxRecyclerAdapter.MxViewHolder> {

    private final int VIEW_ITEM = 1;
    private final int VIEW_PROGRESS = 0;
    private boolean loading = false;
    private boolean isLoadMoreEnabled = false;
    private int loadMoreRes = 0;//R.layout.loading_indicator;
    private final ArrayList<M> data;
    private final ArrayList<M> temp;
    private int layout;
    private OnRecyclerViewItemClick<M> recyclerViewItemClick;
    private OnRecyclerViewItemCheckChange<M> recyclerViewItemCheckChange;
    private ArrayList<OnDataUpdate<M>> onDataUpdateArrayList;

    public interface OnRecyclerViewItemClick<M> {
        void onRecyclerViewItemClick(View view, M model);
    }

    public interface OnRecyclerViewItemCheckChange<M> {
        void onRecyclerViewItemCheckChange(View view, boolean isCheck, M model);
    }

    public interface OnHolderItemClick {
        void onHolderItemClick(View view, int position);
    }

    public interface OnHolderItemCheckChange {
        void onHolderItemCheckChange(View view, boolean isCheck, int position);
    }

    public interface OnFilter<M> {
        boolean onFilterApply(@Nullable Object filter, @NonNull M model);

        void onFilterResult(ArrayList<M> filteredList);
    }

    public interface OnLoadMoreListener {
        boolean onLoadMore();
    }

    public interface OnDataUpdate<M> {
        public void onDataUpdate(ArrayList<M> data);
    }


    public MxRecyclerAdapter(@LayoutRes int layout) {
        data = new ArrayList<>();
        temp = new ArrayList<>();
        temp.addAll(data);
        enableDataObserver();
        this.layout = layout;
        onDataUpdateArrayList = new ArrayList<>();
    }

    public void onCreatingHolder(@NonNull B binding, @NonNull MxViewHolder holder) {

    }

    public abstract void onBind(@NonNull B binding, @NonNull M model);

    public void onBind(@NonNull B binding, @NonNull M model, @NonNull List<Object> payloads) {
    }


    public final ArrayList<M> getData() {
        return data;
    }

    public final ArrayList<M> getTemp() {
        return temp;
    }

    public void clear(boolean deepClean) {
        data.clear();
        if (deepClean) {
            temp.clear();
        }
    }

    public void remove(M model) {
        data.remove(model);
        temp.remove(model);
        notifyDataSetChanged();
    }


    private void clearFilter() {
        data.clear();
        data.addAll(temp);
    }

    public void add(M model) {
        data.add(model);
        temp.add(model);
        notifyDataSetChanged();
    }

    public void addAll(List<M> addAll, boolean deepCopy) {
        data.addAll(addAll);
        if (deepCopy) {
            temp.addAll(addAll);
        }
        notifyDataSetChanged();
    }

    public MxRecyclerAdapter<M, B> setRecyclerViewItemClick(OnRecyclerViewItemClick<M> recyclerViewItemClick) {
        this.recyclerViewItemClick = recyclerViewItemClick;
        return this;
    }

    public MxRecyclerAdapter<M, B> setRecyclerViewItemCheckChange(OnRecyclerViewItemCheckChange<M> recyclerViewItemCheckChange) {
        this.recyclerViewItemCheckChange = recyclerViewItemCheckChange;
        return this;
    }


    public void onItemClick(View view, M model) {
        if (recyclerViewItemClick != null)
            recyclerViewItemClick.onRecyclerViewItemClick(view, model);
    }

    public void onItemCheckChange(View view, boolean isCheck, M model) {
        if (recyclerViewItemCheckChange != null)
            recyclerViewItemCheckChange.onRecyclerViewItemCheckChange(view, isCheck, model);
    }


    public void performFilter(Object text, OnFilter<M> onFilter) {
        ArrayList<M> result = new ArrayList<>();
        if (text.toString().length() <= 0) {
            result.addAll(temp);
        } else {
            result.clear();
            for (M d : temp) {
                if (d instanceof OnFilter) {
                    onFilter = (OnFilter<M>) d;
                }
                if (onFilter != null) {
                    if (onFilter.onFilterApply(text, d)) {
                        result.add(d);
                    }
                }
            }
        }
        if (onFilter != null) {
            onFilter.onFilterResult(result);
        }
    }



    @Override
    public final MxViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_ITEM) {
            MxViewHolder mxViewHolder = new MxViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                    layout, parent, false));
            onCreatingHolder((B) mxViewHolder.binding, mxViewHolder);
            mxViewHolder.setHolderItemClick(new OnHolderItemClick() {
                @Override
                public void onHolderItemClick(View view, int position) {
                    if (position != -1)
                        onItemClick(view, data.get(position));
                }
            });
            mxViewHolder.setHolderItemCheckChange(new OnHolderItemCheckChange() {
                @Override
                public void onHolderItemCheckChange(View view, boolean isCheck, int position) {
                    if (position != -1) {
                        onItemCheckChange(view, isCheck, data.get(position));
                    }
                }
            });
            return mxViewHolder;
        } else {
            View view;
            if (loadMoreRes == -1) {
                view = getProgressView(parent.getContext());
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(loadMoreRes, parent, false);
            }
            return new ProgressViewHolder(view);
        }
    }

    @Override
    public final void onBindViewHolder(@NonNull MxViewHolder holder, int position) {
        if (!holder.isLoadingView) {
            onBind((B) holder.binding, data.get(position));
        }
    }

    @Override
    public final void onBindViewHolder(@NonNull MxViewHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
        if (!holder.isLoadingView) {
            onBind((B) holder.binding, data.get(position), payloads);
        }
    }

    @Override
    public int getItemCount() {
        if (data == null) return 0;
        if (isLoadMoreEnabled && loading)
            return data.size() + 1;
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadMoreEnabled && loading) {
            if (position == getItemCount() - 1) {
                return VIEW_PROGRESS;
            } else return VIEW_ITEM;
        }
        return VIEW_ITEM;
    }

    private View getProgressView(Context context) {
        View view = new FrameLayout(context);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER_HORIZONTAL;
        progressBar.setLayoutParams(lp);
        ((ViewGroup) view).addView(progressBar);
        return view;
    }


    public void setLoadMoreComplete() {
        loading = false;
        notifyDataSetChanged();
    }

    public void setLoadMoreRes(int loadMoreRes) {
        this.loadMoreRes = loadMoreRes;
    }

    public MxRecyclerAdapter<M, B> setOnLoadMoreListener(RecyclerView recyclerView, final OnLoadMoreListener onLoadMoreListener) {
        if (recyclerView != null && onLoadMoreListener != null) {

            final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

            if (layoutManager instanceof GridLayoutManager) {
                ((GridLayoutManager) layoutManager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (getItemViewType(position) == VIEW_PROGRESS)
                            return ((GridLayoutManager) layoutManager).getSpanCount();
                        return 1;
                    }
                });
            }

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    int totalItemCount = layoutManager.getItemCount();
                    int lastVisibleItem = 0;

                    if (layoutManager instanceof StaggeredGridLayoutManager) {
                        int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(null);
                        lastVisibleItem = getLastVisibleItem(lastVisibleItemPositions);
                    } else if (layoutManager instanceof GridLayoutManager) {
                        lastVisibleItem = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                    } else if (layoutManager instanceof LinearLayoutManager) {
                        lastVisibleItem = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                    }
                    if (!loading && totalItemCount <= (lastVisibleItem + 2)) {
                        new android.os.Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                boolean previous = loading;
                                loading = onLoadMoreListener.onLoadMore();
                                if (loading != previous) {
                                    if (!previous) {
                                        notifyItemInserted(getItemCount() - 1);
                                    } else if (previous == true && loading == false) {
                                        notifyItemRemoved(getItemCount() - 1);
                                    }
                                }
                            }
                        });
                    }
                }
            });

            isLoadMoreEnabled = true;
        }
        return this;
    }


    private int getLastVisibleItem(int[] lastVisibleItemPositions) {
        int maxSize = 0;
        for (int i = 0; i < lastVisibleItemPositions.length; i++) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i];
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i];
            }
        }
        return maxSize;
    }


    public static class MxViewHolder extends RecyclerView.ViewHolder /*implements SwipeOpenViewHolder*/ {

        private ViewDataBinding binding;
        boolean isLoadingView;
        private OnHolderItemClick holderItemClick;
        private OnHolderItemCheckChange holderItemCheckChange;
        public View swipeView;
        public int startViewSize = 0, endViewSize = 0;

        public MxViewHolder(ViewDataBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;

        }

        public MxViewHolder(View view) {
            super(view);
        }

        void setHolderItemClick(OnHolderItemClick holderItemClick) {
            this.holderItemClick = holderItemClick;
        }

        public void setHolderItemCheckChange(OnHolderItemCheckChange holderItemCheckChange) {
            this.holderItemCheckChange = holderItemCheckChange;
        }

        private View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holderItemClick.onHolderItemClick(view, getAdapterPosition());
            }
        };

        public View.OnClickListener getClickListener() {
            return mOnClickListener;
        }

        public CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                holderItemCheckChange.onHolderItemCheckChange(compoundButton, b, getAdapterPosition());
            }
        };

        public CompoundButton.OnCheckedChangeListener getCheckedChangeListener() {
            return checkedChangeListener;
        }

        public void setEnableSwipeToDelete(View swipeView, int startViewSize, int endViewSize) {
            this.swipeView = swipeView;
            this.startViewSize = startViewSize;
            this.endViewSize = endViewSize;
        }

//        @NonNull
//        @Override
//        public View getSwipeView() {
//            return swipeView;
//        }
//
//        @NonNull
//        @Override
//        public RecyclerView.ViewHolder getViewHolder() {
//            return this;
//        }
//
//        @Override
//        public float getEndHiddenViewSize() {
//            return endViewSize;
//        }
//
//        @Override
//        public float getStartHiddenViewSize() {
//            return startViewSize;
//        }
//
//        @Override
//        public void notifyStartOpen() {
//
//        }
//
//        @Override
//        public void notifyEndOpen() {
//
//        }
    }

    private class ProgressViewHolder extends MxViewHolder {
        ProgressViewHolder(View v) {
            super(v);
            isLoadingView = true;
            swipeView = v;
            startViewSize = 0;
            endViewSize = 0;
            setEnableSwipeToDelete(v,0,0);
        }
    }

    public void enableSwipeAction(RecyclerView recyclerView) {
//        SwipeOpenItemTouchHelper helper = new SwipeOpenItemTouchHelper(new SwipeOpenItemTouchHelper.SimpleCallback(
//                SwipeOpenItemTouchHelper.START | SwipeOpenItemTouchHelper.END));
//        helper.attachToRecyclerView(recyclerView);
    }


    public MxRecyclerAdapter<M, B> addOnDataUpdateListener(OnDataUpdate<M> onDataUpdate) {
        this.onDataUpdateArrayList.add(onDataUpdate);
        return this;
    }

    private void onDataUpdate() {
        for (OnDataUpdate<M> onDataUpdate : onDataUpdateArrayList) {
            onDataUpdate.onDataUpdate(getData());
        }
    }

    public final void disableDataObserver() {
        unregisterAdapterDataObserver(dataChangeObs);
    }

    public final void enableDataObserver() {
        registerAdapterDataObserver(dataChangeObs);
    }

    private RecyclerView.AdapterDataObserver dataChangeObs = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            onDataUpdate();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
            onDataUpdate();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            super.onItemRangeChanged(positionStart, itemCount, payload);
            onDataUpdate();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            onDataUpdate();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            onDataUpdate();
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
            onDataUpdate();
        }
    };
}
