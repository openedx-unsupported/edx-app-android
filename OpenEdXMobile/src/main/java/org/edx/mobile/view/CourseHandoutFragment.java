package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.Xml.Encoding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.event.NetworkConnectivityChangeEvent;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.module.analytics.ISegment;
import org.edx.mobile.task.GetHandoutTask;
import org.edx.mobile.util.NetworkUtil;
import org.edx.mobile.util.WebViewUtil;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import de.greenrobot.event.EventBus;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class CourseHandoutFragment extends BaseFragment {
    protected final Logger logger = new Logger(getClass().getName());

    @InjectExtra(Router.EXTRA_COURSE_DATA)
    private EnrolledCoursesResponse courseData;

    @Inject
    private ISegment segIO;

    @Inject
    private IEdxEnvironment environment;

    @InjectView(R.id.webview)
    private WebView webview;

    @InjectView(R.id.no_coursehandout_tv)
    private TextView errorTextView;

    private boolean isHandoutFetched;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        segIO.trackScreenView(courseData.getCourse().getName() + " - Handouts");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EventBus.getDefault().register(this);
        return inflater.inflate(R.layout.fragment_handout, container, false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new URLInterceptorWebViewClient(getActivity(), webview).setAllLinksAsExternal(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (!(NetworkUtil.isConnected(getActivity()))) {
            showErrorMessage(R.string.reset_no_network_message, FontAwesomeIcons.fa_wifi);
        } else {
            loadData();
        }
    }

    private void loadData() {
        GetHandoutTask task = new GetHandoutTask(getActivity(), courseData) {

            @Override
            public void onSuccess(HandoutModel result) {
                if (getActivity() == null) {
                    return;
                }

                if (result != null && (!TextUtils.isEmpty(result.handouts_html))) {
                    populateHandouts(result);
                } else {
                    CourseHandoutFragment.this.showErrorMessage(R.string.no_handouts_to_display,
                            FontAwesomeIcons.fa_exclamation_circle);
                }
                isHandoutFetched = true;
            }

            @Override
            public void onException(Exception ex) {
                super.onException(ex);

                if (getActivity() == null) {
                    return;
                }

                isHandoutFetched = false;
                CourseHandoutFragment.this.showErrorMessage(R.string.no_handouts_to_display,
                        FontAwesomeIcons.fa_exclamation_circle);
            }
        };
        task.execute();
    }

    private void populateHandouts(HandoutModel handout) {
        hideErrorMessage();

        StringBuilder buff = WebViewUtil.getIntialWebviewBuffer(getActivity(), logger);

        buff.append("<body>");
        buff.append("<div class=\"header\">");
        buff.append(handout.handouts_html);
        buff.append("</div>");
        buff.append("</body>");

        webview.clearCache(true);
        webview.loadDataWithBaseURL(environment.getConfig().getApiHostURL(), buff.toString(),
                "text/html", Encoding.UTF_8.toString(), null);

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NetworkConnectivityChangeEvent event) {
        if (!isHandoutFetched) {
            if (NetworkUtil.isConnected(getContext())) {
                hideErrorMessage();
                loadData();
            } else {
                showErrorMessage(R.string.reset_no_network_message, FontAwesomeIcons.fa_wifi);
            }
        }
    }

    /**
     * Shows the error message with and optional top icon, if the web page failed to load
     *
     * @param errorMsg  The error message to show
     * @param errorIcon The error icon to show with the error message
     */
    private void showErrorMessage(@StringRes int errorMsg, @NonNull Icon errorIcon) {
        webview.setVisibility(View.GONE);
        Context context = getContext();
        errorTextView.setVisibility(View.VISIBLE);
        errorTextView.setText(errorMsg);
        errorTextView.setCompoundDrawablesWithIntrinsicBounds(null,
                new IconDrawable(context, errorIcon)
                        .sizeRes(context, R.dimen.content_unavailable_error_icon_size)
                        .colorRes(context, R.color.edx_brand_gray_back),
                null, null
        );
    }

    private void hideErrorMessage() {
        webview.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
