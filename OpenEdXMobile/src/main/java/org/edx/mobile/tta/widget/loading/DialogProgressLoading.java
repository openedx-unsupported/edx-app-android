package org.edx.mobile.tta.widget.loading;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import org.edx.mobile.R;

/**
 * Loading Dialog
 * Created by Arjun on 20/09/18.
 */

public class DialogProgressLoading extends Dialog {

    private ImageView mLogoImageView;

    private TextView mMsgTextView;

    private Context mContext;

    private String mMessage = "Loading...";

    private int mImgResId = R.drawable.ic_loading;

    private AnimationDrawable mAnimationDrawable;

    public DialogProgressLoading(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public DialogProgressLoading(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_progress_loading);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        mLogoImageView = (ImageView) findViewById(R.id.iv_logo);
        mMsgTextView = (TextView) findViewById(R.id.tv_msg);
        setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onStart() {
        if (mMsgTextView != null) {
            mMsgTextView.setText(mMessage);
        }

        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_progess_loading);
        animation.setInterpolator(new LinearInterpolator());
        mLogoImageView.startAnimation(animation);
        super.onStart();
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    @Override
    public void setCanceledOnTouchOutside(boolean cancel) {
        super.setCanceledOnTouchOutside(cancel);
    }

    public void setImage(int imageResId) {
        mImgResId = imageResId;
    }

}
