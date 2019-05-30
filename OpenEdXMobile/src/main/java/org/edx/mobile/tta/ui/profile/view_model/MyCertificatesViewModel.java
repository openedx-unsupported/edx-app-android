package org.edx.mobile.tta.ui.profile.view_model;

import android.content.Context;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.bumptech.glide.Glide;
import com.maurya.mx.mxlib.core.MxInfiniteAdapter;
import com.maurya.mx.mxlib.core.OnRecyclerItemClickListener;

import org.edx.mobile.R;
import org.edx.mobile.databinding.TRowCertificateBinding;
import org.edx.mobile.tta.data.local.db.table.Certificate;
import org.edx.mobile.tta.exception.TaException;
import org.edx.mobile.tta.interfaces.OnResponseCallback;
import org.edx.mobile.tta.ui.base.TaBaseFragment;
import org.edx.mobile.tta.ui.base.mvvm.BaseViewModel;

import java.util.List;

public class MyCertificatesViewModel extends BaseViewModel {

    public CertificatesAdapter adapter;
    public RecyclerView.LayoutManager layoutManager;

    public MyCertificatesViewModel(Context context, TaBaseFragment fragment) {
        super(context, fragment);

        adapter = new CertificatesAdapter(mActivity);
        layoutManager = new GridLayoutManager(mActivity, 2);

        adapter.setItemClickListener((view, item) -> {
            String url = mDataManager.getConfig().getApiHostURL() + item.getDownload_url();
            mDataManager.getEdxEnvironment().getRouter().showAuthenticatedWebviewActivity(
                    mActivity, url, item.getCourse_name()
            );
        });

        fetchCertificates();
    }

    private void fetchCertificates() {
        mActivity.showLoading();
        mDataManager.getMyCertificatesFromLocal(new OnResponseCallback<List<Certificate>>() {
            @Override
            public void onSuccess(List<Certificate> data) {
                mActivity.hideLoading();
                adapter.setItems(data);
            }

            @Override
            public void onFailure(Exception e) {
                mActivity.hideLoading();
                mActivity.showLongSnack(e.getLocalizedMessage());
            }
        }, new TaException("Certificates not available"));

    }

    public class CertificatesAdapter extends MxInfiniteAdapter<Certificate> {
        public CertificatesAdapter(Context context) {
            super(context);
        }

        @Override
        public void onBind(@NonNull ViewDataBinding binding, @NonNull Certificate model, @Nullable OnRecyclerItemClickListener<Certificate> listener) {
            if (binding instanceof TRowCertificateBinding) {
                TRowCertificateBinding certificateBinding = (TRowCertificateBinding) binding;
                certificateBinding.contentTitle.setText(model.getCourse_name());
                Glide.with(getContext())
                        .load(mDataManager.getConfig().getApiHostURL() + model.getImage())
                        .placeholder(R.drawable.placeholder_course_card_image)
                        .into(certificateBinding.contentImage);

                certificateBinding.getRoot().setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(v, model);
                    }
                });

            }
        }
    }
}
