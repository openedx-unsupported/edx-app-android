package org.edx.mobile.tta.wordpress_client.data.tasks;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.edx.mobile.tta.wordpress_client.data.WordPressDatabase;

import java.util.concurrent.Callable;

/**
 * Created by JARVICE on 06-12-2017.
 */

public class WpQueryCursorSyncTask implements Callable {

    private boolean distinct;
    private String table;
    private String[] projection;
    private String selection;
    private String[] selectionArgs;
    private String groupBy;
    private String having;
    private String orderBy;
    private String limit;
    private Context mContext;
    private SQLiteOpenHelper database;

    public WpQueryCursorSyncTask(Context context, boolean distinct, String table, String[] projection, String selection,
                                 String[] selectionArgs, String groupBy, String having, String orderBy, String limit) {
        this.distinct =distinct;
        this.table = table;
        this.projection = projection;
        this.selection =selection ;
        this.selectionArgs =selectionArgs ;
        this.groupBy = groupBy;
        this.having=having;
        this.orderBy =orderBy ;
        this.limit =limit ;
        this.mContext=context;
        database = WordPressDatabase.getInstance(context);
    }

    @Override
    public Cursor call() {
        Cursor cursor=null;
        try {
            Cursor  mcursor= database.getReadableDatabase().query(distinct, table, projection, selection, selectionArgs,
                    groupBy, having, orderBy, limit);
            return mcursor;
        } catch (Exception ex) {
            Log.d("wpGetTask","Worpress database sync task crashing",ex);
        }
        return cursor;
    }
}