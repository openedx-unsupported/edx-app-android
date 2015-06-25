package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.facebook.Settings;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LikeView;

import org.apache.http.protocol.HTTP;
import org.edx.mobile.R;
import org.edx.mobile.base.CourseDetailBaseFragment;
import org.edx.mobile.loader.AsyncTaskResult;
import org.edx.mobile.loader.FriendsInCourseLoader;
import org.edx.mobile.model.api.AnnouncementsModel;
import org.edx.mobile.model.api.CourseEntry;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.facebook.FacebookSessionUtil;
import org.edx.mobile.module.facebook.IUiLifecycleHelper;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.social.SocialMember;
import org.edx.mobile.social.facebook.FacebookProvider;
import org.edx.mobile.task.GetAnnouncementTask;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.SocialUtils;
import org.edx.mobile.view.custom.CourseImageHeader;
import org.edx.mobile.view.custom.EdxWebView;
import org.edx.mobile.view.custom.SocialAffirmView;
import org.edx.mobile.view.custom.SocialFacePileView;
import org.edx.mobile.view.custom.SocialShareView;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;
import org.edx.mobile.view.dialog.InstallFacebookDialog;

import java.util.ArrayList;
import java.util.List;

public class CourseCombinedInfoFragment extends CourseDetailBaseFragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<AsyncTaskResult<List<SocialMember>>> {

    static final String TAG = CourseCombinedInfoFragment.class.getCanonicalName();

    private final int LOADER_ID = 0x416BED;

    private CourseImageHeader headerImageView;
    private TextView courseTextName;
    private TextView courseTextDetails;
    private EdxWebView announcementWebView;
    private LinearLayout facePileContainer;
    private SocialFacePileView facePileView;
    private LayoutInflater inflater;
    private View certificateContainer;
    private TextView groupLauncher;
    private View notificationSettingRow;
    private Switch notificationSwitch;

    private EnrolledCoursesResponse courseData;
    private List<AnnouncementsModel> savedAnnouncements;
    private SocialAffirmView likeButton;
    private SocialShareView shareButton;
    private IUiLifecycleHelper uiHelper;
    private PrefManager featuresPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        logger.debug("created: " + getClass().getName());

        featuresPref = new PrefManager(getActivity(), PrefManager.Pref.FEATURES);

        Settings.sdkInitialize(getActivity());

        uiHelper = IUiLifecycleHelper.Factory.getInstance(getActivity(), null);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_course_combined_info, container, false);

        courseTextName = (TextView) view.findViewById(R.id.course_detail_name);
        courseTextDetails = (TextView) view.findViewById(R.id.course_detail_extras);
        certificateContainer = view.findViewById(R.id.combined_course_certificate_container);
        likeButton = (SocialAffirmView) view.findViewById(R.id.course_affirm_btn);

        headerImageView = (CourseImageHeader) view.findViewById(R.id.header_image_view);

        //Register clicks with the OnClickListener interface
        shareButton = (SocialShareView) view.findViewById(R.id.combined_course_social_share);
        shareButton.setOnClickListener(this);

        TextView handoutText = (TextView) view.findViewById(R.id.combined_course_handout_text);
        View handoutArrow =  view.findViewById(R.id.next_arrow);
        if (environment.getConfig().isNewCourseNavigationEnabled() ) {
            handoutText.setVisibility(View.GONE);
            handoutArrow.setVisibility(View.GONE);
        } else {
            handoutText.setOnClickListener(this);
        }

        TextView certificateButton = (TextView) view.findViewById(R.id.view_cert_button);
        certificateButton.setOnClickListener(this);

        facePileContainer = (LinearLayout) view.findViewById(R.id.social_face_pile_container);
        facePileContainer.setOnClickListener(this);

        facePileView = (SocialFacePileView) facePileContainer.findViewById(R.id.combined_course_facepile);

        groupLauncher = (TextView) view.findViewById(R.id.combined_course_social_group);
        groupLauncher.setOnClickListener(this);

        announcementWebView = (EdxWebView) view.findViewById(R.id.announcement_webview);
        URLInterceptorWebViewClient client = new URLInterceptorWebViewClient(announcementWebView) {

            @Override
            public void onOpenExternalURL(String url) {
                new BrowserUtil().open(getActivity(), url);
            }
        };
        // treat every link as external link in this view, so that all links will open in external browser
        client.setAllLinksAsExternal(true);

        notificationSettingRow = view.findViewById(R.id.notificaton_setting_row);
        notificationSwitch =  (Switch) view.findViewById(R.id.notification_switch);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {

            try {
                savedAnnouncements = savedInstanceState.getParcelableArrayList(Router.EXTRA_ANNOUNCEMENTS);
            } catch (Exception ex) {
                logger.error(ex);
            }

        }

        try {
            final Bundle bundle = getArguments();
            courseData = (EnrolledCoursesResponse) bundle.getSerializable(Router.EXTRA_ENROLLMENT);
            FacebookProvider fbProvider = new FacebookProvider();

            if(courseData != null) {

                //Create the inflater used to create the announcement list
                inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                if (savedAnnouncements == null) {
                    loadAnnouncementData(courseData);
                } else {
                    populateAnnouncements(savedAnnouncements);
                }

                courseTextName.setText(courseData.getCourse().getName());
                CourseEntry course = courseData.getCourse();
                courseTextDetails.setText( course.getDescription(this.getActivity()));

                String url = courseData.getCourse().getCourse_url();

                SocialUtils.SocialType socialType = SocialUtils.SocialType.NONE;
                if (fbProvider.isLoggedIn()){
                    socialType = SocialUtils.SocialType.FACEBOOK;
                }

                if (url != null) {
                    likeButton.setSocialAffirmType(socialType, url);
                }

                shareButton.setSocialShareType(socialType);

                String headerImageUrl = courseData.getCourse().getCourse_image(environment.getConfig());
                headerImageView.setImageUrl(headerImageUrl, environment.getImageCacheManager().getImageLoader() );

                updateInteractiveVisibility();

            }
            showSocialEnabled(fbProvider.isLoggedIn());

            if ( environment.getConfig().isNotificationEnabled()
                    && courseData != null && courseData.getCourse() != null){
                notificationSettingRow.setVisibility(View.VISIBLE);
                final String courseId = courseData.getCourse().getId();
                final String subscriptionId = courseData.getCourse().getSubscription_id();
                boolean isSubscribed = environment.getNotificationDelegate().isSubscribedByCourseId(courseId);
                notificationSwitch.setChecked(isSubscribed);
                notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                        environment.getNotificationDelegate().changeNotificationSetting(
                                courseId, subscriptionId, isChecked);
                    }
                });
            } else {
                notificationSwitch.setOnCheckedChangeListener(null);
                notificationSettingRow.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            logger.error(ex);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        LikeView.handleOnActivityResult(getActivity(), requestCode, resultCode, data);
        //
        uiHelper.onActivityResult(requestCode, resultCode, data, null);

    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (savedAnnouncements != null) {
            outState.putParcelableArrayList(Router.EXTRA_ANNOUNCEMENTS, new ArrayList<Parcelable>(savedAnnouncements));
        }
        uiHelper.onSaveInstanceState(outState);

    }

    private void loadAnnouncementData(EnrolledCoursesResponse enrollment) {
        GetAnnouncementTask task = new GetAnnouncementTask(getActivity(), enrollment) {

            @Override
            public void onException(Exception ex) {
                showEmptyAnnouncementMessage();
            }

            @Override
            public void onSuccess(List<AnnouncementsModel> announcementsList) {
                try {

                    savedAnnouncements = announcementsList;
                    populateAnnouncements(savedAnnouncements);

                } catch (Exception ex) {
                    logger.error(ex);
                    showEmptyAnnouncementMessage();
                }
            }
        };
        ProgressBar progressBar = (ProgressBar) getView().findViewById(R.id.api_spinner);
        task.setProgressDialog(progressBar);
        task.execute();

    }

    private void showSocialEnabled(boolean enabled){

        View view = getView();

        if (view != null){
            boolean allowSocialFeatures = featuresPref.getBoolean(PrefManager.Key.ALLOW_SOCIAL_FEATURES, true);

            View loggedInLayout = view.findViewById(R.id.social_layout);
            View groupLinkView = view.findViewById(R.id.combined_course_social_group_container);

            if (!allowSocialFeatures) {
                enabled = false;
                groupLinkView.setVisibility(View.GONE);
            } else {
                groupLinkView.setVisibility(View.VISIBLE);
            }
            loggedInLayout.setVisibility(enabled ? View.VISIBLE : View.GONE);

            if (enabled) {
                if (courseData.getCourse().getMembers_list() == null) {
                    fetchCourseMembers();
                } else {
                    populateFacePile();
                }
            }

        }

    }

    private void fetchCourseMembers(){

        Bundle args = new Bundle();

        args.putString(FriendsInCourseLoader.TAG_COURSE_ID, courseData.getCourse().getId());
        args.putString(FriendsInCourseLoader.TAG_COURSE_OAUTH, FacebookSessionUtil.getAccessToken());

        getLoaderManager().restartLoader(LOADER_ID, args, this);

    }

    private void populateFacePile(){
        List<SocialMember> courseFriends = courseData.getCourse().getMembers_list();

        facePileView.clearAvatars();
        if (courseFriends != null && courseFriends.size() > 0) {
            facePileView.setMemberList(courseFriends);
            facePileContainer.setVisibility(View.VISIBLE);
        } else {
            facePileContainer.setVisibility(View.GONE);
        }

    }

    private void populateAnnouncements(List<AnnouncementsModel> announcementsList) {
        if(announcementsList !=null && announcementsList.size()>0) {
            hideEmptyAnnouncementMessage();

            final String divider = "<div style='height:2px; width:100%; background-color: #E2E3E5'></div>";

            StringBuffer buff = new StringBuffer();

            // add meta viewport
            buff.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");

            for (AnnouncementsModel m : announcementsList) {
                String entry = String.format("%s <br/><br/> %s <br/> %s <br/><br/>", m.getDate(), divider, m.getContent());
                buff.append(entry);
            }

            announcementWebView.loadDataWithBaseURL(environment.getConfig().getApiHostURL(), buff.toString(), "text/html", HTTP.UTF_8, null);
        } else {
            showEmptyAnnouncementMessage();
        }
    }

    private void updateInteractiveVisibility() {

        if (certificateContainer != null) {
            certificateContainer.setVisibility((courseData != null && courseData.isCertificateEarned()) ? View.VISIBLE : View.GONE);
        }

        if (groupLauncher != null) {
            groupLauncher.setVisibility((courseData != null && courseData.getCourse().isGroupAvailable(SocialUtils.SocialType.FACEBOOK)) ? View.VISIBLE : View.GONE);
        }

    }

    public void showEmptyAnnouncementMessage(){
        try{
            if(getView()!=null){
                getView().findViewById(R.id.no_announcement_tv).setVisibility(View.VISIBLE);
            }
        }catch(Exception e){
            logger.error(e);
        }

    }

    private void hideEmptyAnnouncementMessage(){
        try{
            if(getView()!=null){
                getView().findViewById(R.id.no_announcement_tv).setVisibility(View.GONE);
            }
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.combined_course_handout_text:

                if (courseData != null) {
                    environment.getRouter().showHandouts(getActivity(), courseData);
                }

                break;
            case R.id.view_cert_button:
                if (courseData != null) {
                    Intent certificateIntent = new Intent(getActivity(),
                            CertificateActivity.class);
                    certificateIntent.putExtra(CertificateFragment.ENROLLMENT, courseData);
                    startActivity(certificateIntent);
                }
                break;
            case R.id.combined_course_social_share:
                FacebookProvider fbProvider = new FacebookProvider();
                FacebookDialog dialog = (FacebookDialog) fbProvider.shareCourse(getActivity(), courseData.getCourse());
                if (dialog != null) {

                    try{

                        environment.getSegment().courseShared(courseData.getCourse().getId(), SocialUtils.Values.FACEBOOK);

                    }catch(Exception e){
                        logger.error(e);
                    }

                    uiHelper.trackPendingDialogCall(dialog.present());
                } else {
                    new InstallFacebookDialog().show(getFragmentManager(), null);
                }
                break;
            case R.id.social_face_pile_container:
                if (courseData != null) {
                    Intent friendsInGroupIntent = new Intent(getActivity(),
                            FriendsInCourseActivity.class);
                    friendsInGroupIntent.putExtra(FriendsInCourseActivity.EXTRA_COURSE, courseData.getCourse());
                    startActivity(friendsInGroupIntent);
                }
                break;
            case R.id.combined_course_social_group:

                try{
                    environment.getSegment().courseGroupAccessed(courseData.getCourse().getId());
                }catch(Exception e){
                    logger.error(e);
                }

                Intent groupLaunchIntent =  SocialUtils.makeGroupLaunchIntent(getActivity(), String.valueOf(courseData.getCourse().getCourseGroup(SocialUtils.SocialType.FACEBOOK)), SocialUtils.SocialType.FACEBOOK);
                startActivity(groupLaunchIntent);
                break;

        }

    }

    @Override
    public Loader<AsyncTaskResult<List<SocialMember>>> onCreateLoader(int i, Bundle bundle) {
        return new FriendsInCourseLoader(getActivity(), bundle, environment);
    }

    @Override
    public void onLoadFinished(Loader<AsyncTaskResult<List<SocialMember>>> objectLoader, AsyncTaskResult<List<SocialMember>> result) {

        if (result.getResult() != null) {
            courseData.getCourse().setMembers_list(result.getResult());

            populateFacePile();
        } else {
            //TODO Handle error
            populateFacePile();
        }


    }

    @Override
    public void onLoaderReset(Loader<AsyncTaskResult<List<SocialMember>>> objectLoader) {
        facePileView.clearAvatars();
    }

}