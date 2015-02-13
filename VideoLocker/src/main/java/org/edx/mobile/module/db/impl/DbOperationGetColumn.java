package org.edx.mobile.module.db.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

class DbOperationGetColumn<T> extends DbOperationSelect<List<T>> {
    
    private Class<T> columnType;
    
    DbOperationGetColumn(boolean distinct, String table, String[] columns,
            String whereClause, String[] whereArgs, String orderBy, Class<T> columnType) {
        super(distinct, table, columns, whereClause, whereArgs, orderBy);
        this.columnType = columnType;
    }
    
    @Override
    public List<T> execute(SQLiteDatabase db) {
        List<T> list = new ArrayList<T>();
        
        Cursor c = getCursor(db);
        if (c.moveToFirst()) {
            do {
                if (columnType == Long.class) { 
                    Long column = c.getLong(0);
                    list.add((T) column);
                } else if (columnType == String.class) {
                    String column = c.getString(0);
                    list.add((T) column);
                } else if (columnType == Integer.class) {
                    Integer column = c.getInt(0);
                    list.add((T) column);
                } else {
                    logger.warn("Class types does NOT match for: " + columnType);
                }
            } while (c.moveToNext());
        }
        c.close();
        
        return list;
    }
    
}
