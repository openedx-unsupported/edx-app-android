package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.social.facebook.FacebookProvider;
import org.edx.mobile.util.BrowserUtil;
import org.edx.mobile.util.SocialUtils;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;
import org.edx.mobile.view.dialog.InstallFacebookDialog;

import roboguice.fragment.RoboFragment;

public class CertificateFragment extends RoboFragment {

    private final Logger logger = new Logger(CertificateFragment.class);

    public WebView webview;

    private ProgressBar progressBar;
    private LinearLayout facebookShare;

    static public String TAG = CertificateFragment.class.getCanonicalName();
    static public String ENROLLMENT = TAG + ".enrollment";

    private EnrolledCoursesResponse courseData;

    private UiLifecycleHelper uiHelper;

    @Inject
    private ISegment segIO;

    private enum CertificateState {
        CERT_LOADING,
        CERT_READY,
        NO_CERT
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(getActivity(), null);
        uiHelper.onCreate(savedInstanceState);


        try{
            segIO.screenViewsTracking("Certificate");
        }catch(Exception e){
            logger.error(e);
        }

    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_certificate, container,
                false);

        progressBar = (ProgressBar) view.findViewById(R.id.api_spinner);

        webview = (WebView) view.findViewById(R.id.webview);
        new URLInterceptorWebViewClient(webview) {

            @Override
            public void onOpenExternalURL(String url) {
                new BrowserUtil().open(getActivity(), url);
            }
        };

        facebookShare = (LinearLayout) view.findViewById(R.id.share_certificate);
        facebookShare.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try{
                    segIO.certificateShared(courseData.getCourse().getId(), SocialUtils.Values.FACEBOOK);
                }catch(Exception e){
                    logger.error(e);
                }

                FacebookProvider fbProvider = new FacebookProvider();
                if (fbProvider.isLoggedIn()){
                    FacebookDialog dialog = (FacebookDialog) fbProvider.shareCertificate(getActivity(), courseData.getCourse());
                    if (dialog != null) {
                        uiHelper.trackPendingDialogCall(dialog.present());
                    } else {
                        new InstallFacebookDialog().show(getFragmentManager(), null);
                    }
                }

            }
        });

        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {

            final Bundle bundle = getArguments();
            courseData = (EnrolledCoursesResponse) bundle
                    .getSerializable(ENROLLMENT);
            webview.loadUrl(courseData.getCertificateURL());

        } catch (Exception ex) {
            logger.error(ex);
        }
        FacebookProvider fbProvider = new FacebookProvider();

        SocialUtils.SocialType type = SocialUtils.SocialType.NONE;

        if (fbProvider.isLoggedIn()) {
            type = SocialUtils.SocialType.FACEBOOK;
        }
        changeSocialState(type);

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
        uiHelper.onSaveInstanceState(outState);
    }

    private void changeSocialState(SocialUtils.SocialType state) {

        facebookShare.setVisibility(View.GONE);
        switch (state){

            case FACEBOOK:
                facebookShare.setVisibility(View.VISIBLE);

        }

    }

    private void changeDisplayState(CertificateState state){

        switch (state){

            case CERT_READY:
                break;
            case NO_CERT:
                break;
            case CERT_LOADING:
                break;

        }

    }


}
