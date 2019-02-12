package com.maurya.mx.mxlib.core;

import android.content.Context;
import android.view.View;

/**
 * Created by mukesh on 6/9/18.
 */

public abstract class MxFiniteAdapter<T> extends MxBaseAdapter<T> {
    private int mHeaderLayout;
    private int mFooterLayout;
    private int mItemLayout;
    private int mItemLimit = 5; //Default max count
    private OnHeaderClickLister mHeaderListener;
    private OnFooterClickLister mFooterListener;

    /**
     * Base constructor.
     * Allocate adapter-related objects here if needed.
     *
     * @param context Context needed to retrieve LayoutInflater
     */
    public MxFiniteAdapter(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(MxViewHolder holder, int position) {
        if (isHeaderViewEnabled() && position == 0) {  //handle header view
            holder.getBinding().getRoot().setOnClickListener(getHeaderListener());
            return;
        } else if (isFooterDisplayable()&& position == getItemCount() - 1) {  //handle footer view
            holder.getBinding().getRoot().setOnClickListener(getFooterListener());
            return;
        } else {
            int offset = 0;
            if (isHeaderViewEnabled())
                offset = 1;
            super.onBindViewHolder(holder, position - offset);
        }
    }

    @Override
    public int getItemLayout(int position) {
        if (isHeaderViewEnabled() && position == 0)
            return getHeaderLayout();
        else if ( isFooterDisplayable()&& position == getItemCount() - 1)
            return getFooterLayout();
        else
            return getItemLayout();
    }

    private boolean isFooterViewEnabled() {
        return getFooterLayout() != 0;
    }

    private boolean isHeaderViewEnabled() {
        return getHeaderLayout() != 0;
    }

    @Override
    public int getItemCount() {
        int offset = isHeaderViewEnabled() ? 1 : 0;
        offset += isFooterDisplayable() ? 1 : 0;
        return Math.min(mItemLimit, super.getItemCount()) + offset;
    }

    public boolean isFooterDisplayable() {
//        return isFooterViewEnabled()&&super.getItemCount() > mItemLimit;
        return isFooterViewEnabled();
    }

    public int getItemLayout() {
        return mItemLayout;
    }

    public void setItemLayout(int itemLayout) {
        this.mItemLayout = itemLayout;
    }

    public int getHeaderLayout() {
        return mHeaderLayout;
    }

    public void setHeaderLayout(int headerLayout) {
        this.mHeaderLayout = headerLayout;
    }

    public int getFooterLayout() {
        return mFooterLayout;
    }

    public void setFooterLayout(int footerLayout) {
        this.mFooterLayout = footerLayout;
    }

    public int getItemLimit() {
        return mItemLimit;
    }

    public void setItemLimit(int itemLimit) {
        this.mItemLimit = itemLimit;
    }

    public OnHeaderClickLister getHeaderListener() {
        return mHeaderListener;
    }

    public void setHeaderListener(OnHeaderClickLister headerListener) {
        this.mHeaderListener = headerListener;
    }

    public OnFooterClickLister getFooterListener() {
        return mFooterListener;
    }

    public void setFooterListener(OnFooterClickLister footerListener) {
        this.mFooterListener = footerListener;
    }

    public interface OnHeaderClickLister extends View.OnClickListener {

    }

    public interface OnFooterClickLister extends View.OnClickListener {

    }
}
