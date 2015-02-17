package org.edx.mobile.module.db.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.edx.mobile.model.IVideoModel;
import org.edx.mobile.module.db.DatabaseModelFactory;

import java.util.ArrayList;
import java.util.List;

class DbOperationGetVideos extends DbOperationSelect<List<IVideoModel>> {
    
    DbOperationGetVideos(boolean distinct, String table, String[] columns,
            String whereClause, String[] whereArgs, String orderBy) {
        super(distinct, table, columns, whereClause, whereArgs, orderBy);
    }
    
    @Override
    public List<IVideoModel> execute(SQLiteDatabase db) {
        List<IVideoModel> list = new ArrayList<IVideoModel>();
        
        Cursor c = getCursor(db);
        if (c.moveToFirst()) {
            do {
                IVideoModel video = DatabaseModelFactory.getModel(c);
                list.add(video);
            } while (c.moveToNext());
        }
        c.close();
        
        return list;
    }
    
}
