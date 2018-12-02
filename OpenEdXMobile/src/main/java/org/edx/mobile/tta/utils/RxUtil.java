package org.edx.mobile.tta.utils;

import org.edx.mobile.tta.data.constants.Constants;
import org.edx.mobile.tta.data.model.BaseResponse;

import java.lang.reflect.Constructor;

import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Arjun on 2018/3/14.
 */

public class RxUtil {
    public static <T> ObservableTransformer<T, T> applyScheduler() {
        return upstream -> upstream.subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    @SuppressWarnings("unchecked")
    public static <T> Function<BaseResponse<T>, T> unwrapResponse(Class<T> cls) {
        return baseResponse -> {
            if (baseResponse.getCode().equals(Constants.SUCCESS_CODE)) {
                T data = baseResponse.getData();
                if (data == null) {
                    Constructor<?> constructor;
                    try {
                        constructor = cls.getConstructor();
                        data = (T) constructor.newInstance();
                    } catch (Exception e) {
                        // 没有data返回字段的请求，要求T一定要有default的构造函数
                        throw new WrongResponseException(e.getMessage());
                    }
                }

                return data;
            } else if (baseResponse.getCode().equals(Constants.USER_NOT_LOGGED_IN)) {
                // auth token过期
                throw new UserNotLoginException(baseResponse.getMsg());
            }
            throw new UnknownServerException(baseResponse.getMsg());
        };
    }

    public static class UserNotLoginException extends Exception {
        public UserNotLoginException(String message) {
            super(message);
        }

        public UserNotLoginException() {
            super();
        }
    }

    public static class UnknownServerException extends Exception {
        public UnknownServerException(String message) {
            super(message);
        }
    }

    public static class WrongResponseException extends Exception {
        public WrongResponseException(String message) {
            super(message);
        }
    }
}


