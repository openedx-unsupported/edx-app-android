package org.edx.mobile.tta.ui.programs.schedule;

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
import org.edx.mobile.databinding.TRowAgendaContentBinding;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.local.db.table.ContentStatus;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.ui.custom.DropDownFilterView;
import org.edx.mobile.tta.utils.ContentSourceUtil;

import java.util.ArrayList;
import java.util.List;

public class ScheduleViewModel extends BaseViewModel {

    public List<DropDownFilterView.FilterItem> typeFilters;
    public List<DropDownFilterView.FilterItem> sessionFilters;

    public DropDownFilterView.OnFilterClickListener typeListener = (v, item, position, prev) -> {

    };

    public DropDownFilterView.OnFilterClickListener sessionListener = (v, item, position, prev) -> {

    };

    public ContentsAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    public ScheduleViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        adapter = new ContentsAdapter(mActivity);

        setFilters();
        fetchSchedules();
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new GridLayoutManager(mActivity, 2);
    }

    private void setFilters(){

        typeFilters = new ArrayList<>();
        typeFilters.add(new DropDownFilterView.FilterItem("Type", null, true,
                R.color.gray_5, R.drawable.t_background_tag_hollow));
        typeFilters.add(new DropDownFilterView.FilterItem("Subject", null, false,
                R.color.white, R.drawable.t_background_tag_filled));
        typeFilters.add(new DropDownFilterView.FilterItem("Unit", null, false,
                R.color.white, R.drawable.t_background_tag_filled));

        sessionFilters = new ArrayList<>();
        sessionFilters.add(new DropDownFilterView.FilterItem("Session", null, true,
                R.color.gray_5, R.drawable.t_background_tag_hollow));
        sessionFilters.add(new DropDownFilterView.FilterItem("Session 1", null, false,
                R.color.white, R.drawable.t_background_tag_filled));
        sessionFilters.add(new DropDownFilterView.FilterItem("Session 2", null, false,
                R.color.white, R.drawable.t_background_tag_filled));

    }

    private void fetchSchedules() {
        List<Content> contents = new ArrayList<>();
        for (int i = 0; i < 20; i++){
            Content content = new Content();
            content.setName("Schedule - " + (i+1));
            content.setIcon("http://www.humana-india.org/images/GSN_1618_GOLD.JPG");
            contents.add(content);
        }
        adapter.setItems(contents);
    }

    public class ContentsAdapter extends MxInfiniteAdapter<Content> {
        public ContentsAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Content model, @Nullable OnRecyclerItemClickListener<Content> listener) {
            if (binding instanceof TRowAgendaContentBinding){
                TRowAgendaContentBinding contentBinding = (TRowAgendaContentBinding) binding;
                contentBinding.contentTitle.setText(model.getName());
                Glide.with(mActivity)
                        .load(model.getIcon())
                        .placeholder(R.drawable.placeholder_course_card_image)
                        .into(contentBinding.contentImage);
                contentBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
            }
        }
    }
}
