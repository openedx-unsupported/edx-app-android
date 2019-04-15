package org.edx.mobile.tta.ui.custom;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TViewFormMultiSpinnerBinding;
import org.edx.mobile.module.registration.model.RegistrationOption;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class FormMultiSpinner extends LinearLayout {
    private TViewFormMultiSpinnerBinding mBinding;

    private boolean isMandatory;

    private List<RegistrationOption> options;

    private List<RegistrationOption> selectedOptions;

    private String label;

    public FormMultiSpinner(Context context) {
        super(context);
        init(context);
    }

    public FormMultiSpinner(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.t_view_form_multi_spinner, this, true);
        mBinding.getRoot().setOnClickListener(v -> {
            mBinding.spinner.performClick();
        });
        selectedOptions = new ArrayList<>();
        mBinding.spinner.setListener(new MultiSelectionSpinner.OnMultipleItemsSelectedListener() {
            @Override
            public void selectedIndices(List<Integer> indices) {

            }

            @Override
            public void selectedStrings(List<String> strings) {
                if (selectedOptions != null) {
                    selectedOptions.clear();
                } else {
                    selectedOptions = new ArrayList<>();
                }
                if (strings == null || strings.isEmpty()){
                    mBinding.spinnerLabel.setVisibility(GONE);
                    mBinding.etRegion.setTextColor(ContextCompat.getColor(context, R.color.gray_1));
                    mBinding.etRegion.setText(label);
                } else {
                    setError(null);
                    mBinding.spinnerLabel.setVisibility(VISIBLE);
                    mBinding.etRegion.setTextColor(ContextCompat.getColor(context, R.color.gray_4));
                    mBinding.etRegion.setText(mBinding.spinner.getSelectedItemsAsString());

                    for (String s: strings){
                        RegistrationOption option = new RegistrationOption(s, s);
                        selectedOptions.add(options.get(options.indexOf(option)));
                    }
                }
            }
        });
    }

    public void setLabel(String label) {
        this.label = label;
        mBinding.spinnerLabel.setText(label);
        mBinding.spinner.setTitle(label);
    }

    public void setItems(List<RegistrationOption> options, List<RegistrationOption> selectedOptions){
        this.options = options;
        this.selectedOptions = selectedOptions;

        List<String> items = new ArrayList<>();
        for (RegistrationOption option: this.options){
            items.add(option.toString());
        }
        mBinding.spinner.setItems(items);

        if (this.selectedOptions != null && !this.selectedOptions.isEmpty()){
            List<String> selected = new ArrayList<>();
            for (RegistrationOption option: this.selectedOptions){
                selected.add(option.toString());
            }
            mBinding.spinner.setSelection(selected);

            setError(null);
            mBinding.spinnerLabel.setVisibility(VISIBLE);
            mBinding.etRegion.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_4));
            mBinding.etRegion.setText(mBinding.spinner.getSelectedItemsAsString());
        } else {
            mBinding.spinnerLabel.setVisibility(GONE);
            mBinding.etRegion.setTextColor(ContextCompat.getColor(getContext(), R.color.gray_1));
            mBinding.etRegion.setText(label);
        }
    }

    public void setMandatory(boolean isMandatory) {
        this.isMandatory = isMandatory;
        if (isMandatory && label != null) {
            mBinding.spinnerLabel.append(getResources().getString(R.string.asterisk_red));
        }
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public boolean validate() {
        return !isMandatory || (selectedOptions != null && !selectedOptions.isEmpty());
    }

    public void setError(String msg) {
        if (msg != null) {
            mBinding.spinnerError.setText(msg);
            mBinding.spinnerError.setVisibility(VISIBLE);
        } else {
            mBinding.spinnerError.setVisibility(GONE);
        }
    }

    public List<RegistrationOption> getSelectedOptions() {
        return selectedOptions;
    }
}
