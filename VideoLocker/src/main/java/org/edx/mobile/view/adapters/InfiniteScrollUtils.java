package org.edx.mobile.view.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.edx.mobile.R;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class InfiniteScrollUtils {
    private static final int VISIBILITY_THRESHOLD = 2; // Load more when only this number of items exists off-screen

    public static <T> InfiniteListController configureListViewWithInfiniteList(@NonNull final ListView list, @NonNull final ArrayAdapter<T> adapter, @NonNull final PageLoader<T> pageLoader) {
        final View footerView = LayoutInflater.from(list.getContext()).inflate(R.layout.list_view_footer_progress, list, false);
        list.addFooterView(footerView, null, false);
        final View loadingIndicator = footerView.findViewById(R.id.loading_indicator);
        final PageLoadController controller = new PageLoadController<>(
                new ListContentController<T>() {
                    @Override
                    public void clear() {
                        adapter.clear();
                    }

                    @Override
                    public void addAll(List<T> items) {
                        adapter.addAll(items);
                    }

                    @Override
                    public void setProgressVisible(boolean visible) {
                        loadingIndicator.setVisibility(visible ? View.VISIBLE : View.GONE);
                    }
                },
                pageLoader);
        controller.onLoadMore();
        list.setOnScrollListener(new ListViewOnScrollListener(controller));
        list.setAdapter(adapter);
        return controller;
    }

    public static <T> InfiniteListController configureRecyclerViewWithInfiniteList(@NonNull final RecyclerView recyclerView, @NonNull final ListContentController<T> adapter, @NonNull final PageLoader<T> pageLoader) {
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        final PageLoadController controller = new PageLoadController<>(adapter, pageLoader);
        controller.onLoadMore();
        recyclerView.addOnScrollListener(new RecyclerViewOnScrollListener(linearLayoutManager, controller));
        return controller;
    }

    public interface ListContentController<T> {
        void clear();

        void addAll(List<T> items);

        void setProgressVisible(boolean visible);
    }

    public interface PageLoader<T> {
        void loadNextPage(@NonNull PageLoadCallback<T> callback);
    }

    public interface PageLoadCallback<T> {
        void onPartialPageLoaded(List<T> newItems);
        void onPageLoaded(List<T> newItems, boolean hasMore);
    }

    public interface InfiniteListController {
        void reset();
    }

    public static class PageLoadController<T> implements InfiniteListController {
        @NonNull
        final ListContentController<T> adapter;
        @NonNull
        final PageLoader<T> pageLoader;
        protected boolean hasMoreItems = true;
        protected boolean loading = true;
        final AtomicInteger activeLoadId = new AtomicInteger();

        public PageLoadController(@NonNull ListContentController<T> adapter, @NonNull PageLoader<T> pageLoader) {
            this.adapter = adapter;
            this.pageLoader = pageLoader;
        }

        public void loadMore() {
            if (!loading && hasMoreItems) {
                loading = true;
                onLoadMore();
            }
        }

        private void onLoadMore() {
            final int instanceLoadId = activeLoadId.get();
            adapter.setProgressVisible(true);
            pageLoader.loadNextPage(new PageLoadCallback<T>() {
                @Override
                public void onPartialPageLoaded(List<T> newItems) {
                    if (isAbandoned()) {
                        return;
                    }
                    adapter.addAll(newItems);
                }

                @Override
                public void onPageLoaded(List<T> newItems, boolean hasMore) {
                    if (isAbandoned()) {
                        return;
                    }
                    adapter.addAll(newItems);
                    if (!hasMore) {
                        adapter.setProgressVisible(false);
                    }
                    hasMoreItems = hasMore;
                    loading = false;
                }

                /**
                 * Return whether this callback has been abandoned because of the controller being reset.
                 *
                 * @return <code>true</code> if the callback has been abandoned otherwise <code>false</code>
                 */
                private boolean isAbandoned() {
                    // Disregard result, since reset() was called
                    return instanceLoadId != activeLoadId.get();
                }
            });
        }

        @Override
        public void reset() {
            activeLoadId.incrementAndGet(); // To disregard any in-progress loads
            adapter.clear();
            hasMoreItems = true;
            loading = true;
            loadMore();
        }
    }

    private static class ListViewOnScrollListener implements AbsListView.OnScrollListener {
        private final PageLoadController pageLoadController;

        protected ListViewOnScrollListener(PageLoadController pageLoadController) {
            this.pageLoadController = pageLoadController;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem + visibleItemCount >= totalItemCount - VISIBILITY_THRESHOLD) {
                pageLoadController.loadMore();
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    }

    public static class RecyclerViewOnScrollListener extends RecyclerView.OnScrollListener {

        private final LinearLayoutManager mLinearLayoutManager;
        private final PageLoadController pageLoadController;

        public RecyclerViewOnScrollListener(LinearLayoutManager linearLayoutManager, PageLoadController pageLoadController) {
            this.mLinearLayoutManager = linearLayoutManager;
            this.pageLoadController = pageLoadController;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            final int visibleItemCount = recyclerView.getChildCount();
            final int totalItemCount = mLinearLayoutManager.getItemCount();
            final int firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

            if ((totalItemCount - visibleItemCount)
                    <= (firstVisibleItem + VISIBILITY_THRESHOLD)) {
                pageLoadController.loadMore();
            }
        }
    }
}
