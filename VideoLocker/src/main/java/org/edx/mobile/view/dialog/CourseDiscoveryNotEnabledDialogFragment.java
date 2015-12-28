package org.edx.mobile.view.dialog;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.ResourceUtil;

import roboguice.fragment.RoboDialogFragment;

public class CourseDiscoveryNotEnabledDialogFragment extends RoboDialogFragment {

    @Inject
    IEdxEnvironment environment;

    public static void show(FragmentActivity activity) {
        final CourseDiscoveryNotEnabledDialogFragment findCoursesFragment = new CourseDiscoveryNotEnabledDialogFragment();
        findCoursesFragment.setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        findCoursesFragment.show(activity.getSupportFragmentManager(), "dialog-find-courses");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.course_discovery_not_enabled_dialog,
                container, false);
        String find_courses_text = getString(R.string.find_courses_dialog_text_2);
        TextView tv_find_courses_2 = (TextView) v.findViewById(R.id.find_courses_dialog_tv2);
        tv_find_courses_2.setText(Html.fromHtml(find_courses_text));

        // Watch for button clicks.
        Button close_button = (Button) v.findViewById(R.id.positiveButton);
        close_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //Check if the dialog is not removing(dismissing)
                // or is visible before dismissing the dialog
                if (!isRemoving() && isVisible())
                    dismiss();
            }
        });

        // Watch for button clicks.
        Button find_courses_button = (Button) v.findViewById(R.id.open_edx_in_browser_btn);
        CharSequence promptText = ResourceUtil.getFormattedString(getResources(), R.string.open_destination_btn_text, "platform_destination", environment.getConfig().getPlatformDestinationName());
        find_courses_button.setText(promptText);
        find_courses_button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //Check if the dialog is not removing(dismissing)
                // or is visible before dismissing the dialog
                if (!isRemoving() && isVisible()) {
                    String url = environment.getConfig().getCourseDiscoveryConfig().getExternalCourseSearchUrl();
                    BrowserUtil.open(getActivity(), url);
                }
            }
        });

        return v;
    }
}
