package org.edx.mobile.module.db.impl;

import android.database.sqlite.SQLiteDatabase;

import org.edx.mobile.module.db.DataCallback;

/**
 * This interface defines a database operation that we want to perform.
 * This operation will be enqueued and will be processed by {@link IDatabaseBaseImpl} class.
 * @author rohan
 *
 */
interface IDbOperation<T> {

    /**
     * Performs this database operation on given database object.
     * @param db
     * @return
     */
    T requestExecute(SQLiteDatabase db);
    
    /**
     * Returns the default value of the data type.
     * @return The data type
     */
    T getDefaultValue();
    
    /**
     * Sets a callback for database operation success and failure.
     * @param callback
     */
    void setCallback(DataCallback<T> callback);
    
    /**
     * Returns callback that was previously set using {@link #setCallback(DataCallback)} method.
     * @return
     */
    DataCallback<T> getCallback();
}
