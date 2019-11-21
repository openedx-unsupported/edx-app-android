package org.humana.mobile.tta.ui.programs.pendingUnits.viewModel;

import android.app.Dialog;
import android.content.Context;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.humana.mobile.R;
import org.humana.mobile.databinding.TRowPendingUnitsBinding;
import org.humana.mobile.tta.data.enums.UserRole;
import org.humana.mobile.tta.data.local.db.table.Unit;
import org.humana.mobile.tta.data.model.SuccessResponse;
import org.humana.mobile.tta.interfaces.OnResponseCallback;
import org.humana.mobile.tta.ui.base.mvvm.BaseVMActivity;
import org.humana.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.humana.mobile.tta.ui.programs.pendingUnits.PendingUnitWebviewActivity;
import org.humana.mobile.tta.utils.ActivityUtil;
import org.humana.mobile.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

public class PendingUnitsWebViewViewModel extends BaseViewModel {

    private static final int TAKE = 10;
    private static final int SKIP = 0;

    private boolean allLoaded;
    private boolean changesMade;
    private int take, skip;
    public ObservableField<String> userName = new ObservableField<>();
    private List<Unit> unitsList;

    public RecyclerView.LayoutManager layoutManager;

    public float rating = 0;

    public PendingUnitsWebViewViewModel(BaseVMActivity activity) {
        super(activity);

        Bundle bundle = mActivity.getIntent().getExtras();
        assert bundle != null;
        userName.set(bundle.getString("username"));
        layoutManager = new LinearLayoutManager(mActivity);
        unitsList = new ArrayList<>();
        changesMade = true;
        take = TAKE;

        skip = SKIP;

        mActivity.showLoading();
    }



    public void getWebResponse() {
        String role;
        if (mDataManager.getLoginPrefs().getRole().equals(UserRole.Student.name())) {
            role= mDataManager.getLoginPrefs().getRole();
        }else {
            role = "staff";
        }

//        mDataManager.setSpecificSession(role,
//                userName.get(), new OnResponseCallback<SuccessResponse>() {
//                    @Override
//                    public void onSuccess(SuccessResponse response) {
//                        if (response.getSuccess()){
//
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Exception e) {
//
//                    }
//                });
    }

}
