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
import org.edx.mobile.model.Page;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class InfiniteScrollUtils {
    private static final int VISIBILITY_THRESHOLD = 2; // Load more when only this number of items exists off-screen

    public static <T> InfiniteListController configureListViewWithInfiniteList(@NonNull final ListView list, @NonNull final ArrayAdapter<T> adapter, @NonNull final PageLoader<T> pageLoader) {
        final View footerView = LayoutInflater.from(list.getContext()).inflate(R.layout.list_view_footer_progress, list, false);
        list.addFooterView(footerView, null, false);
        final View loadingIndicator = footerView.findViewById(R.id.loading_indicator);
        final InfiniteListController controller = new PageLoadController<>(
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
        controller.loadMore();
        list.setOnScrollListener(new ListViewOnScrollListener(controller));
        list.setAdapter(adapter);
        return controller;
    }

    public static <T> InfiniteListController configureRecyclerViewWithInfiniteList(@NonNull final RecyclerView recyclerView, @NonNull final ListContentController<T> adapter, @NonNull final PageLoader<T> pageLoader) {
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(recyclerView.getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        final InfiniteListController controller = new PageLoadController<>(adapter, pageLoader);
        controller.loadMore();
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
        void onPageLoaded(Page<T> newPage);
    }

    public interface InfiniteListController {
        void loadMore();
        void reset();
    }

    public static class PageLoadController<T> implements InfiniteListController {
        @NonNull
        final ListContentController<T> adapter;
        @NonNull
        final PageLoader<T> pageLoader;
        protected boolean hasMoreItems = true;
        protected boolean loading;
        final AtomicInteger activeLoadId = new AtomicInteger();

        public PageLoadController(@NonNull ListContentController<T> adapter, @NonNull PageLoader<T> pageLoader) {
            this.adapter = adapter;
            this.pageLoader = pageLoader;
        }

        @Override
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
                public void onPageLoaded(Page<T> newPage) {
                    if (isAbandoned()) {
                        return;
                    }
                    adapter.addAll(newPage.getResults());
                    hasMoreItems = newPage.hasNext();
                    if (!hasMoreItems) {
                        adapter.setProgressVisible(false);
                    }
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
            onLoadMore();
        }
    }

    private static class ListViewOnScrollListener implements AbsListView.OnScrollListener {
        private final InfiniteListController infiniteListController;

        protected ListViewOnScrollListener(InfiniteListController infiniteListController) {
            this.infiniteListController = infiniteListController;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem + visibleItemCount >= totalItemCount - VISIBILITY_THRESHOLD) {
                infiniteListController.loadMore();
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    }

    public static class RecyclerViewOnScrollListener extends RecyclerView.OnScrollListener {

        private final LinearLayoutManager mLinearLayoutManager;
        private final InfiniteListController infiniteListController;

        public RecyclerViewOnScrollListener(LinearLayoutManager linearLayoutManager, InfiniteListController infiniteListController) {
            this.mLinearLayoutManager = linearLayoutManager;
            this.infiniteListController = infiniteListController;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            final int visibleItemCount = recyclerView.getChildCount();
            final int totalItemCount = mLinearLayoutManager.getItemCount();
            final int firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

            if ((totalItemCount - visibleItemCount)
                    <= (firstVisibleItem + VISIBILITY_THRESHOLD)) {
                infiniteListController.loadMore();
            }
        }
    }
}
