package org.edx.mobile.module.db;

/**
 * This is callback interface for database operations. Each database operation either sends back a result or 
 * fails with an exception. This interface contains both success and failure callback methods.
 * @author rohan
 *
 * @param <T> T - Result object type when database operation succeeds.
 */
public interface IDbCallback<T> {
    
    /**
     * Queue processor calls this method. This method call onResult method.
     * onResult method gets called in the message queue of the thread who created this callback object. 
     * @param result
     */
    void sendResult(T result);
    
    /**
     * Queue processor calls this method. This method call onFail method.
     * onFail method gets called in the message queue of the thread who created this callback object.
     * @param ex
     */
    void sendException(Exception ex);
}
