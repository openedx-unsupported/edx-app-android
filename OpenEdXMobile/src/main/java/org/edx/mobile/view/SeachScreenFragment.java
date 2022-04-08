package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.annotation.Nullable;
import org.edx.mobile.base.BaseFragment;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.databinding.FragmentSearchScreenBinding;
import org.edx.mobile.discovery.DiscoveryCallback;
import org.edx.mobile.discovery.model.CombinationOfSeachResult;
import org.edx.mobile.discovery.model.ResponseError;
import org.edx.mobile.discovery.model.SearchResult;
import org.edx.mobile.discovery.model.SearchResultList;
import org.edx.mobile.discovery.model.SearchTags;
import org.edx.mobile.discovery.net.course.CourseApi;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.LocaleManager;
import org.edx.mobile.view.adapters.OnRecyclerItemClickListener;
import org.edx.mobile.view.adapters.SearchListAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

import static android.app.Activity.RESULT_OK;

public class SeachScreenFragment extends BaseFragment implements OnRecyclerItemClickListener {
    public static final String TAG = SeachScreenFragment.class.getCanonicalName();
    private FragmentSearchScreenBinding binding;
    @Inject
    LoginPrefs loginPrefs;
    @Inject
    CourseApi courseApi;
    private SearchListAdapter searchListAdapter;
    private int page = 1;
    @Inject
    protected IEdxEnvironment environment;

    public static SeachScreenFragment newInstance(@Nullable Bundle bundle) {
        final SeachScreenFragment fragment = new SeachScreenFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public void showSoftKeyboard(View view){
        if(view.requestFocus()){
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_search_screen, container,
                false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.announceForAccessibility("Search Screen");
        binding.shimmerLayout.setVisibility(View.GONE);
      /*  if (binding.editSearch.isFocused()) {
            binding.editSearch.setCursorVisible(true);
        }*/
        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        binding.seachIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.editSearch.getText().toString().length() > 0) {
                    binding.searchResults.setVisibility(View.VISIBLE);
                    binding.shimmerLayout.setVisibility(View.VISIBLE);
                    binding.searchResult.setVisibility(View.GONE);
                    binding.searchCount.setVisibility(View.GONE);
                    getSearchResult(binding.editSearch.getText().toString());
                } else {
                    Toast.makeText(getActivity(), "Nothing to search", Toast.LENGTH_LONG).show();
                }
            }
        });
        binding.viewMoreResults.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSearchNextResult(binding.editSearch.getText().toString());
            }
        });

        searchListAdapter = new SearchListAdapter(getActivity(), SeachScreenFragment.this::onItemClick);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        binding.searchResult.setLayoutManager(mLayoutManager);
        binding.searchResult.setAdapter(searchListAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
        }
    }

    private void getSearchResult(String query) {
        final String token = loginPrefs.getAuthorizationHeaderJwt();
        if (token != null) {
            Log.d("Token_JWT ", token);
        }
        String selectedLanguage = "en";
        if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
            selectedLanguage = LocaleManager.getLanguagePref(getActivity());
        }
        page = 1;
        Call<SearchResult> search = courseApi.getSearchNextResult(token, selectedLanguage, String.valueOf(page), "100", query.trim());
        search.enqueue(new DiscoveryCallback<SearchResult>() {
            @Override
            protected void onResponse(@NonNull SearchResult responseBody) {
                List<CombinationOfSeachResult> combinationOfSeachResults = new ArrayList<>();
                List<CombinationOfSeachResult> newcombinationOfSeachResults = new ArrayList<>();
                binding.shimmerLayout.setVisibility(View.GONE);
                if (responseBody != null) {
                    if (responseBody.getNext() != null) {
                        page = page + 1;
                        binding.viewMoreResults.setVisibility(View.VISIBLE);
                    } else {
                        binding.viewMoreResults.setVisibility(View.GONE);
                    }
                    for (SearchResultList searchResultList : responseBody.getResults()) {
                        if (searchResultList.getProgram_details() != null) {
                            if (searchResultList.getProgram_details().getTags() != null) {
                                for (SearchTags searchTags : searchResultList.getProgram_details().getTags()) {
                                    if (searchTags.getTags() != null) {
                                        for (String tag : searchTags.getTags()) {
                                            CombinationOfSeachResult combinationOfSeachResult = new CombinationOfSeachResult();
                                            combinationOfSeachResult.setCourseName(searchResultList.getTitle());
                                            combinationOfSeachResult.setProgramName(searchTags.getProgram_name());
                                            combinationOfSeachResult.setTagName(tag);
                                            combinationOfSeachResults.add(combinationOfSeachResult);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (combinationOfSeachResults != null && combinationOfSeachResults.size() > 0) {
                    String userType = loginPrefs.getUserType();
                    if (userType != null) {
                        if (userType.toLowerCase().equals("teacher")) {
                            for (CombinationOfSeachResult combinationOfSeachResult : combinationOfSeachResults) {
                                if (combinationOfSeachResult.getTagName().toLowerCase().contains("teacher")) {
                                    newcombinationOfSeachResults.add(combinationOfSeachResult);
                                } else if (!combinationOfSeachResult.getTagName().toLowerCase().contains("student") &&
                                        !combinationOfSeachResult.getTagName().toLowerCase().contains("teacher")) {
                                    newcombinationOfSeachResults.add(combinationOfSeachResult);
                                }
                            }
                        } else if (userType.toLowerCase().equals("student")) {
                            for (CombinationOfSeachResult combinationOfSeachResult : combinationOfSeachResults) {
                                if (combinationOfSeachResult.getTagName().toLowerCase().contains("student")) {
                                    newcombinationOfSeachResults.add(combinationOfSeachResult);
                                }else if (!combinationOfSeachResult.getTagName().toLowerCase().contains("student") &&
                                        !combinationOfSeachResult.getTagName().toLowerCase().contains("teacher")) {
                                    newcombinationOfSeachResults.add(combinationOfSeachResult);
                                }
                            }
                        }
                    } else {
                        newcombinationOfSeachResults.clear();
                        for (CombinationOfSeachResult combinationOfSeachResult : combinationOfSeachResults) {
                            if (!combinationOfSeachResult.getTagName().toLowerCase().contains("student") &&
                                    !combinationOfSeachResult.getTagName().toLowerCase().contains("teacher")) {
                                newcombinationOfSeachResults.add(combinationOfSeachResult);
                            }
                        }
                    }
                }
                binding.searchResult.setVisibility(View.VISIBLE);
                searchListAdapter.setSearchResult(newcombinationOfSeachResults);
                binding.searchCount.setVisibility(View.VISIBLE);
                binding.searchCount.setText(String.valueOf(searchListAdapter.getItemCount()) + " " + getString(R.string.results_for) + " " + binding.editSearch.getText().toString().trim());
                binding.searchCount.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            }

            @Override
            protected void onFailure(ResponseError responseError, @NonNull Throwable error) {
                super.onFailure(responseError, error);
            }
        });

    }

    private void getSearchNextResult(String query) {
        final String token = loginPrefs.getAuthorizationHeaderJwt();
        if (token != null) {
            Log.d("Token_JWT ", token);
        }
        String selectedLanguage = "en";
        if (!LocaleManager.getLanguagePref(getActivity()).isEmpty()) {
            selectedLanguage = LocaleManager.getLanguagePref(getActivity());
        }
        Call<SearchResult> search = courseApi.getSearchNextResult(token, selectedLanguage, String.valueOf(page), "100", query.trim());
        search.enqueue(new DiscoveryCallback<SearchResult>() {
            @Override
            protected void onResponse(@NonNull SearchResult responseBody) {
                List<CombinationOfSeachResult> combinationOfSeachResults = new ArrayList<>();
                List<CombinationOfSeachResult> newcombinationOfSeachResults = new ArrayList<>();
                if (responseBody != null) {
                    if (responseBody.getNext() != null) {
                        page = page + 1;
                        binding.viewMoreResults.setVisibility(View.VISIBLE);
                    } else {
                        binding.viewMoreResults.setVisibility(View.GONE);
                    }

                    for (SearchResultList searchResultList : responseBody.getResults()) {
                        if (searchResultList.getProgram_details() != null) {
                            if (searchResultList.getProgram_details().getTags() != null) {
                                for (SearchTags searchTags : searchResultList.getProgram_details().getTags()) {
                                    if (searchTags.getTags() != null) {
                                        for (String tag : searchTags.getTags()) {
                                            CombinationOfSeachResult combinationOfSeachResult = new CombinationOfSeachResult();
                                            combinationOfSeachResult.setCourseName(searchResultList.getTitle());
                                            combinationOfSeachResult.setProgramName(searchTags.getProgram_name());
                                            combinationOfSeachResult.setTagName(tag);
                                            combinationOfSeachResults.add(combinationOfSeachResult);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if (combinationOfSeachResults != null && combinationOfSeachResults.size() > 0) {
                    String userType = loginPrefs.getUserType();
                    if (userType != null) {
                        if (userType.toLowerCase().equals("teacher")) {
                            for (CombinationOfSeachResult combinationOfSeachResult : combinationOfSeachResults) {
                                if (combinationOfSeachResult.getTagName().toLowerCase().contains("teacher")) {
                                    newcombinationOfSeachResults.add(combinationOfSeachResult);
                                } else if (!combinationOfSeachResult.getTagName().toLowerCase().contains("student") &&
                                        !combinationOfSeachResult.getTagName().toLowerCase().contains("teacher")) {
                                    newcombinationOfSeachResults.add(combinationOfSeachResult);
                                }
                            }
                        } else if (userType.toLowerCase().equals("student")) {
                            for (CombinationOfSeachResult combinationOfSeachResult : combinationOfSeachResults) {
                                if (combinationOfSeachResult.getTagName().toLowerCase().contains("student")) {
                                    newcombinationOfSeachResults.add(combinationOfSeachResult);
                                } else if (!combinationOfSeachResult.getTagName().toLowerCase().contains("student") &&
                                        !combinationOfSeachResult.getTagName().toLowerCase().contains("teacher")) {
                                    newcombinationOfSeachResults.add(combinationOfSeachResult);
                                }
                            }
                        }
                    } else {
                        newcombinationOfSeachResults.clear();
                        for (CombinationOfSeachResult combinationOfSeachResult : combinationOfSeachResults) {
                            if (!combinationOfSeachResult.getTagName().toLowerCase().contains("student") &&
                                    !combinationOfSeachResult.getTagName().toLowerCase().contains("teacher")) {
                                newcombinationOfSeachResults.add(combinationOfSeachResult);
                            }
                        }
                    }
                }
                searchListAdapter.updateSearchResult(newcombinationOfSeachResults);
                binding.searchCount.setText(String.valueOf(searchListAdapter.getItemCount()) +
                        " " + "results for " + binding.editSearch.getText().toString().trim());
                binding.searchCount.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
            }

            @Override
            protected void onFailure(ResponseError responseError, @NonNull Throwable error) {
                super.onFailure(responseError, error);
            }
        });

    }

    @Override
    public void onResume() {
        //  binding.shimmerLayout.startShimmer();
        super.onResume();
    }

    @Override
    public void onPause() {
        //  binding.shimmerLayout.stopShimmer();
        super.onPause();
    }

    @Override
    public void onItemClick(View view, Object item) {
        if (item instanceof CombinationOfSeachResult) {
            CombinationOfSeachResult combinationOfSeachResult = (CombinationOfSeachResult) item;
            environment.getRouter().showProgramsActivity(getActivity(), combinationOfSeachResult.getTagName(), "");
        }
    }
}
