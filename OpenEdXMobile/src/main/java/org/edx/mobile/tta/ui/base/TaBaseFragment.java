package org.edx.mobile.tta.ui.base;

import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.ViewGroup;

import org.edx.mobile.BR;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.tta.analytics.Analytic;
import org.edx.mobile.tta.interfaces.PermissionListener;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;
import org.edx.mobile.tta.utils.RxV4Fragment;
import org.edx.mobile.util.PermissionsUtil;
import org.edx.mobile.view.common.PageViewStateCallback;
import org.edx.mobile.view.dialog.AlertDialogFragment;

public abstract class TaBaseFragment extends RxV4Fragment
        implements PermissionListener, PageViewStateCallback {
    protected ViewDataBinding mBinding;
    protected PermissionListener permissionListener;
    private BaseViewModel viewModel;
    protected final Logger logger = new Logger(getClass().getName());
    public Analytic analytic;

    private int requestCode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        analytic = new Analytic(getActivity());
    }

    protected ViewDataBinding binding(LayoutInflater inflater, ViewGroup container, int layoutId, BaseViewModel viewModel) {
        mBinding = DataBindingUtil.inflate(inflater, layoutId, container, false);
        mBinding.setVariable(BR.viewModel, viewModel);
        this.viewModel = viewModel;
        return mBinding;
    }

    public void showErrorDialog(@Nullable String title, @NonNull String message) {
        AlertDialogFragment.showDialog(getFragmentManager(),title, message);
    }

    public void showShortSnack(String msg){
        Snackbar.make(getView(), msg, Snackbar.LENGTH_SHORT).show();
    }

    public void logD(String msg){
        logger.debug(msg);
    }

    public void askForPermissions(String[] permissions, int requestCode) {
        this.requestCode = requestCode;
        if (getActivity() != null) {
            if (permissionListener != null && getGrantedPermissionsCount(permissions) == permissions.length) {
                permissionListener.onPermissionGranted(permissions, requestCode);
            } else {
                PermissionsUtil.requestPermissions(requestCode, permissions, this);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == this.requestCode && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            onPermissionGranted(permissions, requestCode);
        } else {
            onPermissionDenied(permissions, requestCode);
        }
    }

    public int getGrantedPermissionsCount(String[] permissions) {
        int grantedPermissionsCount = 0;
        for (String permission : permissions) {
            if (PermissionsUtil.checkPermissions(permission, getActivity())) {
                grantedPermissionsCount++;
            }
        }

        return grantedPermissionsCount;
    }

    @Override
    public void onPermissionGranted(String[] permissions, int requestCode) {

    }

    @Override
    public void onPermissionDenied(String[] permissions, int requestCode) {

    }

    @Override
    public void onPageShow() {

    }

    @Override
    public void onPageDisappear() {

    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.onResume();
    }


}