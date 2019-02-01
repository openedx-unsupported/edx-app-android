package org.edx.mobile.tta.ui.agenda;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.maurya.mx.mxlib.view.MxFiniteRecyclerView;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.agenda.view_model.AgendaViewModel;
import org.edx.mobile.tta.ui.base.TaBaseFragment;

public class AgendaFragment extends TaBaseFragment {
    public static final String TAG = AgendaFragment.class.getCanonicalName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AgendaViewModel viewModel = new AgendaViewModel(getActivity(), this);
        View view = binding(inflater, container, R.layout.t_fragment_agenda, viewModel)
                .getRoot();
        viewModel.getAgenda();
        return view;
    }
}
