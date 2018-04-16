package org.edx.mobile.module.db.impl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.DbStructure;
import org.edx.mobile.util.AppConstants;
import org.edx.mobile.util.FileUtil;
import org.edx.mobile.util.Sha1Util;
import org.edx.mobile.util.TextUtils;

import java.io.File;
import java.util.Arrays;

/**
 * This class is an implementation of {@link SQLiteOpenHelper} and handles
 * database upgrades.
 * @author rohan
 *
 */
class DbHelper extends SQLiteOpenHelper {
    private SQLiteDatabase sqliteDb;
    private Context context;
    protected final Logger logger = new Logger(getClass().getName());

    public DbHelper(Context context) {
        super(context, DbStructure.NAME, null, DbStructure.VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE "                        + DbStructure.Table.DOWNLOADS
                + " ("
                + DbStructure.Column.ID                     + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DbStructure.Column.USERNAME               + " TEXT, "
                + DbStructure.Column.VIDEO_ID               + " TEXT, "
                + DbStructure.Column.TITLE                  + " TEXT, "
                + DbStructure.Column.SIZE                   + " TEXT, "
                + DbStructure.Column.DURATION               + " LONG, "
                + DbStructure.Column.FILEPATH               + " TEXT, "
                + DbStructure.Column.URL                    + " TEXT, "
                + DbStructure.Column.URL_HLS                + " TEXT, "
                + DbStructure.Column.URL_HIGH_QUALITY       + " TEXT, "
                + DbStructure.Column.URL_LOW_QUALITY        + " TEXT, "
                + DbStructure.Column.URL_YOUTUBE            + " TEXT, "
                + DbStructure.Column.WATCHED                + " INTEGER, "
                + DbStructure.Column.DOWNLOADED             + " INTEGER, "
                + DbStructure.Column.DM_ID                  + " INTEGER, "
                + DbStructure.Column.EID                    + " TEXT, "
                + DbStructure.Column.CHAPTER                + " TEXT, "
                + DbStructure.Column.SECTION                + " TEXT, "
                + DbStructure.Column.DOWNLOADED_ON          + " INTEGER, "
                + DbStructure.Column.LAST_PLAYED_OFFSET     + " INTEGER, "
                + DbStructure.Column.IS_COURSE_ACTIVE       + " BOOLEAN, "
                + DbStructure.Column.UNIT_URL               + " TEXT, "
                + DbStructure.Column.VIDEO_FOR_WEB_ONLY     + " BOOLEAN "
                + ")";
        db.execSQL(sql);

        createAssessmentTable(db);

        logger.debug("Database created");
    }

    private void createAssessmentTable(SQLiteDatabase db){
        String sql = "CREATE TABLE "                        + DbStructure.Table.ASSESSMENT
            + " ("
            + DbStructure.Column.ASSESSMENT_TB_ID       + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + DbStructure.Column.ASSESSMENT_TB_USERNAME + " TEXT, "
            + DbStructure.Column.ASSESSMENT_TB_UNIT_ID  + " TEXT, "
            + DbStructure.Column.ASSESSMENT_TB_UNIT_WATCHED + " BOOLEAN "
            + ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String upgradeToV2 =
                "ALTER TABLE " + DbStructure.Table.DOWNLOADS + " ADD COLUMN "
                        + DbStructure.Column.UNIT_URL + " TEXT ";

        String[] upgradeToV3 = new String[]{
                "ALTER TABLE " + DbStructure.Table.DOWNLOADS + " ADD COLUMN "
                        + DbStructure.Column.URL_HIGH_QUALITY + " TEXT ",

                "ALTER TABLE " + DbStructure.Table.DOWNLOADS + " ADD COLUMN "
                        + DbStructure.Column.URL_LOW_QUALITY + " TEXT ",

                "ALTER TABLE " + DbStructure.Table.DOWNLOADS + " ADD COLUMN "
                        + DbStructure.Column.URL_YOUTUBE + " TEXT "};

        String upgradeToV4 =
                "ALTER TABLE " + DbStructure.Table.DOWNLOADS + " ADD COLUMN "
                        + DbStructure.Column.VIDEO_FOR_WEB_ONLY + " BOOLEAN ";

        String upgradeToV7 = "ALTER TABLE " + DbStructure.Table.DOWNLOADS + " ADD COLUMN "
                        + DbStructure.Column.URL_HLS + " TEXT ";

        if (oldVersion == 1) {
            // upgrade from 1 to 2
            db.execSQL(upgradeToV2);
        }

        if (oldVersion < 3) {
            // upgrade to version 3
            for (String query : upgradeToV3) {
                db.execSQL(query);
            }
        }

        if (oldVersion < 4) {
            // upgrade to version 4
            db.execSQL(upgradeToV4);
        }

        if (oldVersion < 5) {
            createAssessmentTable(db);
        }

        if (oldVersion < 6) {
            db.beginTransaction();
            try {
                final File externalAppDir = FileUtil.getExternalAppDir(context);
                final String previousAppDirPath = TextUtils.join("/", Arrays.<CharSequence>asList(
                        Environment.getExternalStorageDirectory().getAbsolutePath(),
                        "Android", "data", context.getPackageName())).toString();
                if (externalAppDir != null) {
                    Cursor cursor = db.query(false, DbStructure.Table.DOWNLOADS,
                            new String[]{DbStructure.Column.ID, DbStructure.Column.USERNAME,
                                    DbStructure.Column.FILEPATH}, null, null, null, null, null, null);
                    if (cursor != null) {
                        try {
                            final int idIndex = cursor.getColumnIndexOrThrow(DbStructure.Column.ID);
                            final int usernameIndex = cursor.getColumnIndexOrThrow(DbStructure.Column.USERNAME);
                            final int filePathIndex = cursor.getColumnIndexOrThrow(DbStructure.Column.FILEPATH);

                            while (cursor.moveToNext()) {
                                final String id = cursor.getString(idIndex);
                                final String username = cursor.getString(usernameIndex);
                                final String filePath = cursor.getString(filePathIndex);
                                final String hashedUsername = Sha1Util.SHA1(username);

                                final String previousDirPath = TextUtils.join(
                                        "/", Arrays.<CharSequence>asList(previousAppDirPath,
                                                username)) + "/";
                                // filePath is null when a video is downloading
                                if (filePath == null || !filePath.startsWith(previousDirPath)) {
                                    db.delete(DbStructure.Table.DOWNLOADS,
                                            DbStructure.Column.ID + "= ?", new String[]{id});
                                    continue;
                                }

                                final String newFilePath = filePath.replaceFirst(
                                        "^" + previousDirPath,
                                        TextUtils.join("/", Arrays.<CharSequence>asList(
                                                externalAppDir.getAbsolutePath(),
                                                AppConstants.Directories.VIDEOS,
                                                hashedUsername)) + "/");

                                // First update the name and path of the videos directory
                                final File previousDir = new File(previousAppDirPath, username);
                                if (previousDir.exists()) {
                                    final File newDir = new File(externalAppDir,
                                            TextUtils.join("/", Arrays.<CharSequence>asList(
                                                    AppConstants.Directories.VIDEOS,
                                                    hashedUsername)).toString());
                                    if (!((newDir.mkdirs() || newDir.exists()) &&
                                            previousDir.renameTo(newDir))) {
                                        continue;
                                    }
                                }

                                // Then update the database row
                                final ContentValues updatedValues = new ContentValues();
                                updatedValues.put(DbStructure.Column.USERNAME, hashedUsername);
                                updatedValues.put(DbStructure.Column.FILEPATH, newFilePath);
                                db.update(DbStructure.Table.DOWNLOADS, updatedValues,
                                        DbStructure.Column.ID + "= ?", new String[]{id});
                            }
                        } finally {
                            cursor.close();
                        }

                        // Now migrate the subtitles directory
                        final File previousSrtDir = new File(externalAppDir, "srtFolder");
                        if (previousSrtDir.exists()) {
                            final File newSrtDir = new File(externalAppDir,
                                    AppConstants.Directories.VIDEOS + "/" + AppConstants.Directories.SUBTITLES);
                            newSrtDir.mkdirs();
                            previousSrtDir.renameTo(newSrtDir);
                        }
                    }
                    db.setTransactionSuccessful();
                    logger.debug("Database upgraded from " + oldVersion + " to " + newVersion);
                }

            } finally {
                db.endTransaction();
            }
        }

        if (oldVersion < 7) {
            // upgrade to version 7
            db.execSQL(upgradeToV7);
            // delete all old videos with ONLINE state to make sure that HLS video encoding is with them when they are saved again.
            db.delete(DbStructure.Table.DOWNLOADS,
                    DbStructure.Column.DOWNLOADED + "=?",
                    new String[]{String.valueOf(DownloadEntry.DownloadedState.ONLINE.ordinal())});
        }

    }

    /**
     * Returns singleton writable {@link SQLiteDatabase} object.
     * @return
     */
    public SQLiteDatabase getDatabase() {
        if (sqliteDb == null) {
            sqliteDb = this.getWritableDatabase();
            logger.debug("Writable database handle opened");
        }
        return sqliteDb;
    }

    @Override
    public synchronized void close() {
        super.close();

        sqliteDb = null;
        logger.debug("Database closed");
    }
}
