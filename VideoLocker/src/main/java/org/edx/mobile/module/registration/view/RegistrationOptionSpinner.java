package org.edx.mobile.module.registration.view;

import android.content.Context;
import android.util.AttributeSet;

import org.edx.mobile.module.registration.model.RegistrationOption;
import org.edx.mobile.view.custom.CustomSelectView;

/**
 * Created by rohan on 2/16/15.
 */
public class RegistrationOptionSpinner extends CustomSelectView<RegistrationOption> {

    public RegistrationOptionSpinner(Context context) {
        super(context);
    }

    public RegistrationOptionSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RegistrationOptionSpinner(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean hasValue(String value){
        if ( value == null || items == null )
            return false;
        for ( RegistrationOption option : items ) {
            if ( value.equals(option.getValue()) )
                return true;
        }
        return false;
    }

    public void select(String value){
        if ( value == null || items == null )
            return;
        for ( RegistrationOption option : items ) {
            if ( value.equals(option.getValue()) ){
                super.select(option);
            }
        }
    }
}
