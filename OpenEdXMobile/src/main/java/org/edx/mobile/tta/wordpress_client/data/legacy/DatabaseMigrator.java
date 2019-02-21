package org.edx.mobile.tta.wordpress_client.data.legacy;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Arjun Singh
 *         Created on 2016/04/01.
 */
public class DatabaseMigrator extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "reporter.db";

    private static final int DATABASE_VERSION = 201;

    private static final String TABLE_MEDIAS = "medias";

    public DatabaseMigrator(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public Cursor getMediasCursor() {
        SQLiteDatabase db = getReadableDatabase();

        return db.query(TABLE_MEDIAS, null, null, null, null, null, null);
    }

    public void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }
}
