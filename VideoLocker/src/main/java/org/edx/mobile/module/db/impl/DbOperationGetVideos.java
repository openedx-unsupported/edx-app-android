package org.edx.mobile.module.db.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.edx.mobile.model.VideoModel;
import org.edx.mobile.module.db.DatabaseModelFactory;

import java.util.ArrayList;
import java.util.List;

class DbOperationGetVideos extends DbOperationSelect<List<VideoModel>> {
    
    DbOperationGetVideos(boolean distinct, String table, String[] columns,
            String whereClause, String[] whereArgs, String orderBy) {
        super(distinct, table, columns, whereClause, whereArgs, orderBy);
    }
    
    @Override
    public List<VideoModel> execute(SQLiteDatabase db) {
        List<VideoModel> list = new ArrayList<VideoModel>();
        
        Cursor c = getCursor(db);
        if (c.moveToFirst()) {
            do {
                VideoModel video = DatabaseModelFactory.getModel(c);
                list.add(video);
            } while (c.moveToNext());
        }
        c.close();
        
        return list;
    }
    
    @Override
    public List<VideoModel> getDefaultValue() {
        return new ArrayList<VideoModel>();
    }
    
}
