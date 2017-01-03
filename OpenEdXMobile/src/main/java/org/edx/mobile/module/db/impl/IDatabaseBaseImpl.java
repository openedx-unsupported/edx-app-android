package org.edx.mobile.module.db.impl;

import android.content.Context;
import android.database.sqlite.SQLiteException;

import org.edx.mobile.logger.Logger;

import java.util.LinkedList;
import java.util.Queue;

class IDatabaseBaseImpl implements Runnable {
    protected static final Logger logger = new Logger(IDatabaseBaseImpl.class.getName());
    protected Context context;
    private DbHelper helper;
    private Queue<IDbOperation<?>> opQueue = new LinkedList<IDbOperation<?>>();
    private boolean isQueueProcessing = false;

    public IDatabaseBaseImpl(Context context) {
        this.context = context;
        helper = new DbHelper(context);
    }

    @Override
    public void run() {
        if (isQueueProcessing) {
            // queue is already being processed, so return
            // this will NOT allow multiple threads to process operation queue
            return;
        }

        do {
            // mark queue being processed
            isQueueProcessing = true;
            IDbOperation<?> op = getNextQueuedOperation();
            if (op == null) {
                break;
            }
            // perform the database operation
            execute(op);
        } while (true);

        // mark queue not being processed
        isQueueProcessing = false;
        // logger.debug("All database operations completed, queue is empty");
    }

    /**
     * Executes given database operation. This is a blocking call.
     * Returns result of the operation.
     *
     * @param op
     * @return
     */
    private synchronized <T> T execute(IDbOperation<T> op) {
        // perform this database operation
        synchronized (helper) {
            T result;
            try {
                result = op.requestExecute(helper.getDatabase());
            } catch (SQLiteException e) {
                /* Catch any SQLite exceptions thrown by the operation, or by the database creation
                 * or upgrade process invoked by the helper, deliver the exception to the callback,
                 * log it in Crashlytics, and return the default value of the operation.
                 */
                op.getCallback().sendException(e);
                logger.error(e, true);
                result = op.getDefaultValue();
            }

            return result;
        }
    }

    /**
     * Enqueues given database operation to the operation queue and starts processing the queue,
     * if not already started.
     * Operation is executed in a queue in background thread if callback is provided for the
     * operation and this method returns null. Otherwise this is a blocking call and returns
     * result object.
     *
     * @param operation
     */
    public synchronized <T> T enqueue(IDbOperation<T> operation) {
        // execute right away if this operation doesn't have a callback to send back the result
        if (operation.getCallback() == null) {
            return execute(operation);
        }

        // add non-blocking operations to the queue and process in sequence 
        synchronized (opQueue) {
            opQueue.add(operation);
        }

        // start processing the queue as we have a database operation to be processed
        new Thread(this).start();

        return null;
    }

    /**
     * Returns and removes the next operation from the operation queue.
     *
     * @return
     */
    private IDbOperation<?> getNextQueuedOperation() {
        synchronized (opQueue) {
            if (opQueue.isEmpty()) {
                return null;
            }

            return opQueue.remove();
        }
    }

    /**
     * Closes this database object.
     */
    public void release() {
        helper.close();
    }

}
