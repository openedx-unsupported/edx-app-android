package org.edx.mobile.tta.wordpress_client.data.tasks;

import android.content.Context;
import android.database.Cursor;

import org.edx.mobile.tta.wordpress_client.data.WordPressDatabase;
import org.edx.mobile.tta.wordpress_client.data.tasks.callback.WpTaskCallback;

/**
 * @author Arjun Singh
 *         Created on 2016/02/15.
 */
public abstract class WpQueryCustomTask<Result> extends WpAsyncTask<Void, Void, Result> {

    private boolean distinct;
    private String table;
    private String[] projection;
    private String selection;
    private String[] selectionArgs;
    private String groupBy;
    private String having;
    private String orderBy;
    private String limit;

    public WpQueryCustomTask(Context context, String table, String[] projection, String selection, String[] selectionArgs, WpTaskCallback<Result> callback) {
        this(context, false, table, projection, selection, selectionArgs, null, null, null, null, callback);
    }

    public WpQueryCustomTask(Context context, boolean distinct, String table, String[] projection, String selection, String[] selectionArgs,
                             String groupBy, String having, String orderBy, String limit, WpTaskCallback<Result> callback) {
        super(context, callback);

        database = WordPressDatabase.getInstance(context);

        this.distinct = distinct;
        this.table = table;
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.groupBy = groupBy;
        this.having = having;
        this.orderBy = orderBy;
        this.limit = limit;
    }

    @Override
    protected Result exec() throws Exception {
        Cursor cursor = getReadableDatabase().query(distinct, table, projection, selection, selectionArgs,
                groupBy, having, orderBy, limit);

        if (cursor != null) {
            Result result = makeResult(cursor);

            cursor.close();

            return result;
        }

        return null;
    }

    protected abstract Result makeResult(Cursor cursor);
}
