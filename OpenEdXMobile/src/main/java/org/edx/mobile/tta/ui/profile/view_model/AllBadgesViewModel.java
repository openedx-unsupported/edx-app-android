package org.edx.mobile.tta.ui.profile.view_model;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowAllBadgesBinding;
import org.edx.mobile.databinding.TRowBadgeBinding;
import org.edx.mobile.tta.data.local.db.table.Badge;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;

import java.util.ArrayList;
import java.util.List;


public class AllBadgesViewModel extends BaseViewModel {
    public RecyclerView.LayoutManager layoutManager;
    public AllBadgeAdapter adapter;

    public AllBadgesViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);
        adapter =  new AllBadgeAdapter(getActivity());
        adapter.setItems(setbadge());

    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new GridLayoutManager(mActivity, 3);
    }

    private List<Badge> setbadge(){
        List<Badge> list= new ArrayList<>();
        for (int i=0;i<=10;i++){
            Badge badge = new Badge();
            badge.setBadgeName("Badge");
            badge.setBadgeImage("http://theteacherapp.org/asset-v1:Mathematics+M01+201706_Mat_01+type@asset+block@Math_sample2.png");
            list.add(badge);
        }
        return list;
    }

    public class AllBadgeAdapter extends MxInfiniteAdapter<Badge> {

        public AllBadgeAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Badge model, @Nullable OnRecyclerItemClickListener<Badge> listener) {
            if (binding instanceof TRowAllBadgesBinding) {
                TRowAllBadgesBinding tRowBadgeBinding = (TRowAllBadgesBinding) binding;
                tRowBadgeBinding.tvbadgename.setText(model.getBadgeName());
                Glide.with(getContext()).
                        load(model.getBadgeImage()).
                        placeholder(R.drawable.placeholder_course_card_image).
                        into(tRowBadgeBinding.ivBadgeimage);
            }
        }
    }
}
