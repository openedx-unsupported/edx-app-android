package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.edx.mobile.R;
import org.edx.mobile.databinding.PanelFilterBySubjectBinding;
import org.edx.mobile.model.SubjectModel;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.util.FileUtil;
import org.edx.mobile.util.ViewAnimationUtil;
import org.edx.mobile.view.adapters.PopularSubjectsAdapter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.edx.mobile.util.UrlUtil.QUERY_PARAM_SUBJECT;
import static org.edx.mobile.util.UrlUtil.buildUrlWithQueryParams;

public class WebViewDiscoverCoursesFragment extends WebViewDiscoverFragment {
    private static final int VIEW_SUBJECTS_REQUEST_CODE = 999;

    @Nullable
    private PanelFilterBySubjectBinding panelBinding;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (shouldShowSubjectDiscovery()) {
            initSubjects();
        }
    }

    private boolean shouldShowSubjectDiscovery() {
        return getActivity() instanceof MainDashboardActivity &&
                environment.getConfig().getDiscoveryConfig().getCourseDiscoveryConfig().isSubjectFilterEnabled() &&
                getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE;
    }

    @Override
    protected String getSearchUrl() {
        return environment.getConfig().getDiscoveryConfig().getCourseDiscoveryConfig().getBaseUrl();
    }

    @Override
    protected int getQueryHint() {
        return R.string.search_for_courses;
    }

    @Override
    protected boolean isSearchEnabled() {
        return environment.getConfig().getDiscoveryConfig().getCourseDiscoveryConfig().isSearchEnabled();
    }

    private void initSubjects() {
        panelBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.panel_filter_by_subject,
                binding.flAddOnContainer, true);
        binding.flAddOnContainer.setVisibility(View.VISIBLE);

        final String subjectItemsJson;
        try {
            subjectItemsJson = FileUtil.loadTextFileFromResources(getContext(), R.raw.subjects);
            final Type type = new TypeToken<List<SubjectModel>>() {
            }.getType();
            final List<SubjectModel> subjectModels = new Gson().fromJson(subjectItemsJson, type);
            final List<SubjectModel> popularSubjects = new ArrayList<>();
            for (SubjectModel subject : subjectModels) {
                if (subject.type == SubjectModel.Type.POPULAR) {
                    popularSubjects.add(subject);
                }
            }

            final PopularSubjectsAdapter adapter = new PopularSubjectsAdapter(popularSubjects, new PopularSubjectsAdapter.ClickListener() {
                @Override
                public void onSubjectClick(View view) {
                    final int position = panelBinding.rvSubjects.getChildAdapterPosition(view);
                    final String baseUrl = getInitialUrl();
                    final String subjectFilter = popularSubjects.get(position).filter;
                    final Map<String, String> queryParams = new HashMap<>();
                    queryParams.put(QUERY_PARAM_SUBJECT, subjectFilter);
                    loadUrl(buildUrlWithQueryParams(logger, baseUrl, queryParams));
                    setSubjectLayoutVisibility(View.GONE);
                    environment.getAnalyticsRegistry().trackSubjectClicked(subjectFilter);
                }

                @Override
                public void onViewAllSubjectsClick() {
                    environment.getRouter().showSubjectsActivityForResult(WebViewDiscoverCoursesFragment.this,
                            VIEW_SUBJECTS_REQUEST_CODE);
                    environment.getAnalyticsRegistry().trackSubjectClicked(Analytics.Values.VIEW_ALL_SUBJECTS);
                }
            });

            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),
                    LinearLayoutManager.HORIZONTAL, false);
            panelBinding.rvSubjects.setLayoutManager(linearLayoutManager);
            panelBinding.rvSubjects.setAdapter(adapter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWebViewLoadProgressChanged(int progress) {
        if (progress == 100) {
            if (binding.webview.getUrl().contains(QUERY_PARAM_SUBJECT)) {
                // It means that WebView just loaded subject related content
                setSubjectLayoutVisibility(View.GONE);
                binding.webview.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            } else {
                setSubjectLayoutVisibility(View.VISIBLE);
            }
        }
    }

    private void setSubjectLayoutVisibility(int visibility) {
        if (panelBinding != null) {
            if (shouldShowSubjectDiscovery()) {
                ViewAnimationUtil.fadeViewTo(panelBinding.llSubjectContent, visibility);
            } else {
                panelBinding.llSubjectContent.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case VIEW_SUBJECTS_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    final String subjectFilter = data.getStringExtra(Router.EXTRA_SUBJECT_FILTER);
                    final String baseUrl = getInitialUrl();
                    final Map<String, String> queryParams = new HashMap<>();
                    queryParams.put(QUERY_PARAM_SUBJECT, subjectFilter);
                    loadUrl(buildUrlWithQueryParams(logger, baseUrl, queryParams));
                    setSubjectLayoutVisibility(View.GONE);
                }
                break;
        }
    }
}
