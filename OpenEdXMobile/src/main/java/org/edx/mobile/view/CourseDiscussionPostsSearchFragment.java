package org.edx.mobile.view;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.databinding.FragmentDiscussionSearchPostsBinding;
import org.edx.mobile.discussion.DiscussionRequestFields;
import org.edx.mobile.discussion.DiscussionService;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.http.callback.CallTrigger;
import org.edx.mobile.http.callback.ErrorHandlingCallback;
import org.edx.mobile.model.Page;
import org.edx.mobile.module.analytics.Analytics;
import org.edx.mobile.util.ResourceUtil;
import org.edx.mobile.util.SoftKeyboardUtil;
import org.edx.mobile.view.adapters.InfiniteScrollUtils;
import org.edx.mobile.view.common.MessageType;
import org.edx.mobile.view.common.TaskMessageCallback;
import org.edx.mobile.view.common.TaskProcessCallback;
import org.edx.mobile.view.common.TaskProgressCallback;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

public class CourseDiscussionPostsSearchFragment extends CourseDiscussionPostsBaseFragment {

    @Inject
    private DiscussionService discussionService;

    private String searchQuery;
    private Call<Page<DiscussionThread>> searchThreadListCall;
    private FragmentDiscussionSearchPostsBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseExtras();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDiscussionSearchPostsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.discussionTopicsSearchview.setQuery(searchQuery, false);
        binding.discussionTopicsSearchview.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query == null || query.trim().isEmpty())
                    return false;
                SoftKeyboardUtil.hide(requireActivity());
                searchQuery = query;
                nextPage = 1;
                controller.reset();
                binding.discussionPostsListview.setVisibility(View.INVISIBLE);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        final Map<String, String> values = new HashMap<>();
        values.put(Analytics.Keys.SEARCH_STRING, searchQuery);
        environment.getAnalyticsRegistry().trackScreenView(Analytics.Screens.FORUM_SEARCH_THREADS,
                courseData.getCourse().getId(), searchQuery, values);
    }

    private void parseExtras() {
        searchQuery = getArguments().getString(Router.EXTRA_SEARCH_QUERY);
    }

    @Override
    protected ListView getDiscussionPostsListView() {
        return binding.discussionPostsListview;
    }

    @Override
    public void loadNextPage(@NonNull final InfiniteScrollUtils.PageLoadCallback<DiscussionThread> callback) {
        final Activity activity = requireActivity();
        final TaskProgressCallback progressCallback = activity instanceof TaskProgressCallback ? (TaskProgressCallback) activity : null;
        final TaskMessageCallback mCallback = activity instanceof TaskMessageCallback ? (TaskMessageCallback) activity : null;
        if (mCallback != null) {
            mCallback.onMessage(MessageType.EMPTY, "");
        }
        if (searchThreadListCall != null) {
            searchThreadListCall.cancel();
        }
        final List<String> requestedFields = Collections.singletonList(
                DiscussionRequestFields.PROFILE_IMAGE.getQueryParamValue());
        searchThreadListCall = discussionService.searchThreadList(
                courseData.getCourse().getId(), searchQuery, nextPage, requestedFields);
        final boolean isRefreshingSilently = callback.isRefreshingSilently();
        searchThreadListCall.enqueue(new ErrorHandlingCallback<Page<DiscussionThread>>(
                activity,
                // Initially we need to show the spinner at the center of the screen. After that,
                // the ListView will start showing a footer-based loading indicator.
                nextPage > 1 || isRefreshingSilently ? null : progressCallback,
                mCallback, CallTrigger.LOADING_UNCACHED) {
            @Override
            protected void onResponse(@NonNull final Page<DiscussionThread> threadsPage) {
                ++nextPage;
                callback.onPageLoaded(threadsPage);
                if (activity instanceof TaskProcessCallback) {
                    if (discussionPostsAdapter.getCount() == 0) {
                        String escapedTitle = TextUtils.htmlEncode(searchQuery);
                        String resultsText = ResourceUtil.getFormattedString(
                                requireContext().getResources(),
                                R.string.forum_no_results_for_search_query,
                                "search_query",
                                escapedTitle
                        ).toString();
                        // CharSequence styledResults = Html.fromHtml(resultsText);
                        ((TaskProcessCallback) activity).onMessage(MessageType.ERROR, resultsText);
                    } else {
                        ((TaskProcessCallback) activity).onMessage(MessageType.EMPTY, "");
                        binding.discussionPostsListview.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Page<DiscussionThread>> call, @NonNull Throwable error) {
                // Don't display any error message if we're doing a silent
                // refresh, as that would be confusing to the user.
                if (!callback.isRefreshingSilently()) {
                    super.onFailure(call, error);
                }
                callback.onError();
                nextPage = 1;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        SoftKeyboardUtil.clearViewFocus(binding.discussionTopicsSearchview);
    }
}
