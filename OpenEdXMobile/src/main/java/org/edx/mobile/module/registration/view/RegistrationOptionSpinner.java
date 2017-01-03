package org.edx.mobile.module.registration.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.edx.mobile.R;
import org.edx.mobile.module.registration.model.RegistrationOption;

import java.util.List;

public class RegistrationOptionSpinner extends Spinner {

    private ArrayAdapter<RegistrationOption> adapter;

    public RegistrationOptionSpinner(Context context) {
        super(context);
    }

    public RegistrationOptionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RegistrationOptionSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean hasValue(@Nullable String value){
        if (adapter != null) {
            return (getAdapterPosition(value) >= 0);
        }
        return false;
    }

    public void select(@Nullable String value){
        if (adapter != null && value != null) {
            int pos = getAdapterPosition(value);
            if (pos >= 0) {
                setSelection(pos);
            }
        }
    }

    public @Nullable String getSelectedItemValue() {
        String value = null;
        RegistrationOption selected = (RegistrationOption)getSelectedItem();
        if (selected != null) {
            value = selected.getValue();
        }
        return value;
    }

    private int getAdapterPosition(@Nullable String input) {
        int posiiton = -1;
        if (input != null && adapter != null) {
            for(int i=0 ; i<adapter.getCount() ; i++){
                RegistrationOption item = adapter.getItem(i);
                if (item != null && input.equals(item.toString())) {
                    posiiton = i;
                    break;
                }
            }
        }
        return posiiton;
    }

    public void setItems(@NonNull List<RegistrationOption> options, @Nullable RegistrationOption defaultOption) {
        adapter = new ArrayAdapter<>(getContext(), R.layout.registration_spinner_item, options);
        setAdapter(adapter);
        if (defaultOption != null) {
            select(defaultOption.toString());
        }
    }
}
