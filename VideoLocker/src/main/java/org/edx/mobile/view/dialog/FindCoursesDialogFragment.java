package org.edx.mobile.view.dialog;

import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.view.custom.ETextView;

import roboguice.fragment.RoboDialogFragment;

public class FindCoursesDialogFragment extends RoboDialogFragment {

    @Inject
    IEdxEnvironment environment;

    public FindCoursesDialogFragment() {
    }   
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_find_courses_dialog,
                container, false);
            String find_courses_text = getString(R.string.find_courses_dialog_text_2);
            ETextView tv_find_courses_2 = (ETextView)v.findViewById(R.id.find_courses_dialog_tv2);
            tv_find_courses_2.setText(Html.fromHtml(find_courses_text));

        // Watch for button clicks.
        Button close_button = (Button) v.findViewById(R.id.positiveButton);
        close_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //Check if the dialog is not removing(dismissing)
                // or is visible before dismissing the dialog
                if(!isRemoving() && isVisible())
                    dismiss();
            }
        });

        // Watch for button clicks.
        Button find_courses_button = (Button) v.findViewById(R.id.open_edx_in_browser_btn);
        find_courses_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //Check if the dialog is not removing(dismissing)
                // or is visible before dismissing the dialog
                if(!isRemoving() && isVisible()){
                    String url = environment.getConfig().getEnrollmentConfig().getExternalCourseSearchUrl();
                    new BrowserUtil().open(getActivity(), url);
                }
            }
        });

        return v;
    }
}