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

    private LinearLayout agendaRegionLists;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AgendaViewModel viewModel = new AgendaViewModel(getActivity(), this);
        View view = binding(inflater, container, R.layout.t_fragment_agenda, viewModel)
                .getRoot();
        agendaRegionLists = view.findViewById(R.id.region_lists);
        viewModel.getAgenda();
        return view;
    }

    public MxFiniteRecyclerView addRegionList(){
        MxFiniteRecyclerView view = new MxFiniteRecyclerView(getActivity());
        view.setItemLayout(R.layout.t_row_agenda_item);
        view.setmMoreButtonVisible(false);
        view.setTitleMargins(getResources().getDimension(R.dimen._20px), 0, 0, 0);
        view.setTitleTextColor(ContextCompat.getColor(getActivity(), R.color.gray_6));
        view.setTitleTextFont(ResourcesCompat.getFont(getActivity(), R.font.hind_semibold));
        view.setTitleTextSize(getResources().getDimension(R.dimen.list_title_size));
        agendaRegionLists.addView(view);
        return view;
    }
}
