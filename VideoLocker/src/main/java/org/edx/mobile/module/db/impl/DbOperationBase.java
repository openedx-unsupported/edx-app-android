package org.edx.mobile.module.db.impl;

import android.database.sqlite.SQLiteDatabase;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.db.DataCallback;

abstract class DbOperationBase<T> implements IDbOperation<T> {
    
    private DataCallback<T> callback;
    protected static final Logger logger = new Logger(DbOperationBase.class.getName());

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
                logger.debug("sending result...");
            }
            
            return result;
        } catch(Exception ex) {
            if (callback != null) {
                callback.sendException(ex);
                logger.debug("sending error...");
                logger.error(ex);
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
