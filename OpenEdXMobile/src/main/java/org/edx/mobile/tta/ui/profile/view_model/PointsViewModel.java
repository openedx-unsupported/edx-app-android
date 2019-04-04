package org.edx.mobile.tta.ui.profile.view_model;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowPointsBinding;
import org.edx.mobile.tta.data.local.db.table.Points;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

public class PointsViewModel extends BaseViewModel {
    public PointsAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    public PointsViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
        adapter = new PointsAdapter(getActivity());
        adapter.setItems(listPoints());
    }
    @Override
    public void onResume() {
        layoutManager = new LinearLayoutManager(mActivity);
    }
    private List<Points> listPoints() {
        List<Points> list = new ArrayList<>();
        for (int i = 0; i <= 15; i++) {
            Points points = new Points();
            points.setCondition(getActivity().getString(R.string.all_parts));
            points.setConditionPoints("90");
            list.add(points);
        }
        return list;
    }

    public class PointsAdapter extends MxInfiniteAdapter<Points> {
        public PointsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Points model, @Nullable OnRecyclerItemClickListener<Points> listener) {
            if (binding instanceof TRowPointsBinding) {
                TRowPointsBinding tRowPointsBinding = (TRowPointsBinding) binding;
                tRowPointsBinding.tvPoints.setText(model.getConditionPoints());
                tRowPointsBinding.tvCondition.setText(model.getCondition());
            }
        }
    }

}
