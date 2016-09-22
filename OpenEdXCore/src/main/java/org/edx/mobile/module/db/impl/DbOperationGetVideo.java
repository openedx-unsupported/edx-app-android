package org.edx.mobile.module.db.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.edx.mobile.model.VideoModel;
import org.edx.mobile.module.db.DatabaseModelFactory;

class DbOperationGetVideo extends DbOperationSelect<VideoModel> {
    
    DbOperationGetVideo(boolean distinct, String table, String[] columns,
            String whereClause, String[] whereArgs, String orderBy) {
        super(distinct, table, columns, whereClause, whereArgs, orderBy);
    }
    
    @Override
    public VideoModel execute(SQLiteDatabase db) {
        VideoModel video = null;
        
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
    
    @Override
    public VideoModel getDefaultValue() {
        // Returning null should be fine here, as video should only be queried if
        // it exists, or at least there should be a null check on the client code
        // otherwise. If we want to return an empty object here, then we will
        // need an appropriate constructor or initializer in the default
        // VideoModel implementation as well.
        return null;
    }
    
}
