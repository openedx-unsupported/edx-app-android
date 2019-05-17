package org.edx.mobile.tta.ui.custom;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TViewFormSpinnerBinding;
import org.edx.mobile.module.registration.model.RegistrationOption;
import org.edx.mobile.tta.ui.interfaces.OnTaItemClickListener;

import java.util.List;

import androidx.annotation.Nullable;

public class FormSpinner extends LinearLayout {

    private TViewFormSpinnerBinding mBinding;

    private ArrayAdapter<RegistrationOption> adapter;

    private boolean isMandatory;

    private List<RegistrationOption> options;

    private RegistrationOption selectedOption;

    private OnTaItemClickListener<RegistrationOption> listener;

    private String label;

    public FormSpinner(Context context) {
        super(context);
        init(context);
    }

    public FormSpinner(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.t_view_form_spinner, this, true);
        mBinding.getRoot().setOnClickListener(v -> {
            mBinding.spinner.performClick();

        });

        setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if (label != null && position == 0) {

                    mBinding.spinnerLabel.setVisibility(GONE);
                    selectedOption = null;
                    mBinding.etRegion.setTextColor(ContextCompat.getColor(context, R.color.gray_1));
                } else {
                    selectedOption = options.get(position);
                    setError(null);
                    mBinding.spinnerLabel.setVisibility(VISIBLE);
                    mBinding.etRegion.setTextColor(ContextCompat.getColor(context, R.color.gray_4));
                }

                if (listener != null) {
                    listener.onItemClick(view, selectedOption);
                }
                mBinding.etRegion.setText(item.toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                /*if (options != null && !options.isEmpty()) {
                    selectedOption = options.get(0);
                    Object item = parent.getItemAtPosition(0);
                    setError(null);
                    if (listener != null) {
                        listener.onItemClick(parent.getSelectedView(), selectedOption);
                    }
                }*/
            }
        });
    }


    public void setLabel(String label) {
        this.label = label;
        mBinding.spinnerLabel.setText(label);
    }

    public void setItems(@NonNull List<RegistrationOption> options, RegistrationOption defaultOption) {
        if (label != null) {
            options.add(0, new RegistrationOption(label, label));
        }
        this.options = options;
        this.selectedOption = defaultOption;
        adapter = new ArrayAdapter<>(getContext(), R.layout.t_view_spinner_item, options);
        adapter.setDropDownViewResource(R.layout.t_view_spinner_item);
        setAdapter(adapter);
        if (defaultOption != null) {
            select(defaultOption.toString());
        }
    }

    public void setAdapter(SpinnerAdapter adapter) {
        mBinding.spinner.setAdapter(adapter);
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener onItemSelectedListener) {
        mBinding.spinner.setOnItemSelectedListener(onItemSelectedListener);
    }

    public void setOnItemSelectedListener(OnTaItemClickListener<RegistrationOption> listener) {
        this.listener = listener;
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
        return !isMandatory || (selectedOption != null);
    }

    public RegistrationOption getSelectedOption() {
        if (selectedOption == null) {
            return new RegistrationOption("", "");
        }
        return selectedOption;
    }

    public void setError(String msg) {
        if (msg != null) {
            mBinding.spinnerError.setText(msg);
            mBinding.spinnerError.setVisibility(VISIBLE);
        } else {
            mBinding.spinnerError.setVisibility(GONE);
        }
    }

    public void notifyDataSetChanged(){
        adapter.notifyDataSetChanged();
    }

    private void select(@android.support.annotation.Nullable String value) {
        if (adapter != null && value != null) {
            int pos = getAdapterPosition(value);
            if (pos >= 0) {
                mBinding.spinner.setSelection(pos);
            }
        }
    }

    private int getAdapterPosition(@android.support.annotation.Nullable String input) {
        int posiiton = -1;
        if (input != null && !input.equals("") && adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                RegistrationOption item = adapter.getItem(i);
                if (item != null && (input.equals(item.getValue()) || input.equals(item.getName()))) {
                    posiiton = i;
                    break;
                }
            }
        }
        return posiiton;
    }

}
