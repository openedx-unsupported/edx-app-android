package org.edx.mobile.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.EmailUtil;
import org.edx.mobile.util.PropertyUtil;
import org.edx.mobile.view.dialog.IDialogCallback;
import org.edx.mobile.view.dialog.WifiSwitchDialogFragment;

import java.util.HashMap;
import java.util.Map;

public class NavigationFragment extends Fragment {

    private PrefManager pref;
    private WifiSwitchDialogFragment newFragment;
    private final Logger logger = new Logger(getClass().getName());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Context context = getActivity().getBaseContext();
    
        pref = new PrefManager(context, PrefManager.Pref.LOGIN);

        View layout = (View) inflater.inflate(R.layout.drawer_navigation, null);

        TextView name_tv = (TextView) layout.findViewById(R.id.name_tv);
        TextView email_tv = (TextView) layout.findViewById(R.id.email_tv);

        TextView my_contents_tv = (TextView) layout.findViewById(R.id.my_contents);
        my_contents_tv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Activity act = getActivity();
                ((BaseFragmentActivity) act).closeDrawer();

                //if MyCourses pressed when on MyCourse screen, just close the drawer.
                if(!(act instanceof MyCoursesListActivity)){
                    Intent myCoursesIntent = new Intent(act, MyCoursesListActivity.class);
                    myCoursesIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    act.startActivity(myCoursesIntent);
                    act.finish();
                }
            }
        });

        View panelFindCourses = layout.findViewById(R.id.panel_option_find_courses);
        if (Config.getInstance().getEnrollment().getEnabled()) {
            panelFindCourses.setVisibility(View.VISIBLE);

            TextView my_videos_tv = (TextView) layout.findViewById(R.id.my_assets);
            my_videos_tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Activity act = getActivity();
                    ((BaseFragmentActivity) act).closeDrawer();

                    if (!(act instanceof MyVideosTabActivity)) {
                        Router.getInstance().showMyVideos(act);
                        //Finish need not be called if the current activity is MyCourseListing
                        // as on returning back from MyVideos,
                        // the student should be returned to the MyCourses screen
                        if (!(act instanceof MyCoursesListActivity)) {
                            act.finish();
                        }
                    }
                }
            });
        }
        else {
            panelFindCourses.setVisibility(View.GONE);
        }

        TextView find_courses_tv = (TextView) layout.findViewById(R.id.find_courses_tv);
        find_courses_tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity act = getActivity();
                ((BaseFragmentActivity) act).closeDrawer();

                if(!(act instanceof FindCoursesActivity)){
                    Router.getInstance().showFindCourses(act);
                    //Finish need not be called if the current activity is MyCourseListing
                    // as on returning back from FindCourses,
                    // the student should be returned to the MyCourses screen
                    if (!(act instanceof MyCoursesListActivity)) {
                        act.finish();
                    }
                }
            }
        });

        TextView my_email_tv = (TextView) layout.findViewById(R.id.my_email);
        my_email_tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String to = Config.getInstance().getFeedbackEmailAddress();
                String subject =getString(R.string.Email_subject);
                String email = "";
                EmailUtil.sendEmail(getActivity(), to, subject, email);
            }
        });

        ProfileModel profile = pref.getCurrentUserProfile();
        if(profile != null) {
            if(profile.name != null) {
                name_tv.setText(profile.name);
            }
            if(profile.email != null) {
                email_tv.setText(profile.email);
            }
        }

        updateWifiSwitch(layout);

        Button logout_btn = (Button) layout.findViewById(R.id.logout_button);
        logout_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                pref.clearAuth();
                pref.put(PrefManager.Key.TRANSCRIPT_LANGUAGE, 
                        getString(R.string.lbl_cc_cancel));
                
                Intent loginIntent = new Intent(getActivity()
                        .getBaseContext(), LoginActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent
                        .FLAG_ACTIVITY_CLEAR_TOP); 
                Intent intent = new Intent();
                intent.setAction(AppConstants.LOGOUT_CLICKED);
                getActivity().sendBroadcast(intent); 
                
                ISegment segIO = SegmentFactory.getInstance();
                segIO.trackUserLogout();
                segIO.resetIdentifyUser();
                
                startActivity(loginIntent);
            }
        });
        

        TextView version_tv = (TextView) layout.findViewById(R.id.tv_version_no);
        try{
            String versionName = PropertyUtil.getManifestVersionName(getActivity());

            if(versionName != null) {
                String envDisplayName = Config.getInstance().getEnvironmentDisplayName();
                String text = String.format("%s %s %s",
                        getString(R.string.label_version), versionName, envDisplayName);
                version_tv.setText(text);
            }
        }catch(Exception e) {
            logger.error(e);
        }

        return layout;
    }

    private void updateWifiSwitch(View layout) {
        final PrefManager wifiPrefManager = new PrefManager(
                getActivity().getBaseContext(),PrefManager.Pref.WIFI);
        Switch wifi_switch = (Switch) layout.findViewById(R.id.wifi_setting);
        
        wifi_switch.setOnCheckedChangeListener(null);
        wifi_switch.setChecked(wifiPrefManager.getBoolean(PrefManager.Key.DOWNLOAD_ON_WIFI,true));
        wifi_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ON_WIFI, true);
                }else{
                    showWifiDialog();
                }
            }
        });
    }

    protected void showWifiDialog() {
        Map<String, String> dialogMap = new HashMap<String, String>();
        dialogMap.put("title", getString(R.string.wifi_dialog_title_help));
        dialogMap.put("message_1",  getString(R.string.wifi_dialog_message_help));
        newFragment = WifiSwitchDialogFragment.newInstance(dialogMap, new IDialogCallback() {
            @Override
            public void onPositiveClicked() {
                try {
                    PrefManager wifiPrefManager = new PrefManager
                            (getActivity().getBaseContext(),PrefManager.Pref.WIFI);
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ON_WIFI, false);
                    updateWifiSwitch(getView());
                } catch(Exception ex) {
                    logger.error(ex);
                }
            }
            
            @Override
            public void onNegativeClicked() {
                try {
                    PrefManager wifiPrefManager = new PrefManager(
                            getActivity().getBaseContext(),PrefManager.Pref.WIFI);
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ON_WIFI, true);
                    
                    updateWifiSwitch(getView());
                } catch(Exception ex) {
                    logger.error(ex);
                }
            }
        });
        newFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        newFragment.show(getFragmentManager(), "dialog");
        newFragment.setCancelable(false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}

