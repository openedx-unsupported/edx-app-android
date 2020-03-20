package org.edx.mobile.view.dialog;

import org.edx.mobile.R;
import androidx.fragment.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class NetworkCheckDialogFragment extends DialogFragment {

    private static final String TAG = NetworkCheckDialogFragment.class.getSimpleName();

    public static final String DIALOG_TITLE = TAG + "_title";
    public static final String DIALOG_MESSAGE = TAG + "_dialog_msg_1";
    public static final String DIALOG_POSITIVE_BUTTON = TAG + "_positive_label";
    public static final String DIALOG_NEGATIVE_BUTTON = TAG + "_negative_label";

    private IDialogCallback callback;

    //TODO don't set callback here
    public static NetworkCheckDialogFragment newInstance(String title, String message, IDialogCallback callback){

        NetworkCheckDialogFragment frag = new NetworkCheckDialogFragment();
        frag.callback = callback;

        Bundle args = new Bundle();

        args.putString(DIALOG_TITLE, title);
        args.putString(DIALOG_MESSAGE, message);
        frag.setArguments(args);
        return frag;
    }

    public static NetworkCheckDialogFragment newInstance(String title, String message, String positiveLabel, String negativeLabel, IDialogCallback callback){

        NetworkCheckDialogFragment frag = new NetworkCheckDialogFragment();
        frag.callback = callback;

        Bundle args = new Bundle();

        args.putString(DIALOG_TITLE, title);
        args.putString(DIALOG_MESSAGE, message);
        args.putString(DIALOG_POSITIVE_BUTTON, positiveLabel);
        args.putString(DIALOG_NEGATIVE_BUTTON, negativeLabel);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_wifi_confirm,
                container, false);
        TextView title_tv = (TextView) v.findViewById(R.id.tv_dialog_title);
        TextView dialog_tv_1 = (TextView) v
                .findViewById(R.id.tv_dialog_message1);

        title_tv.setText(getArguments().getString(DIALOG_TITLE));
        dialog_tv_1.setText(getArguments().getString(DIALOG_MESSAGE));

        Button positiveButton = (Button) v.findViewById(R.id.positiveButton);
        positiveButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (callback != null) {
                    callback.onPositiveClicked();
                }
                dismiss();
            }
        });

        if(getArguments().containsKey(DIALOG_POSITIVE_BUTTON))
            positiveButton.setText(getArguments().getString(DIALOG_POSITIVE_BUTTON));

        Button negativeButton = (Button) v.findViewById(R.id.negativeButton);
        negativeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (callback != null) {
                    callback.onNegativeClicked();
                }
                dismiss();
            }
        });

        if(getArguments().containsKey(DIALOG_NEGATIVE_BUTTON))
            negativeButton.setText(getArguments().getString(DIALOG_NEGATIVE_BUTTON));


        return v;
    }
}
