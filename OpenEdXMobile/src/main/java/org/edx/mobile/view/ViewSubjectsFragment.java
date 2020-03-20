package org.edx.mobile.view;

import android.app.Activity;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.databinding.FragmentViewSubjectsBinding;
import org.edx.mobile.model.SubjectModel;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.module.analytics.AnalyticsRegistry;
import org.edx.mobile.util.FileUtil;
import org.edx.mobile.util.SoftKeyboardUtil;
import org.edx.mobile.view.adapters.SubjectsAdapter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Inject;

public class ViewSubjectsFragment extends BaseFragment {
    private static final String KEY_SEARCH_QUERY = "query";

    @Inject
    private AnalyticsRegistry analyticsRegistry;
    private FragmentViewSubjectsBinding binding;
    private SubjectsAdapter adapter;
    private String searchQuery;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_subjects, container, false);

        initSubjectsGrid();
        initSubjectsSearch();

        if (savedInstanceState != null) {
            searchQuery = savedInstanceState.getString(KEY_SEARCH_QUERY);
            if (!TextUtils.isEmpty(searchQuery)) {
                performSearch(searchQuery);
            }
        }

        analyticsRegistry.trackScreenView(Analytics.Screens.ALL_SUBJECTS);

        return binding.getRoot();
    }

    private void initSubjectsGrid() {
        final String subjectItemsJson;
        try {
            subjectItemsJson = FileUtil.loadTextFileFromResources(getContext(), R.raw.subjects);
            final Type type = new TypeToken<List<SubjectModel>>() {
            }.getType();
            final List<SubjectModel> allSubjects = new Gson().fromJson(subjectItemsJson, type);
            adapter = new SubjectsAdapter(getContext(), allSubjects);
            binding.glSubjects.setAdapter(adapter);

            binding.glSubjects.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final SubjectModel subjectModel = adapter.getItem(position);
                    final Intent data = new Intent().putExtra(Router.EXTRA_SUBJECT_FILTER, subjectModel.filter);

                    analyticsRegistry.trackSubjectClicked(subjectModel.filter);

                    getActivity().setResult(Activity.RESULT_OK, data);
                    getActivity().finish();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initSubjectsSearch() {
        binding.svSearchSubjects.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });
    }

    private void performSearch(@NonNull String query) {
        adapter.getFilter().filter(query);
    }

    @Override
    public void onResume() {
        super.onResume();
        SoftKeyboardUtil.clearViewFocus(binding.svSearchSubjects);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (!TextUtils.isEmpty(binding.svSearchSubjects.getQuery())) {
            outState.putString(KEY_SEARCH_QUERY, searchQuery);
        }
    }

}
