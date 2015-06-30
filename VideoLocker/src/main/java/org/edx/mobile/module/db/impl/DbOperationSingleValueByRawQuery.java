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
        
        T result = null;
        if (c.moveToFirst()) {
            if (columnType == Long.class) { 
                Long column = c.getLong(0);
                result = (T) column;
            } else if (columnType == String.class) {
                String column = c.getString(0);
                result = (T) column;
            } else if (columnType == Integer.class) {
                Integer column = c.getInt(0);
                result = (T) column;
            } else {
                logger.warn("Class types does NOT match for: " + columnType);
            }
        }
        
        c.close();
        
        return result;
    }
    
    @Override
    public T getDefaultValue() {
        if (columnType == Long.class) {
            return (T) (Long) (-1L);
        } else if (columnType == String.class) {
            return (T) "";
        } else if (columnType == Integer.class) {
            return (T) (Integer) (-1);
        } else {
            logger.warn("Class types does NOT match for: " + columnType);
        }

        return null;
    }

}
