package org.edx.mobile.tta.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TViewFormEdittextBinding;

import androidx.annotation.Nullable;

public class FormEditText extends LinearLayout {

    private TViewFormEdittextBinding mBinding;

    private int inputType = EditorInfo.TYPE_NULL;
    private String text = "";
    private String hint = "";

    public FormEditText(Context context) {
        super(context);
    }

    public FormEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FormEditText);

        inputType = typedArray.getInt(R.styleable.FormEditText_android_inputType, EditorInfo.TYPE_NULL);
        text = typedArray.getString(R.styleable.FormEditText_android_text);
        hint = typedArray.getString(R.styleable.FormEditText_android_hint);

        typedArray.recycle();
        init(context);
        setValues();
    }

    private void init(Context context){
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.t_view_form_edittext, this, true);
    }

    private void setValues() {
        setInputType(inputType);
        setText(text);
        setHint(hint);
    }

    public void setInputType(int type){
        this.inputType = type;
        mBinding.textInputEditText.setInputType(type);
    }

    public void setText(String text){
        this.text = text;
        mBinding.textInputEditText.setText(text);
    }

    public void setHint(String hint){
        this.hint = hint;
        mBinding.textInputEditText.setHint(hint);
    }

    public void setPasswordVisibilityToggleEnabled(boolean enabled) {
        mBinding.textInputLayout.setPasswordVisibilityToggleEnabled(enabled);
    }

    public void setPasswordVisibilityToggleDrawable(int id) {
        mBinding.textInputLayout.setPasswordVisibilityToggleDrawable(id);
    }
}
