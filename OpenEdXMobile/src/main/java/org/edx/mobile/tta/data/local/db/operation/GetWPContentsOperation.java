package org.edx.mobile.tta.data.local.db.operation;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.DbStructure;
import org.edx.mobile.module.db.impl.DbOperationSelect;
import org.edx.mobile.tta.data.enums.DownloadType;
import org.edx.mobile.util.Sha1Util;

import java.util.ArrayList;
import java.util.List;

import static org.edx.mobile.util.BrowserUtil.loginPrefs;

public class GetWPContentsOperation extends DbOperationSelect<List<Long>> {
    public GetWPContentsOperation(String sourceName) {
        super(true, DbStructure.Table.DOWNLOADS, new String[]{DbStructure.Column.CONTENT_ID},
                DbStructure.Column.USERNAME + "=? AND "
                + DbStructure.Column.TYPE + "=? AND "
                + DbStructure.Column.CHAPTER + "=? AND "
                + DbStructure.Column.DOWNLOADED + "=?",
                new String[]{Sha1Util.SHA1(loginPrefs.getUsername()),
                        DownloadType.WP_VIDEO.name(), sourceName,
                        String.valueOf(DownloadEntry.DownloadedState.DOWNLOADED.ordinal())
                },
                null);
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
