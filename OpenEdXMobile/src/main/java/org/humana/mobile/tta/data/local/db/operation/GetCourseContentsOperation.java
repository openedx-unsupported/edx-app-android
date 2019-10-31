package org.humana.mobile.tta.data.local.db.operation;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.humana.mobile.model.db.DownloadEntry;
import org.humana.mobile.module.db.DbStructure;
import org.humana.mobile.module.db.impl.DbOperationSelect;
import org.humana.mobile.tta.data.enums.DownloadType;
import org.humana.mobile.util.Sha1Util;

import java.util.ArrayList;
import java.util.List;

import static org.humana.mobile.util.BrowserUtil.loginPrefs;

public class GetCourseContentsOperation extends DbOperationSelect<List<Long>> {
    public GetCourseContentsOperation() {
        super(true, DbStructure.Table.DOWNLOADS, new String[]{DbStructure.Column.CONTENT_ID},
                DbStructure.Column.USERNAME + "=? AND "
                        + "(" + DbStructure.Column.TYPE + "=? OR " + DbStructure.Column.TYPE + "=?) AND "
                + DbStructure.Column.DOWNLOADED + "=?",
                new String[]{Sha1Util.SHA1(loginPrefs.getUsername()), DownloadType.SCORM.name(), DownloadType.PDF.name(),
                        String.valueOf(DownloadEntry.DownloadedState.DOWNLOADED.ordinal())},
                DbStructure.Column.CONTENT_ID, null, null);
    }

    @Override
    public List<Long> execute(SQLiteDatabase db) {
        List<Long> contentIds = new ArrayList<>();

        Cursor c = getCursor(db);
        if (c.moveToFirst()) {
            do {
                contentIds.add(c.getLong(c.getColumnIndex(DbStructure.Column.CONTENT_ID)));
            } while (c.moveToNext());
        }
        c.close();
        return contentIds;
    }

    @Override
    public List<Long> getDefaultValue() {
        return null;
    }
}
