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
import org.edx.mobile.tta.data.enums.BadgeType;
import org.edx.mobile.tta.data.local.db.table.Badge;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.utils.BadgeHelper;

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

        Badge badge1 = new Badge();
        badge1.setBadgeName("शिक्षक सितारा");
        badge1.setType(BadgeType.star_teacher.name());
        list.add(badge1);

        Badge badge2 = new Badge();
        badge2.setBadgeName("जिज्ञासु");
        badge2.setType(BadgeType.inquisitive.name());
        list.add(badge2);

        Badge badge3 = new Badge();
        badge3.setBadgeName("जागरूक श्रोता");
        badge3.setType(BadgeType.aware_listener.name());
        list.add(badge3);

        Badge badge4 = new Badge();
        badge4.setBadgeName("प्रमाणपत्र");
        badge4.setType(BadgeType.certificate.name());
        list.add(badge4);

        Badge badge5 = new Badge();
        badge5.setBadgeName("मूल्यांकन");
        badge5.setType(BadgeType.evaluator.name());
        list.add(badge5);

        Badge badge6 = new Badge();
        badge6.setBadgeName("प्रशंसक");
        badge6.setType(BadgeType.fan.name());
        list.add(badge6);

        Badge badge7 = new Badge();
        badge7.setBadgeName("रायशुमार");
        badge7.setType(BadgeType.opinion.name());
        list.add(badge7);

        Badge badge8 = new Badge();
        badge8.setBadgeName("उस्ताद");
        badge8.setType(BadgeType.master.name());
        list.add(badge8);

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
                tRowBadgeBinding.ivBadgeimage.setImageResource(BadgeHelper.getBadgeIcon(BadgeType.valueOf(model.getType())));            }
        }
    }
}
