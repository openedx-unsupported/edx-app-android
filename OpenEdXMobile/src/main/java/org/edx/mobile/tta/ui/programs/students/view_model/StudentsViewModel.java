package org.edx.mobile.tta.ui.programs.students.view_model;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowSuggestedTeacherGridBinding;
import org.edx.mobile.tta.data.model.feed.SuggestedUser;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;

import java.util.ArrayList;
import java.util.List;

public class StudentsViewModel extends BaseViewModel {

    public UsersAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    public StudentsViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        adapter = new UsersAdapter(mActivity);

        fetchStudents();
    }

    @Override
    public void onResume() {
        super.onResume();
        layoutManager = new GridLayoutManager(mActivity, 2);
    }

    private void fetchStudents() {

        List<SuggestedUser> users = new ArrayList<>();
        String name = mDataManager.getLoginPrefs().getUsername() != null &&
                mDataManager.getLoginPrefs().getUsername().equalsIgnoreCase("staff") ?
                "Staff" : "Student";
        for (int i = 0; i < 20; i++){
            SuggestedUser user = new SuggestedUser();
            user.setName(name + " - " + (i+1));
            users.add(user);
        }
        adapter.setItems(users);

    }

    public class UsersAdapter extends MxInfiniteAdapter<SuggestedUser> {

        public UsersAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull SuggestedUser model, @Nullable OnRecyclerItemClickListener<SuggestedUser> listener) {
            if (binding instanceof TRowSuggestedTeacherGridBinding) {
                TRowSuggestedTeacherGridBinding teacherBinding = (TRowSuggestedTeacherGridBinding) binding;
                teacherBinding.setViewModel(model);
                Glide.with(getContext())
                        .load("")
                        .placeholder(R.drawable.profile_photo_placeholder)
                        .into(teacherBinding.userImage);

                teacherBinding.followBtn.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

                teacherBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });
            }
        }
    }
}
