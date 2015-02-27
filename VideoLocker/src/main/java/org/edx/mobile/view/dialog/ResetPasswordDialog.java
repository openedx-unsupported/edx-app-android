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

    private EditText edtEmail;
    private TextView txtError;
    private ProgressBar progressBar;
    private RelativeLayout layoutReset;
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
        edtEmail = (EditText) v.findViewById(R.id.email_edit);
        txtError = (TextView) v.findViewById(R.id.dialog_error_message);
        progressBar = (ProgressBar) v.findViewById(R.id.login_spinner);
        layoutReset = (RelativeLayout) v.findViewById(R.id.reset_layout);

        Button btnPositive = (Button) v.findViewById(R.id.positiveButton);
        btnPositive.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                layoutReset.setVisibility(View.VISIBLE);
                txtError.setVisibility(View.GONE);

                if (!NetworkUtil.isConnected(getActivity())) {
                    edtEmail.requestFocus();
                    // no network txtError
                    txtError.setText(getResources().getString(
                            R.string.network_not_connected_short));
                    txtError.setVisibility(View.VISIBLE);
                    layoutReset.setVisibility(View.GONE);
                    return;
                }

                String strEmail = edtEmail.getText().toString().trim();

                if (InputValidationUtil.isValidEmail(strEmail)) {
                    try {
                        resetPassword(strEmail);
                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                } else {
                    edtEmail.requestFocus();
                    // display txtError
                    txtError.setVisibility(View.VISIBLE);
                    layoutReset.setVisibility(View.GONE);
                }
            }
        });

        Button btnNegative = (Button) v.findViewById(R.id.negativeButton);
        btnNegative.setOnClickListener(new OnClickListener() {
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
        edtEmail.setEnabled(false);
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
                    edtEmail.setEnabled(true);
                    layoutReset.setVisibility(View.GONE);
                }
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
                edtEmail.setEnabled(true);
                layoutReset.setVisibility(View.GONE);
                isResetSuccessful = false;
            }
        };
        resetPasswordTask.setProgressDialog(progressBar);
        isResetSuccessful = false;
        resetPasswordTask.execute(email);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        TextView txtTitle = (TextView) getView()
                .findViewById(R.id.tv_dialog_title);
        TextView txtDialogMessage = (TextView) getView().findViewById(
                R.id.tv_dialog_message1);
        txtTitle.setText(getResources().getString(
                R.string.confirm_dialog_title_help));
        txtDialogMessage.setText(getResources().getString(
                R.string.confirm_dialog_message_help));

        String strEmailText = getArguments().getString("login_email");
        edtEmail.setText(strEmailText);
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
        edtEmail.setEnabled(true);
        layoutReset.setVisibility(View.GONE);
        //Check if the password was reset successfully when in background.
        //If reset successfully then display the reset successful dialog and dismiss this dialog
        if(isResetSuccessful){
            dismiss();
            onResetSuccessful();
        }
    }
}