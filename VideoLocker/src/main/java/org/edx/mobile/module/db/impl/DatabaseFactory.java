package org.edx.mobile.module.db.impl;

import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.db.IDatabase;

import android.content.Context;
import android.util.SparseArray;

/**
 * This class provides singleton instances of database implemention as {@link org.edx.mobile.module.db.IDatabase}.
 * @author rohan
 *
 */
public class DatabaseFactory {
    
    public static final int                 TYPE_DATABASE_NATIVE = 1;
    /* Keep singleton instances in a map, so that multiple db implementations can be handled */
    private static SparseArray<IDatabase>   dbMap = new SparseArray<IDatabase>();
    private static final Logger logger = new Logger(DatabaseFactory.class.getName());

    /**
     * Returns singleton instance of the {@link IDatabase} for the given type.
     * The only supported type is TYPE_DATABASE_NATIVE. 
     * @param context
     * @param type
     * @return
     * @throws IllegalArgumentException if the type is invalid.
     */
    public static IDatabase getInstance(Context context, int type, String username) {
        IDatabaseImpl db = null;
        
        if (type == TYPE_DATABASE_NATIVE) {
            // manage singleton object
            if (dbMap.get(type) == null) {
                db = new IDatabaseImpl(context, username);
                dbMap.put(type, db);
                logger.debug("Database object created");
            }
            
            // update username everytime
            db = (IDatabaseImpl) dbMap.get(type);
            db.setUsername(username);

            return db;
        } 
        
        throw new IllegalArgumentException("Database type " + type + 
                " is not supported");
    }
}
