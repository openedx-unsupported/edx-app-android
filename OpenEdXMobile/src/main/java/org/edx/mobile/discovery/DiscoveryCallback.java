package org.edx.mobile.discovery;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

import org.edx.mobile.discovery.model.ActionError;
import org.edx.mobile.discovery.model.ResponseError;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Abstract class used for all Discourse Api Callbacks
 *
 * @param <T>
 */

public abstract class DiscoveryCallback<T> implements Callback<T> {
    private ResponseError mError;

    protected abstract void onResponse(@NonNull final T responseBody);

    protected void onFailure(ResponseError responseError, @NonNull final Throwable error) {
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (response.isSuccessful()) {
            onResponse(response.body());
        } else if (response.code() == 422) {
            //special case handle response code 422 when creating post or topic or message
            mError = new ResponseError(response);
            try {
                ActionError error = new Gson().fromJson(response.errorBody().string(), ActionError.class);
                if (error.getErrors() != null && error.getErrors().length > 0) {
                    mError.setMsg(error.getErrors()[0]);
                }
                onFailure(call, mError);
            } catch (Exception e) {
                onFailure(call, mError);
            }
        } else {
            mError = new ResponseError(response);
            onFailure(call, mError);
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable t) {
        //  mError=new ResponseError(null);
        onFailure(mError, t);

    }
}

