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

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.model.api.ProfileModel;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.module.analytics.SegmentFactory;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.EmailUtil;
import org.edx.mobile.util.Config;
import org.edx.mobile.util.PropertyUtil;
import org.edx.mobile.view.dialog.IDialogCallback;
import org.edx.mobile.view.dialog.NetworkCheckDialogFragment;


public class NavigationFragment extends Fragment {

    private static final String TAG = "NavigationFragment";

    private PrefManager pref;
    private final Logger logger = new Logger(getClass().getName());
    private PrefManager socialPref;
    private NetworkCheckDialogFragment newFragment;

    private UiLifecycleHelper uiLifecycleHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiLifecycleHelper = new UiLifecycleHelper(getActivity(), callback);
        uiLifecycleHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Context context = getActivity().getBaseContext();

        socialPref = new PrefManager(context, PrefManager.Pref.FEATURES);

        pref = new PrefManager(context, PrefManager.Pref.LOGIN);

        View layout = inflater.inflate(R.layout.drawer_navigation, null);

        TextView name_tv = (TextView) layout.findViewById(R.id.name_tv);
        TextView email_tv = (TextView) layout.findViewById(R.id.email_tv);

        TextView my_contents_tv = (TextView) layout.findViewById(R.id.my_contents);
        my_contents_tv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Activity act = getActivity();

                if(act instanceof MyCoursesListActivity){
                    //if MyCourses pressed when on MyCourse screen, close drawer
                    ((MyCoursesListActivity) act).closeDrawer();
                }else {
                    ((BaseFragmentActivity) act).closeDrawer();
                    Intent myCoursesIntent = new Intent(getActivity(), MyCoursesListActivity.class);
                    myCoursesIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getActivity().startActivity(myCoursesIntent);
                    act.finish();
                }
            }
        });

        TextView my_videos_tv = (TextView) layout.findViewById(R.id.my_assets);
        my_videos_tv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Activity act = getActivity();

                if(act instanceof MyVideosTabActivity){
                    ((MyVideosTabActivity) act).closeDrawer();

                }else {
                    ((BaseFragmentActivity) act).closeDrawer();
                    Intent myVideosIntent = new Intent(getActivity(),
                            MyVideosTabActivity.class);
                    myVideosIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getActivity().startActivity(myVideosIntent);
                    //Finish is not called because the MyCourse activity need not be deleted
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

        TextView groups_tv = (TextView) layout.findViewById(R.id.my_groups);
        groups_tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity act = getActivity();

                if (act instanceof MyGroupsListActivity) {
                    ((MyGroupsListActivity) act).closeDrawer();
                } else {
                    ((BaseFragmentActivity) act).closeDrawer();
                    Intent myGroupsIntent = new Intent(getActivity(),
                            MyGroupsListActivity.class);
                    myGroupsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    ((BaseFragmentActivity) act).startActivity(myGroupsIntent);
                }

            }
        });


        TextView settings_tv = (TextView) layout.findViewById(R.id.my_settings);
        settings_tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                Activity act = getActivity();

                if (act instanceof SettingsActivity) {
                    ((SettingsActivity) act).closeDrawer();
                } else {
                    ((BaseFragmentActivity) act).closeDrawer();
                    Intent settingsIntent = new Intent(getActivity(),
                            SettingsActivity.class);
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getActivity().startActivity(settingsIntent);
                }

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

    @Override
    public void onResume() {
        super.onResume();
        uiLifecycleHelper.onResume();

        if (getView() != null){
            TextView groups_tv = (TextView) getView().findViewById(R.id.my_groups);
            boolean allowSocialFeatures = socialPref.getBoolean(PrefManager.Key.ALLOW_SOCIAL_FEATURES, true);
            groups_tv.setVisibility(allowSocialFeatures ? View.VISIBLE : View.GONE);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiLifecycleHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiLifecycleHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiLifecycleHelper.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        uiLifecycleHelper.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiLifecycleHelper.onSaveInstanceState(outState);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public static String getNavigationFragmentTag() {
        return TAG;
    }

    private void updateWifiSwitch(View layout) {
        final PrefManager wifiPrefManager = new PrefManager(
                getActivity().getBaseContext(),PrefManager.Pref.WIFI);
        Switch wifi_switch = (Switch) layout.findViewById(R.id.wifi_setting);
        
        wifi_switch.setOnCheckedChangeListener(null);
        wifi_switch.setChecked(wifiPrefManager.getBoolean(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI,true));
        wifi_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, true);
                }else{
                    showWifiDialog();
                }
            }
        });
    }

    protected void showWifiDialog() {
        newFragment = NetworkCheckDialogFragment.newInstance(getString(R.string.wifi_dialog_title_help), getString(R.string.wifi_dialog_message_help), new IDialogCallback() {
            @Override
            public void onPositiveClicked() {
                try {
                    PrefManager wifiPrefManager = new PrefManager
                            (getActivity().getBaseContext(), PrefManager.Pref.WIFI);
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, false);
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
                    
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_ONLY_ON_WIFI, true);
                    wifiPrefManager.put(PrefManager.Key.DOWNLOAD_OFF_WIFI_SHOW_DIALOG_FLAG, true);

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
}

