package org.edx.mobile.tta.wordpress_client.data.tasks;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import org.edx.mobile.tta.wordpress_client.data.WordPressDatabase;
import org.edx.mobile.tta.wordpress_client.data.tasks.callback.WpTaskCallback;

/**
 * @author Arjun Singh
 *         Created on 2016/02/11.
 */
public class WpInsertTask extends WpAsyncTask<Void, Void, Long> {

    private String table;
    private ContentValues values;
    private  Context ctx;

    public WpInsertTask(Context context, String table, ContentValues values, WpTaskCallback<Long> callback) {
        super(context, callback);

        this.table = table;
        this.values = values;
        ctx=context;
    }

    @Override
    protected Long exec() throws Exception {

        SQLiteDatabase db;
        WordPressDatabase wordPressDatabase=new WordPressDatabase(context);
        db= wordPressDatabase.getWritableDatabase();
        //SQLiteDatabase db = getWritableDatabase();

        return db.insert(table, null, values);
    }
}
