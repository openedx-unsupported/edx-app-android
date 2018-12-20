package org.edx.mobile.tta.ui.login.view_model;

import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;

public class RegisterViewModel extends BaseViewModel {

    public ObservableField<String> cellphone = new ObservableField<>("");
    public ObservableField<String> password = new ObservableField<>("");
    public ObservableField<String> confirmPassword = new ObservableField<>("");
    public ObservableBoolean cellValid = new ObservableBoolean();
    public ObservableBoolean passValid = new ObservableBoolean();
    public ObservableBoolean confirmPassValid = new ObservableBoolean();
    public ObservableInt passDrawable = new ObservableInt();
    public ObservableInt confirmPassDrawable = new ObservableInt();

    private boolean passVisible = false;
    private boolean confirmPassVisible = false;

    public TextWatcher numWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            cellphone.set(s.toString());
            String numString = s.toString().trim();
            if (numString.length() == 10 && numString.matches("[0-9]+")){
                cellValid.set(true);
            } else {
                cellValid.set(false);
            }
        }
    };

    public TextWatcher passWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            password.set(s.toString());
            setPassDrawable();
            String passString = s.toString();
            if (passString.length() >= 3 && passString.equals(confirmPassword.get())){
                passValid.set(true);
            } else {
                passValid.set(false);
            }
        }
    };

    public TextWatcher ConfirmPassWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            confirmPassword.set(s.toString());
            setConfirmPassDrawable();
            String confirmPassString = s.toString();
            if (confirmPassString.length() >= 3 && confirmPassString.equals(password.get())){
                confirmPassValid.set(true);
            } else {
                confirmPassValid.set(false);
            }
        }
    };

    public RegisterViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
    }

    public void register(){

    }

    public void signIn(){

    }

    private void setPassDrawable(){
        if (password.get().length() > 0){
            if (passVisible){
                passDrawable.set(R.drawable.ic_visibility_green_24dp);
            } else {
                passDrawable.set(R.drawable.ic_visibility_gray_4_24dp);
            }
        } else {
            passDrawable.set(0);
        }
    }

    private void setConfirmPassDrawable(){
        if (confirmPassword.get().length() > 0){
            if (confirmPassVisible){
                confirmPassDrawable.set(R.drawable.ic_visibility_green_24dp);
            } else {
                confirmPassDrawable.set(R.drawable.ic_visibility_gray_4_24dp);
            }
        } else {
            confirmPassDrawable.set(0);
        }
    }
}
