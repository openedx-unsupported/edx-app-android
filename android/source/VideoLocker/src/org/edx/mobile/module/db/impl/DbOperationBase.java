package org.edx.mobile.module.db.impl;

import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.util.LogUtil;

import android.database.sqlite.SQLiteDatabase;

abstract class DbOperationBase<T> implements IDbOperation<T> {
    
    private DataCallback<T> callback;

    @Override
    public final void setCallback(DataCallback<T> callback) {
        this.callback = callback;
    }
    
    @Override
    public DataCallback<T> getCallback() {
        return callback;
    }
    
    @Override
    public T requestExecute(SQLiteDatabase db) {
        try {
            T result = execute(db);
            
            if (callback != null) {
                callback.sendResult(result);
                LogUtil.log(getClass().getName(), "sending result...");
            }
            
            return result;
        } catch(Exception ex) {
            if (callback != null) {
                callback.sendException(ex);
                LogUtil.log(getClass().getName(), "sending error...");
            }
        }
        
        return null;
    }
    
    /**
     * Sub-class should actually execute the database operation and return the result.
     * Exceptions are caught by this class and sub-class don't need to do so.
     * @param db
     * @return
     */
    public abstract T execute(SQLiteDatabase db);
}
