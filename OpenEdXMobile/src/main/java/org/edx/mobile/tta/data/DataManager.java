package org.edx.mobile.tta.data;

import org.edx.mobile.tta.data.local.db.ILocalDataSource;
import org.edx.mobile.tta.data.local.db.LocalDataSource;
import org.edx.mobile.tta.data.model.BaseResponse;
import org.edx.mobile.tta.data.model.EmptyResponse;
import org.edx.mobile.tta.data.remote.IRemoteDataSource;
import org.edx.mobile.tta.data.remote.RetrofitServiceUtil;
import org.edx.mobile.tta.utils.RxUtil;
import org.edx.mobile.tta.ui.login.model.LoginRequest;
import org.edx.mobile.tta.ui.login.model.LoginResponse;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

/**
 * Created by Arjun on 2018/9/18.
 */

@Singleton
public class DataManager {
    private static DataManager mDataManager;
    private IRemoteDataSource mRemoteDataSource;
    private ILocalDataSource mLocalDataSource;
@Inject
    public DataManager(IRemoteDataSource remoteDataSource, ILocalDataSource localDataSource) {
        mRemoteDataSource = remoteDataSource;
        mLocalDataSource = localDataSource;
    }

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


    public Observable<LoginResponse> login(LoginRequest loginRequest) {
        return preProcess(mRemoteDataSource.login(loginRequest));
    }

    public Observable<EmptyResponse> getEmpty() {
        return preEmptyProcess(mRemoteDataSource.getEmpty());
    }
}

