package org.edx.mobile.profiles;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.test.BaseTestCase;
import org.edx.mobile.view.adapters.LoadingViewHolder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class AccomplishmentListAdapterTest extends BaseTestCase {
    private AccomplishmentListAdapter adapter;
    @Mock
    View mockView;
    @Mock
    Fragment mockFragment;
    @Mock
    AccomplishmentListAdapter.Listener mockListener;

    @Before
    public void setUp() throws Exception {
        adapter = new TestableAccomplishmentListAdapterTest("http://example.com/", mockListener);
    }

    @Test
    public void getItemCount() {
        adapter.setItems(Arrays.asList(new BadgeAssertion(), new BadgeAssertion(), new BadgeAssertion()));
        adapter.setPageLoading(false);
        assertThat(adapter.getItemCount()).isEqualTo(3);
        adapter.setPageLoading(true);
        assertThat(adapter.getItemCount()).isEqualTo(4);
    }

    @Test
    public void getItemViewType() {
        adapter.setItems(Collections.singletonList(new BadgeAssertion()));
        adapter.setPageLoading(true);
        assertThat(adapter.getItemViewType(0)).isEqualTo(AccomplishmentListAdapter.RowType.ITEM);
        assertThat(adapter.getItemViewType(1)).isEqualTo(AccomplishmentListAdapter.RowType.PROGRESS);
    }

    @Test
    public void onBindViewHolder_withPositionOfItem_calls_setContent() {
        final BadgeAssertion badgeAssertion = new BadgeAssertion();
        adapter.setItems(Collections.singletonList(badgeAssertion));
        final AccomplishmentListAdapter.ItemViewHolder viewHolder = mock(AccomplishmentListAdapter.ItemViewHolder.class);
        adapter.onBindViewHolder(viewHolder, 0);
        verify(viewHolder).setContent(badgeAssertion, false);
    }

    @Test
    public void onBindViewHolder_withPositionOfLoadingIndicator_doesNothing() {
        adapter.setItems(Collections.<BadgeAssertion>emptyList());
        adapter.setPageLoading(true);
        final LoadingViewHolder viewHolder = mock(LoadingViewHolder.class);
        adapter.onBindViewHolder(viewHolder, 0);
        verifyZeroInteractions(viewHolder);
    }

    @Test
    public void onCreateViewHolder_withItemViewType_returnsItemViewHolder() {
        final BadgeAssertion badgeAssertion = new BadgeAssertion();
        adapter.setItems(Collections.singletonList(badgeAssertion));
        final RecyclerView.ViewHolder viewHolder = adapter.onCreateViewHolder(mock(ViewGroup.class), AccomplishmentListAdapter.RowType.ITEM);
        assertThat(viewHolder).isInstanceOf(AccomplishmentListAdapter.ItemViewHolder.class);
    }

    @Test
    public void onCreateViewHolder_withProgressViewType_returnsLoadingViewHolder() {
        final RecyclerView.ViewHolder viewHolder = adapter.onCreateViewHolder(mock(ViewGroup.class), AccomplishmentListAdapter.RowType.PROGRESS);
        assertThat(viewHolder).isInstanceOf(LoadingViewHolder.class);
    }

    static class TestableAccomplishmentListAdapterTest extends AccomplishmentListAdapter {
        public TestableAccomplishmentListAdapterTest(@NonNull String imageUrlPrefix, @NonNull Listener listener) {
            super(imageUrlPrefix, listener);
        }

        @NonNull
        @Override
        protected ItemViewHolder createItemViewHolder(@NonNull ViewGroup parent) {
            return mock(ItemViewHolder.class);
        }

        @NonNull
        @Override
        protected LoadingViewHolder createProgressViewHolder(@NonNull ViewGroup parent) {
            return mock(LoadingViewHolder.class);
        }
    }
}
