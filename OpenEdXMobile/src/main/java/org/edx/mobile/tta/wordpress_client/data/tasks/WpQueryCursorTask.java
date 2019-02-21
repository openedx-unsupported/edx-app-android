package org.edx.mobile.tta.wordpress_client.data.tasks;

import android.content.Context;
import android.database.Cursor;

import org.edx.mobile.tta.wordpress_client.data.WordPressDatabase;
import org.edx.mobile.tta.wordpress_client.data.tasks.callback.WpTaskCallback;

/**
 * @author Arjun Singh
 *         Created on 2016/02/11.
 */
public class WpQueryCursorTask extends WpAsyncTask<Void, Void, Cursor> {

    private boolean distinct;
    private String table;
    private String[] projection;
    private String selection;
    private String[] selectionArgs;
    private String groupBy;
    private String having;
    private String orderBy;
    private String limit;

    public WpQueryCursorTask(Context context, String table, String[] projection, String selection, String[] selectionArgs, WpTaskCallback<Cursor> callback) {
        this(context, false, table, projection, selection, selectionArgs, null, null, null, null, callback);
    }

    //projects ==collumns string  //selection==where cause
    public WpQueryCursorTask(Context context, boolean distinct, String table, String[] projection, String selection,
                             String[] selectionArgs, String groupBy, String having, String orderBy, String limit, WpTaskCallback<Cursor> callback) {
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
    protected Cursor exec() throws Exception {
        return getReadableDatabase().query(distinct, table, projection, selection, selectionArgs,
                groupBy, having, orderBy, limit);
    }
}
