package org.edx.mobile.tta.wordpress_client.data.tasks.callback;

import org.edx.mobile.tta.wordpress_client.data.tasks.WpAsyncTask;
import org.edx.mobile.tta.wordpress_client.util.LogUtils;

/**
 * @author Arjun Singh
 *         Created on 2016/03/15.
 */
public abstract class SimpleWpTaskCallback<Result> implements WpTaskCallback<Result> {

    @Override
    public void onTaskResultNull() {

    }

    @Override
    public void onTaskCancelled() {

    }

    @Override
    public void onTaskFailure(WpAsyncTask task, String error) {
        LogUtils.w("Task failure for (" + task.getClass().getSimpleName() + ") : " + error);
    }
}
