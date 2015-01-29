package org.edx.mobile.module.db.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.edx.mobile.model.IVideoModel;
import org.edx.mobile.module.db.DatabaseModelFactory;

class DbOperationGetVideo extends DbOperationSelect<IVideoModel> {
    
    DbOperationGetVideo(boolean distinct, String table, String[] columns,
            String whereClause, String[] whereArgs, String orderBy) {
        super(distinct, table, columns, whereClause, whereArgs, orderBy);
    }
    
    @Override
    public IVideoModel execute(SQLiteDatabase db) {
        IVideoModel video = null;
        
        Cursor c = getCursor(db);
        if (c.moveToFirst()) {
            video = DatabaseModelFactory.getModel(c);
        }
        if (c.moveToNext()) {
            logger.warn("More than one records available that match your query, " +
                    "but you chose only first one");
        }
        c.close();
        
        return video;
    }
    
}
