package org.edx.mobile.tta.ui.custom;

import android.content.Context;
import android.content.res.TypedArray;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.LinearLayout;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TViewFormEdittextBinding;

import androidx.annotation.Nullable;

public class FormEditText extends LinearLayout {

    private TViewFormEdittextBinding mBinding;

    private boolean isMandatory;

    private int inputType = EditorInfo.TYPE_TEXT_VARIATION_NORMAL;
    public ObservableField<String> text = new ObservableField<>("");
    private String hint = "";

    public FormEditText(Context context) {
        super(context);
        init(context);
    }

    public FormEditText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FormEditText);

        inputType = typedArray.getInt(R.styleable.FormEditText_android_inputType, EditorInfo.TYPE_TEXT_VARIATION_NORMAL);
        text.set(typedArray.getString(R.styleable.FormEditText_android_text));
        hint = typedArray.getString(R.styleable.FormEditText_android_hint);

        typedArray.recycle();
        init(context);
    }

    private void init(Context context){
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.t_view_form_edittext, this, true);
        mBinding.setViewModel(this);

        setValues();
    }

    private void setValues() {
//        setInputType(inputType);
//        setText(text.get());
//        setHint(hint);
        setTextWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s != null && !s.toString().trim().equals("")){
                    setError(null);
                }
            }
        });
    }

    public void setInputType(int type){
        this.inputType = type;
        mBinding.textInputEditText.setInputType(type);
    }

    public void setText(String text){
        this.text.set(text);
    }

    public String getText(){
        return text.get();
    }

    public void setHint(String hint){
        this.hint = hint;
        mBinding.textInputLayout.setHint(hint);
    }

    public void setPasswordVisibilityToggleEnabled(boolean enabled) {
        mBinding.textInputLayout.setPasswordVisibilityToggleEnabled(enabled);
    }

    public void setPasswordVisibilityToggleDrawable(int id) {
        mBinding.textInputLayout.setPasswordVisibilityToggleDrawable(id);
    }

    public void setMandatory(boolean isMandatory){
        this.isMandatory = isMandatory;
        if (isMandatory && hint != null){
            mBinding.textInputLayout.setHint(hint + getResources().getString(R.string.asterisk_red));
        }
    }

    public void setSubLabel(String subLabel){
        if (subLabel != null){
            mBinding.subLabel.setText(subLabel);
            mBinding.subLabel.setVisibility(VISIBLE);
        }else{
            mBinding.subLabel.setText("");
            mBinding.subLabel.setVisibility(GONE);
        }

    }

    public void setVisibility(int visibility){
        mBinding.getRoot().setVisibility(visibility);
    }

    public boolean isVisible(){
        return mBinding.getRoot().isShown();
    }

    public boolean isMandatory(){
        return isMandatory;
    }

    public boolean validate(){
        return !isMandatory || (text.get() != null && !text.get().trim().equals(""));
    }

    public void setError(String msg){
        mBinding.textInputLayout.setError(msg);
    }

    public void setTextWatcher(TextWatcher watcher){
        mBinding.textInputEditText.addTextChangedListener(watcher);
    }
}
