package com.maurya.mx.mxlib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.maurya.mx.mxlib.R;


/**
 * Created by mukesh on 30/7/18.
 */

public class EmptyView extends LinearLayout {
    private ImageView mImage;
    private TextView mTextView;
    private Drawable mDrawable;
    private String mMessage;

    public EmptyView(Context context) {
        super(context);
        init(context);
    }

    public EmptyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.EmptyView);
        mDrawable = typedArray.getDrawable(R.styleable.EmptyView_mx_image);
        mMessage = typedArray.getString(R.styleable.EmptyView_mx_message);
        typedArray.recycle();
        init(context);
    }

    private void init(Context context) {
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        LayoutInflater.from(context).inflate(R.layout.mx_empty_view_layout, this);
        mImage = (ImageView) this.findViewById(R.id.mx_error_image);
        mTextView = (TextView) this.findViewById(R.id.mx_error_text);
        mImage.setImageDrawable(mDrawable);
        mTextView.setText(mMessage);
    }

    public void setEmptyImage(@DrawableRes int icon) {
        mImage.setImageResource(icon);
    }

    public void setEmptyText(String msg) {
        mTextView.setText(msg);
    }

    public void setVisibility(boolean canShow) {
        this.setVisibility(canShow ? VISIBLE : GONE);
    }
}