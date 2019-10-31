package org.humana.mobile.tta.ui.programs.curricullam;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.humana.mobile.R;
import org.humana.mobile.tta.ui.base.TaBaseFragment;
import org.humana.mobile.tta.ui.library.LibraryFragment;

public class CurricullamFragment extends TaBaseFragment {
    public static final String TAG = LibraryFragment.class.getCanonicalName();
    public CurricullamViewModel viewModel;
    WebView webView;
    final String url_to_load = "http://www.humana-india.org/results-and-data/annual-reports";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new CurricullamViewModel(getActivity(), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = binding(inflater, container, R.layout.curricullam_fragment, viewModel)
                .getRoot();

        webView = rootView.findViewById(R.id.web_view);

        webView.loadUrl(url_to_load);

        return rootView;
    }
}
