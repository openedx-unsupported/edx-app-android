package org.edx.mobile.tta.ui.programs.discussion;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.library.LibraryFragment;

public class DiscussionFragment extends TaBaseFragment {

    public static final String TAG = LibraryFragment.class.getCanonicalName();
    public DiscussionViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = binding(inflater, container, R.layout.nothing_to_show, viewModel)
                .getRoot();

        viewModel = new DiscussionViewModel(getActivity(), this);
        TextView mtext_nothing;
        mtext_nothing = rootView.findViewById(R.id.text_nothing);
        mtext_nothing.setText("No discussions available..");
        return rootView;
    }
}
