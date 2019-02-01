package org.edx.mobile.tta.data.remote;

import android.content.Context;
import android.widget.Toast;

import org.edx.mobile.tta.ui.base.TaBaseActivity;
import org.edx.mobile.tta.widget.loading.ILoading;


import io.reactivex.Observer;
import io.reactivex.annotations.Nullable;
import io.reactivex.disposables.Disposable;
/**
 * Created by Arjun on 2018/9/18.
 */
public class NetworkObserver<T> implements Observer<T> {
    private static final String TAG = "NetworkObserver";
    private Context mContext;
    private ILoading mLoading;

    public NetworkObserver(Context context) {
        mContext = context;
    }

    public NetworkObserver(Context context, @Nullable ILoading loading) {
        mContext = context;
        mLoading = loading;
    }


    public NetworkObserver(TaBaseActivity activity) {
        mContext = activity;
        mLoading = activity;
    }

    public NetworkObserver(TaBaseActivity activity, Boolean showLoading) {
        mContext = activity;
        if (showLoading) {
            mLoading = activity;
        }
    }


    @Override
    public void onSubscribe(Disposable d) {
        if (mLoading != null) {
            mLoading.showLoading();
        }
    }

    @Override
    public void onNext(T result) {
        onHandleSuccess(result);
    }

    @Override
    public void onError(Throwable e) {

        if (mLoading != null) {
            mLoading.hideLoading();
        }

        String errorMsg = e.getMessage();

        /*if (e instanceof ConnectException) {
            errorMsg = "Can not connect to the server, please try again later.";
        } else if (e instanceof HttpException) {
            int errorCode = ((HttpException) e).code();
            switch (errorCode / 100) {
                case 5:
                    errorMsg = "Server internal error.";
                    break;
                case 4:
                    errorMsg = "URL not found.";
                    break;
                default:
                    errorMsg = "Unknown server";
            }
        } else if (e instanceof SocketTimeoutException) {
            errorMsg = "Connection server timeout";
        } else if (e instanceof RxUtil.UserNotLoginException) {
            ActivityUtil.gotoLogin(mContext);
        } else {
            errorMsg = e.getMessage();
        }*/

        onHandleError(errorMsg);
        onHandleFinal();
    }

    @Override
    public void onComplete() {
        if (mLoading != null) {
            mLoading.hideLoading();
        }
        onHandleFinal();
    }

    protected void onHandleSuccess(T t) {}

    protected void onHandleError(String msg) {
        if (msg == null) {
            msg = "unknown mistake";
        }
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    protected void onHandleFinal() {

    }
}
