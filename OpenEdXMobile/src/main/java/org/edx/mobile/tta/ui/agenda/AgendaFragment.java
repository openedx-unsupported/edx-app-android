package org.edx.mobile.tta.ui.agenda;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.edx.mobile.R;
import org.edx.mobile.tta.ui.agenda.view_model.AgendaViewModel;
import org.edx.mobile.tta.ui.base.TaBaseFragment;

public class AgendaFragment extends TaBaseFragment {
    public static final String TAG = AgendaFragment.class.getCanonicalName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return binding(inflater, container, R.layout.t_fragment_agenda, new AgendaViewModel(getActivity(), this))
                .getRoot();
    }
}
