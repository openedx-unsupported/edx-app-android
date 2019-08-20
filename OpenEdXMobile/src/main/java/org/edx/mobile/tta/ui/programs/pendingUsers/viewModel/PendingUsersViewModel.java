package org.edx.mobile.tta.ui.programs.pendingUsers.viewModel;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxFiniteAdapter;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.CommonFilterItemBinding;
import org.edx.mobile.databinding.TFragmentPendingUsersBinding;
import org.edx.mobile.databinding.TRowPendingFilterBinding;
import org.edx.mobile.databinding.TRowStudentsGridBinding;
import org.edx.mobile.tta.data.model.program.ProgramFilter;
import org.edx.mobile.tta.data.model.program.ProgramUser;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;

import java.util.List;

public class PendingUsersViewModel extends BaseViewModel {

    public List<ProgramFilter> filterList;
    public List<ProgramUser> programUserList;
    public FiltersAdapter filtersAdapter;
    public UsersAdapter usersAdapter;


    public RecyclerView.LayoutManager filterLayoutManager;
    public RecyclerView.LayoutManager layoutManager;
    public PendingUsersViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        usersAdapter = new UsersAdapter(mActivity);
        filtersAdapter = new FiltersAdapter(mActivity);
    }


    @Override
    public void onResume() {
        super.onResume();

        filterLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);
        layoutManager = new GridLayoutManager(mActivity, 2);

    }

    public void getFilters(){
        mDataManager.getProgramFilters(new OnResponseCallback<List<ProgramFilter>>() {
            @Override
            public void onSuccess(List<ProgramFilter> data) {
                filterList = data;

            }

            @Override
            public void onFailure(Exception e) {

            }
        });
        filtersAdapter.setItems(filterList);

    }

    public void fetchUsers(){
        mDataManager.getPendingUsers("", "", 0, 0,
                new OnResponseCallback<List<ProgramUser>>() {
            @Override
            public void onSuccess(List<ProgramUser> data) {
                programUserList = data;
            }

            @Override
            public void onFailure(Exception e) {

            }
        });
        usersAdapter.setItems(programUserList);
    }

    public class UsersAdapter extends MxInfiniteAdapter<ProgramUser> {

        public UsersAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull ProgramUser model,
                           @Nullable OnRecyclerItemClickListener<ProgramUser> listener) {
            if (binding instanceof TRowStudentsGridBinding) {
                TFragmentPendingUsersBinding teacherBinding = (TFragmentPendingUsersBinding) binding;
//                teacherBinding.setViewModel(model);


//                teacherBinding.followBtn.setOnClickListener(v -> {
//                    if (listener != null) {
//                        listener.onItemClick(v, model);
//                    }
//                });
//
//                teacherBinding.getRoot().setOnClickListener(v -> {
//                    if (listener != null) {
//                        listener.onItemClick(v, model);
//                    }
//                });
            }
        }
    }

    public class FiltersAdapter extends MxFiniteAdapter<ProgramFilter> {
        public FiltersAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull ProgramFilter model,
                           @Nullable OnRecyclerItemClickListener<ProgramFilter> listener) {
            if (binding instanceof TRowPendingFilterBinding){
                TRowPendingFilterBinding itemBinding = (TRowPendingFilterBinding) binding;

            }

        }

    }
}
