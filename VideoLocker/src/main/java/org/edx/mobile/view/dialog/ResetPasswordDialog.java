package org.edx.mobile.view.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.ResetPasswordResponse;
import org.edx.mobile.task.ResetPasswordTask;
import org.edx.mobile.util.InputValidationUtil;
import org.edx.mobile.util.NetworkUtil;

public class ResetPasswordDialog extends DialogFragment {

    private final Logger logger = new Logger(getClass().getName());

    private EditText email_et;
    private TextView error;
    private ProgressBar progressbar;
    private RelativeLayout resetLayout;
    private ResetPasswordTask resetPasswordTask;
    private boolean isResetSuccessful = false;

    public ResetPasswordDialog() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialog, container, false);
        // Watch for button clicks.
        email_et = (EditText) v.findViewById(R.id.email_edit);
        error = (TextView) v.findViewById(R.id.dialog_error_message);
        progressbar = (ProgressBar) v.findViewById(R.id.login_spinner);
        resetLayout = (RelativeLayout) v.findViewById(R.id.reset_layout);

        Button positiveBtn = (Button) v.findViewById(R.id.positiveButton);
        positiveBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                resetLayout.setVisibility(View.VISIBLE);
                error.setVisibility(View.GONE);

                if (!NetworkUtil.isConnected(getActivity())) {
                    email_et.requestFocus();
                    // no network error
                    error.setText(getResources().getString(
                            R.string.network_not_connected_short));
                    error.setVisibility(View.VISIBLE);
                    resetLayout.setVisibility(View.GONE);
                    return;
                }

                String emailStr = email_et.getText().toString().trim();

                if (InputValidationUtil.isValidEmail(emailStr)) {
                    try {
                        resetPassword(emailStr);
                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                } else {
                    email_et.requestFocus();
                    // display error
                    error.setVisibility(View.VISIBLE);
                    resetLayout.setVisibility(View.GONE);
                }
            }
        });

        Button negativeBtn = (Button) v.findViewById(R.id.negativeButton);
        negativeBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Stop the reset password task and close this dialog
                stopResetPasswordTask();
                dismiss();
            }
        });

        return v;
    }

    /**
     * This method calls the ResetPasswordTask
     * @param email
     */
    private void resetPassword(String email) {
        email_et.setEnabled(false);
        resetPasswordTask = new ResetPasswordTask(getActivity()) {
            @Override
            public void onFinish(ResetPasswordResponse result) {
                if (result != null && result.isSuccess()) {
                    isResetSuccessful = true;
                    //Only if the app is in foreground then
                    //dismiss and call the reset successful dialog
                    if(isResumed()){
                        onResetSuccessful();
                        dismiss();
                    }
                } else {
                    isResetSuccessful = false;
                    if(result!=null){
                        onResetFailed(result);
                    }
                    email_et.setEnabled(true);
                    resetLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
                email_et.setEnabled(true);
                resetLayout.setVisibility(View.GONE);
                isResetSuccessful = false;
            }
        };
        resetPasswordTask.setProgressDialog(progressbar);
        isResetSuccessful = false;
        resetPasswordTask.execute(email);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TextView title = (TextView) getView()
                .findViewById(R.id.tv_dialog_title);
        TextView message = (TextView) getView().findViewById(
                R.id.tv_dialog_message1);
        title.setText(getResources().getString(
                R.string.confirm_dialog_title_help));
        message.setText(getResources().getString(
                R.string.confirm_dialog_message_help));

        String email_text = getArguments().getString("login_email");
        email_et.setText(email_text);
    }

    protected void onResetSuccessful() {
        // sub-classes may override this method to show further alerts
    }

    protected void onResetFailed(ResetPasswordResponse result) {
        // sub-classes may override this method to show further alerts
    }

    private void stopResetPasswordTask(){
        if(resetPasswordTask!=null){
            resetPasswordTask.cancel(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        email_et.setEnabled(true);
        resetLayout.setVisibility(View.GONE);
        //Check if the password was reset successfully when in background.
        //If reset successfully then display the reset successful dialog and dismiss this dialog
        if(isResetSuccessful){
            dismiss();
            onResetSuccessful();
        }
    }
}