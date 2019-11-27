package org.edx.mobile.module.registration.view;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ArrayAdapter;

import org.edx.mobile.R;
import org.edx.mobile.module.registration.model.RegistrationOption;

import java.util.List;

public class RegistrationOptionSpinner extends AppCompatSpinner {

    private ArrayAdapter<RegistrationOption> adapter;
    private RegistrationSelectView.OnSpinnerFocusedListener onSpinnerFocusedListener;

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

    @Nullable
    public String getSelectedItemValue() {
        String value = null;
        final RegistrationOption selected = (RegistrationOption) getSelectedItem();
        if (selected != null) {
            value = selected.getValue();
        }
        return value;
    }

    @NonNull
    public String getSelectedItemName() {
        String value = null;
        final RegistrationOption selected = (RegistrationOption) getSelectedItem();
        if (selected != null) {
            value = selected.getName();
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

    public void setOnSpinnerFocusedListener(@Nullable RegistrationSelectView.OnSpinnerFocusedListener onSpinnerFocusedListener) {
        this.onSpinnerFocusedListener = onSpinnerFocusedListener;
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        /*
        Whenever there's an AppCompatSpinner on screen, its read out by TalkBack even when its not
        in focus. This workaround ensures that AppCompatSpinner's content is read only when its
        in focus.
        There's an open StackOverflow issue on this as well:
        https://stackoverflow.com/questions/44708495/how-to-prevent-spinner-announcement-when-initialized
         */
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED
                || event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
            super.onInitializeAccessibilityEvent(event);
        }
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED ||
                event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED ||
                event.getEventType() == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
            if (onSpinnerFocusedListener != null) {
                onSpinnerFocusedListener.onSpinnerFocused();
            }
        }
    }
}
