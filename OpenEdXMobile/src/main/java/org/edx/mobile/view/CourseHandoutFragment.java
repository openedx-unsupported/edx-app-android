package org.edx.mobile.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Xml.Encoding;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentWebviewWithPaddingsBinding;
import org.edx.mobile.http.callback.ErrorHandlingOkCallback;
import org.edx.mobile.http.notifications.FullScreenErrorNotification;
import org.edx.mobile.http.provider.OkHttpClientProvider;
import org.edx.mobile.interfaces.RefreshListener;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.api.EnrolledCoursesResponse;
import org.edx.mobile.model.api.HandoutModel;
import org.edx.mobile.util.WebViewUtil;
import org.edx.mobile.view.custom.URLInterceptorWebViewClient;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.Request;

@AndroidEntryPoint
public class CourseHandoutFragment extends BaseFragment implements RefreshListener {

    protected final Logger logger = new Logger(getClass().getName());

    @Inject
    IEdxEnvironment environment;

    @Inject
    OkHttpClientProvider okHttpClientProvider;

    private EnrolledCoursesResponse courseData;
    private FullScreenErrorNotification errorNotification;
    private FragmentWebviewWithPaddingsBinding binding;

    public static Bundle makeArguments(EnrolledCoursesResponse courseData) {
        Bundle courseBundle = new Bundle();
        courseBundle.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
        return courseBundle;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentWebviewWithPaddingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        errorNotification = new FullScreenErrorNotification(binding.webview);
        courseData = (EnrolledCoursesResponse) getArguments().getSerializable(Router.EXTRA_COURSE_DATA);

        new URLInterceptorWebViewClient(requireActivity(), binding.webview, false, null)
                .setAllLinksAsExternal(true);
        loadData();
    }

    private void loadData() {
        okHttpClientProvider.getWithOfflineCache().newCall(new Request.Builder()
                .url(courseData.getCourse().getCourse_handouts())
                .get()
                .build())
                .enqueue(new ErrorHandlingOkCallback<HandoutModel>(requireActivity(),
                        HandoutModel.class, errorNotification, this) {
                    @Override
                    protected void onResponse(@NonNull final HandoutModel result) {
                        if (getActivity() == null) {
                            return;
                        }

                        if (!TextUtils.isEmpty(result.handouts_html)) {
                            populateHandouts(result);
                        } else {
                            errorNotification.showError(R.string.no_handouts_to_display,
                                    R.drawable.ic_error, 0, null);
                        }
                    }

                    @Override
                    protected void onFinish() {
                    }
                });
    }

    private void populateHandouts(HandoutModel handout) {
        hideErrorMessage();

        StringBuilder buff = WebViewUtil.getIntialWebviewBuffer(requireActivity(), logger);

        buff.append("<body>");
        buff.append("<div class=\"header\">");
        buff.append(handout.handouts_html);
        buff.append("</div>");
        buff.append("</body>");

        binding.webview.loadDataWithBaseURL(environment.getConfig().getApiHostURL(), buff.toString(),
                "text/html", Encoding.UTF_8.toString(), null);

    }

    private void hideErrorMessage() {
        binding.webview.setVisibility(View.VISIBLE);
        errorNotification.hideError();
    }

    @Override
    public void onRefresh() {
        errorNotification.hideError();
        loadData();
    }
}
