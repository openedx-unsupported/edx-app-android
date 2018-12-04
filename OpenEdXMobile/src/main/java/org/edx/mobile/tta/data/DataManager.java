package org.edx.mobile.tta.data;

import android.os.Debug;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.edx.mobile.tta.data.local.db.ILocalDataSource;
import org.edx.mobile.tta.data.local.db.LocalDataSource;
import org.edx.mobile.tta.data.model.BaseResponse;
import org.edx.mobile.tta.data.model.EmptyResponse;
import org.edx.mobile.tta.data.pref.AppPref;
import org.edx.mobile.tta.data.remote.IRemoteDataSource;
import org.edx.mobile.tta.data.remote.RetrofitServiceUtil;
import org.edx.mobile.tta.utils.RxUtil;
import org.edx.mobile.tta.ui.login.model.LoginRequest;
import org.edx.mobile.tta.ui.login.model.LoginResponse;


import io.reactivex.Observable;

/**
 * Created by Arjun on 2018/9/18.
 */

@Singleton
public class DataManager {
//    private IRemoteDataSource mRemoteDataSource;
//    private ILocalDataSource mLocalDataSource;

    private AppPref appPref;

    public static class Provider implements com.google.inject.Provider<DataManager>{
//        @Inject IRemoteDataSource remoteDataSource;
//
//        @Inject ILocalDataSource localDataSource;

//        @Inject AppPref appPref;

        @Override
        public DataManager get() {
            Log.d("__________LOG_________", "data manager");
            return new DataManager();
        }
    }

    /*@Inject
    public DataManager() {
        mRemoteDataSource = remoteDataSource;
        mLocalDataSource = localDataSource;
    }*/

/*    public static DataManager getInstance() {
        if (mDataManager == null) {
            synchronized (DataManager.class) {
                if (mDataManager == null) {
                    mDataManager = new DataManager(RetrofitServiceUtil.create(), new LocalDataSource());
                }
            }
        }
        return mDataManager;
    }*/

    public AppPref getAppPref() {
        return appPref;
    }

    private <T> Observable<T> preProcess(Observable<BaseResponse<T>> observable) {
        return observable.compose(RxUtil.applyScheduler())
                .map(RxUtil.unwrapResponse(null));
    }

    private <T> Observable<T> preProcess(Observable<BaseResponse<T>> observable, Class<T> cls) {
        return observable.compose(RxUtil.applyScheduler())
                .map(RxUtil.unwrapResponse(cls));
    }

    private Observable<EmptyResponse> preEmptyProcess(Observable<BaseResponse<EmptyResponse>> observable) {
        return preProcess(observable, EmptyResponse.class);
    }


    /*public Observable<LoginResponse> login(LoginRequest loginRequest) {
        return preProcess(mRemoteDataSource.login(loginRequest));
    }

    public Observable<EmptyResponse> getEmpty() {
        return preEmptyProcess(mRemoteDataSource.getEmpty());
    }*/
}

