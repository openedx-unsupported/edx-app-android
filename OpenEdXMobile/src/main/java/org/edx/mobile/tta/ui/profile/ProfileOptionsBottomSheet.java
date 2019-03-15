package org.edx.mobile.tta.ui.profile;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.LinearLayout;

import org.edx.mobile.R;

public class ProfileOptionsBottomSheet extends BottomSheetDialogFragment {
    public static final String TAG = ProfileOptionsBottomSheet.class.getCanonicalName();

    private View.OnClickListener listener;

    private LinearLayout profileOptionsLayout, signOutOptionsLayout;

    public static ProfileOptionsBottomSheet newInstance(View.OnClickListener listener){
        ProfileOptionsBottomSheet fragment = new ProfileOptionsBottomSheet();
        fragment.listener = listener;
        return fragment;
    }

    //Bottom Sheet Callback
    private BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }

        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        //Get the content View
        View contentView = View.inflate(getContext(), R.layout.t_fragment_profile_bottomsheet, null);

        profileOptionsLayout = contentView.findViewById(R.id.profile_options_layout);
        signOutOptionsLayout = contentView.findViewById(R.id.sign_out_options_layout);

        contentView.findViewById(R.id.ivClose).setOnClickListener(listener);
        contentView.findViewById(R.id.edit_profile_layout).setOnClickListener(v -> {
            if (profileOptionsLayout.getVisibility() == View.VISIBLE) {
                if (listener != null){
                    listener.onClick(v);
                }
            }
        });
        contentView.findViewById(R.id.change_password_layout).setOnClickListener(v -> {
            if (profileOptionsLayout.getVisibility() == View.VISIBLE) {
                if (listener != null){
                    listener.onClick(v);
                }
            }
        });
        contentView.findViewById(R.id.contact_us_layout).setOnClickListener(v -> {
            if (profileOptionsLayout.getVisibility() == View.VISIBLE) {
                if (listener != null){
                    listener.onClick(v);
                }
            }
        });
        contentView.findViewById(R.id.sign_out_layout).setOnClickListener(v -> {
            if (profileOptionsLayout.getVisibility() == View.VISIBLE) {
                profileOptionsLayout.setVisibility(View.INVISIBLE);
                signOutOptionsLayout.setVisibility(View.VISIBLE);
            }
        });

        contentView.findViewById(R.id.btn_yes).setOnClickListener(v -> {
            if (signOutOptionsLayout.getVisibility() == View.VISIBLE) {
                if (listener != null){
                    listener.onClick(v);
                }
            }
        });
        contentView.findViewById(R.id.btn_no).setOnClickListener(v -> {
            if (signOutOptionsLayout.getVisibility() == View.VISIBLE) {
                signOutOptionsLayout.setVisibility(View.INVISIBLE);
                profileOptionsLayout.setVisibility(View.VISIBLE);
            }
        });

        dialog.setContentView(contentView);

        //Set the coordinator layout behavior
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        CoordinatorLayout.Behavior behavior = params.getBehavior();

        //Set callback
        if (behavior != null && behavior instanceof BottomSheetBehavior) {
            ((BottomSheetBehavior) behavior).setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (listener != null){
            listener.onClick(null);
        }
    }

}
