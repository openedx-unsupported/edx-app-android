package org.edx.mobile.module.db.impl;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

class DbOperationSingleValueByRawQuery<T> extends DbOperationBase<T> {
    
    private String sqlQuery;
    private String[] selectionArgs;
    private Class<T> columnType;
    
    DbOperationSingleValueByRawQuery(String sqlQuery, String[] selectionArgs, Class<T> valueType) {
        this.sqlQuery = sqlQuery;
        this.selectionArgs = selectionArgs;
        this.columnType = valueType;
    }
    
    @Override
    public T execute(SQLiteDatabase db) {
        Cursor c = db.rawQuery(sqlQuery, selectionArgs);
        
        if (c.moveToFirst()) {
            if (columnType == Long.class) { 
                Long column = c.getLong(0);
                return (T) column;
            } else if (columnType == String.class) {
                String column = c.getString(0);
                return (T) column;
            } else if (columnType == Integer.class) {
                Integer column = c.getInt(0);
                return (T) column;
            } else {
                logger.warn("Class types does NOT match for: " + columnType);
            }
        }
        
        c.close();
        
        return null;
    }
    
}
