package com.maurya.mx.mxlib.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.maurya.mx.mxlib.R;
import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.databinding.MxFiniteRecyclerViewLayoutBinding;

/**
 * Created by mukesh on 10/9/18.
 */

public class MxFiniteRecyclerView extends LinearLayout {
    private static final int DEFAULT_MAX_ITEM_COUNT = 5;   //default item count
    private static final int MX_SPAN_COUNT = 1;
    private boolean mCanShowVertical = false, mCanHideTitleLayout = false;
    private MxFiniteRecyclerViewLayoutBinding mBinding;
    private boolean isDividerEnabled = false;

    //List title
    private boolean mTitleVisible;
    private String mTitleText = "";
    private Drawable mTitleDrawableStart = null;
    private Drawable mTitleDrawableTop = null;
    private Drawable mTitleDrawableEnd = null;
    private Drawable mTitleDrawableBottom = null;
    private float mTitleTextSize;
    private int mTitleTextColor;
    private Typeface mTitleTextFont;
    private float mTitleMarginStart;
    private float mTitleMarginTop;
    private float mTitleMarginEnd;
    private float mTitleMarginBottom;

    //List view more
    private boolean mMoreButtonVisible;
    private String mMoreButtonText = "SEE ALL";
    private Drawable mMoreButtonDrawableStart = null;
    private Drawable mMoreButtonDrawableTop = null;
    private Drawable mMoreButtonDrawableEnd = null;
    private Drawable mMoreButtonDrawableBottom = null;
    private float mMoreButtonTextSize;
    private int mMoreButtonTextColor;
    private Typeface mMoreButtonTextFont;
    private float mMoreButtonMarginStart;
    private float mMoreButtonMarginTop;
    private float mMoreButtonMarginEnd;
    private float mMoreButtonMarginBottom;

    private int mMaxItem = DEFAULT_MAX_ITEM_COUNT;
    private int mItemLayout,mHeaderLayout, mFooterLayout;  //default layout can be set here , for now it's none
    private OnClickListener mMoreButtonListener;
    private MxFiniteAdapter<?> mAdapter;

    public MxFiniteRecyclerView(Context context) {
        super(context);
        init(context);
    }

    public MxFiniteRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MxFiniteRecyclerView);

        mTitleVisible = typedArray.getBoolean(R.styleable.MxFiniteRecyclerView_mx_titleVisible, true);
        mTitleText = typedArray.getString(R.styleable.MxFiniteRecyclerView_mx_titleText);
        mTitleDrawableStart = typedArray.getDrawable(R.styleable.MxFiniteRecyclerView_mx_titleDrawableStart);
        mTitleDrawableTop = typedArray.getDrawable(R.styleable.MxFiniteRecyclerView_mx_titleDrawableTop);
        mTitleDrawableEnd = typedArray.getDrawable(R.styleable.MxFiniteRecyclerView_mx_titleDrawableEnd);
        mTitleDrawableBottom = typedArray.getDrawable(R.styleable.MxFiniteRecyclerView_mx_titleDrawableBottom);
        mTitleTextSize = typedArray.getDimension(R.styleable.MxFiniteRecyclerView_mx_titleTextSize, getResources().getDimension(R.dimen.text_size_button_material));
        try {
            mTitleTextColor = typedArray.getColor(R.styleable.MxFiniteRecyclerView_mx_titleTextColor, getResources().getColor(R.color.colorAccent));
        } catch (Resources.NotFoundException e) {
            Log.d("_______LOG________", e.getMessage());
        }
        try {
            mTitleTextFont = ResourcesCompat.getFont(getContext(), typedArray.getResourceId(R.styleable.MxFiniteRecyclerView_mx_titleTextFont, 0));
        } catch (Resources.NotFoundException e) {
            mTitleTextFont = null;
        }
        mTitleMarginStart = typedArray.getDimension(R.styleable.MxFiniteRecyclerView_mx_titleMarginStart, 0);
        mTitleMarginTop = typedArray.getDimension(R.styleable.MxFiniteRecyclerView_mx_titleMarginTop, 0);
        mTitleMarginEnd = typedArray.getDimension(R.styleable.MxFiniteRecyclerView_mx_titleMarginEnd, 0);
        mTitleMarginBottom = typedArray.getDimension(R.styleable.MxFiniteRecyclerView_mx_titleMarginBottom, 0);

        mMoreButtonVisible = typedArray.getBoolean(R.styleable.MxFiniteRecyclerView_mx_moreButtonVisible, true);
        String moreText = typedArray.getString(R.styleable.MxFiniteRecyclerView_mx_moreButtonText);
        if (!TextUtils.isEmpty(moreText))
            mMoreButtonText=moreText;
        mMoreButtonDrawableStart = typedArray.getDrawable(R.styleable.MxFiniteRecyclerView_mx_moreButtonDrawableStart);
        mMoreButtonDrawableTop = typedArray.getDrawable(R.styleable.MxFiniteRecyclerView_mx_moreButtonDrawableTop);
        mMoreButtonDrawableEnd = typedArray.getDrawable(R.styleable.MxFiniteRecyclerView_mx_moreButtonDrawableEnd);
        mMoreButtonDrawableBottom = typedArray.getDrawable(R.styleable.MxFiniteRecyclerView_mx_moreButtonDrawableBottom);
        mMoreButtonTextSize = typedArray.getDimension(R.styleable.MxFiniteRecyclerView_mx_moreButtonTextSize, getResources().getDimension(R.dimen.text_size_button_material));
        mMoreButtonTextColor = typedArray.getColor(R.styleable.MxFiniteRecyclerView_mx_moreButtonTextColor, getResources().getColor(R.color.colorAccent));
        try {
            mMoreButtonTextFont = ResourcesCompat.getFont(getContext(), typedArray.getResourceId(R.styleable.MxFiniteRecyclerView_mx_moreButtonTextFont, 0));
        } catch (Resources.NotFoundException e) {
            mMoreButtonTextFont = null;
        }
        mMoreButtonMarginStart = typedArray.getDimension(R.styleable.MxFiniteRecyclerView_mx_moreButtonMarginStart, 0);
        mMoreButtonMarginTop = typedArray.getDimension(R.styleable.MxFiniteRecyclerView_mx_moreButtonMarginTop, 0);
        mMoreButtonMarginEnd = typedArray.getDimension(R.styleable.MxFiniteRecyclerView_mx_moreButtonMarginEnd, 0);
        mMoreButtonMarginBottom = typedArray.getDimension(R.styleable.MxFiniteRecyclerView_mx_moreButtonMarginBottom, 0);

        mMaxItem = typedArray.getInt(R.styleable.MxFiniteRecyclerView_mx_itemLimit, DEFAULT_MAX_ITEM_COUNT);
        mItemLayout = typedArray.getResourceId(R.styleable.MxFiniteRecyclerView_mx_itemLayout, 0);
        mHeaderLayout = typedArray.getResourceId(R.styleable.MxFiniteRecyclerView_mx_headerLayout, 0);
        mFooterLayout = typedArray.getResourceId(R.styleable.MxFiniteRecyclerView_mx_footerLayout, 0);
        mCanShowVertical = typedArray.getBoolean(R.styleable.MxFiniteRecyclerView_mx_canShowVertical, false);
        mCanHideTitleLayout = typedArray.getBoolean(R.styleable.MxFiniteRecyclerView_mx_hideTitleLayout, false);
        isDividerEnabled = typedArray.getBoolean(R.styleable.MxFiniteRecyclerView_mx_titleDivider, false);
        typedArray.recycle();
        init(context);
        setValues();
    }

    private void setValues() {
        setTitle();
        setMoreButton();
        mBinding.dividerView.setVisibility(isDividerEnabled ? VISIBLE : GONE);
        hideTitleLayout(mCanHideTitleLayout);
    }

    private void init(Context context) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.mx_finite_recycler_view_layout, this, true);
        mBinding.mxRecyclerView.setNestedScrollingEnabled(false);
        if (mCanShowVertical)
            mBinding.mxRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        else
            mBinding.mxRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

    }

    public void setAdapter(MxFiniteAdapter<?> adapter) {
        this.mAdapter = adapter;
        mAdapter.setItemLayout(getItemLayout());//it's required
        setmMaxItem(mMaxItem);
        //set here header & footer layout into adapter
        if (getHeaderLayout() != 0) {
            mAdapter.setHeaderLayout(getHeaderLayout());
        }
        if (getFooterLayout() != 0) {
            mAdapter.setFooterLayout(getFooterLayout());
        }
        mBinding.mxRecyclerView.setAdapter(mAdapter);
    }

    public void addItemDecoration(RecyclerView.ItemDecoration decoration){
        mBinding.mxRecyclerView.addItemDecoration(decoration);
    }

    public int getRecyclerViewOrientation(){
        return ((LinearLayoutManager) mBinding.mxRecyclerView.getLayoutManager()).getOrientation();
    }

    public int getmMaxItem() {
        return mMaxItem;
    }

    public void setmMaxItem(int mMaxItem) {
        this.mMaxItem = mMaxItem;
        mAdapter.setItemLimit(mMaxItem);
    }

    public void setOnMoreButtonClickListener(OnClickListener listener) {
        this.mMoreButtonListener = listener;
        mBinding.mxViewMoreButton.setOnClickListener(mMoreButtonListener);

    }

    private void setTitle(){
        if (mTitleVisible) {
            mBinding.mxTitleText.setVisibility(VISIBLE);
            setTitleText(mTitleText);
            setTitleDrawables();
            setTitleTextSize(mTitleTextSize);
            setTitleTextColor(mTitleTextColor);
            setTitleTextFont(mTitleTextFont);
            setTitleMargins(mTitleMarginStart, mTitleMarginTop, mTitleMarginEnd, mTitleMarginBottom);
        } else {
            mBinding.mxTitleText.setVisibility(GONE);
        }
    }

    public void setmTitleVisible(boolean mTitleVisible) {
        this.mTitleVisible = mTitleVisible;
        setTitle();
    }

    public void setTitleText(String title) {
        this.mTitleText = title;
        mBinding.mxTitleText.setText(title);
    }

    public void setTitleDrawables(@DrawableRes int start, @DrawableRes int top,@DrawableRes int end,@DrawableRes int bottom){
        if (start != 0) mTitleDrawableStart = ContextCompat.getDrawable(getContext(), start);
        if (top != 0) mTitleDrawableTop = ContextCompat.getDrawable(getContext(), top);
        if (end != 0) mTitleDrawableEnd = ContextCompat.getDrawable(getContext(), end);
        if (bottom != 0) mTitleDrawableBottom = ContextCompat.getDrawable(getContext(), bottom);
        setTitleDrawables();
    }

    private void setTitleDrawables() {
        mBinding.mxTitleText.setCompoundDrawablesRelativeWithIntrinsicBounds(
                mTitleDrawableStart, mTitleDrawableTop, mTitleDrawableEnd, mTitleDrawableBottom
        );
    }

    public void setTitleTextSize(float textSize){
        mTitleTextSize = textSize;
        mBinding.mxTitleText.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
    }

    public void setTitleTextColor(@ColorInt int color){
        mTitleTextColor = color;
        mBinding.mxTitleText.setTextColor(color);
    }

    public void setTitleTextFont(Typeface typeface){
        mTitleTextFont = typeface;
        if (typeface != null){
            mBinding.mxTitleText.setTypeface(typeface);
        }
    }

    public void setTitleMargins(float start, float top, float end, float bottom){
        mTitleMarginStart = start;
        mTitleMarginTop = top;
        mTitleMarginEnd = end;
        mTitleMarginBottom = bottom;
        LayoutParams params = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        params.setMargins((int)start, (int)top, (int)end, (int)bottom);
        mBinding.mxTitleText.setLayoutParams(params);
    }

    private void setMoreButton(){
        if (mMoreButtonVisible) {
            mBinding.mxViewMoreButton.setVisibility(VISIBLE);
            setMoreButtonText(mMoreButtonText);
            setMoreButtonDrawables();
            setMoreButtonTextSize(mMoreButtonTextSize);
            setMoreButtonTextColor(mMoreButtonTextColor);
            setMoreButtonTextFont(mMoreButtonTextFont);
            setMoreButtonMargins(mMoreButtonMarginStart, mMoreButtonMarginTop, mMoreButtonMarginEnd, mMoreButtonMarginBottom);
            mBinding.mxViewMoreButton.setOnClickListener(mMoreButtonListener);
        } else {
            mBinding.mxViewMoreButton.setVisibility(GONE);
        }
    }

    public void setmMoreButtonVisible(boolean mMoreButtonVisible) {
        this.mMoreButtonVisible = mMoreButtonVisible;
        setMoreButton();
    }

    public void setMoreButtonText(String moreButtonText){
        this.mMoreButtonText = moreButtonText;
        mBinding.mxViewMoreButton.setText(mMoreButtonText);
    }

    public void setMoreButtonDrawables(@DrawableRes int start, @DrawableRes int top,@DrawableRes int end,@DrawableRes int bottom){
        if (start != 0) mMoreButtonDrawableStart = ContextCompat.getDrawable(getContext(), start);
        if (top != 0) mMoreButtonDrawableTop = ContextCompat.getDrawable(getContext(), top);
        if (end != 0) mMoreButtonDrawableEnd = ContextCompat.getDrawable(getContext(), end);
        if (bottom != 0) mMoreButtonDrawableBottom = ContextCompat.getDrawable(getContext(), bottom);
        setMoreButtonDrawables();
    }

    private void setMoreButtonDrawables() {
        mBinding.mxViewMoreButton.setCompoundDrawablesRelativeWithIntrinsicBounds(
                mMoreButtonDrawableStart, mMoreButtonDrawableTop, mMoreButtonDrawableEnd, mMoreButtonDrawableBottom
        );
    }

    public void setMoreButtonTextSize(float textSize){
        mMoreButtonTextSize = textSize;
        mBinding.mxViewMoreButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
    }

    public void setMoreButtonTextColor(int color){
        mMoreButtonTextColor = color;
        mBinding.mxViewMoreButton.setTextColor(color);
    }

    public void setMoreButtonTextFont(Typeface typeface){
        mMoreButtonTextFont = typeface;
        if (typeface != null){
            mBinding.mxViewMoreButton.setTypeface(typeface);
        }
    }

    public void setMoreButtonMargins(float start, float top, float end, float bottom){
        mMoreButtonMarginStart = start;
        mMoreButtonMarginTop = top;
        mMoreButtonMarginEnd = end;
        mMoreButtonMarginBottom = bottom;
        LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.setMargins((int)start, (int)top, (int)end, (int)bottom);
        mBinding.mxViewMoreButton.setLayoutParams(params);
    }

    public void hideTitleLayout(boolean b) {
        this.mCanHideTitleLayout = b;
        mBinding.titleLayout.setVisibility(mCanHideTitleLayout ? GONE : VISIBLE);
    }

    public boolean canShowVertical() {
        return mCanShowVertical;
    }

    public void setShowVertical(boolean canShowVertical) {
        this.mCanShowVertical = canShowVertical;
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

    public void setHeaderLayout(@LayoutRes int headerLayout) {
        this.mHeaderLayout = headerLayout;
    }

    public int getFooterLayout() {
        return mFooterLayout;
    }

    public void setFooterLayout(@LayoutRes int footerLayout) {
        this.mFooterLayout = footerLayout;
    }

    public void setVisibility(boolean canShow) {
        this.setVisibility(canShow ? VISIBLE : GONE);
    }

    protected void checkCanShowEmpty() {
        mBinding.mxEmptyView.setVisibility(mAdapter != null && mAdapter.isEmpty());
    }

    /**
     * Method to toggle progressBar
     *
     * @param b boolean
     */
    protected void canShowProgress(boolean b) {
        mBinding.mxProgressBar.setVisibility(b ? View.VISIBLE : View.GONE);
    }
}
