package org.edx.mobile.tta.data.local.db;

import org.edx.mobile.tta.data.local.db.table.User;
import java.util.List;
import java.util.concurrent.Callable;
import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

/**
 * Created by Arjun on 2018/9/18.
 */
@Singleton
public class LocalDataSource implements ILocalDataSource {

    private final TADatabase mAppDatabase;

    /*public static class RoomDbProvider implements com.google.inject.Provider<TADatabase>
    {
        private  Context mCtx;
        RoomDbProvider (Context ctx)
        {
            this.mCtx=ctx;
        }

        @Override
        public TADatabase get() {
            return Room.databaseBuilder(mCtx, TADatabase.class, "dbasfsfs").fallbackToDestructiveMigration()
                    .build();
        }
    }*/

    @Inject
    public LocalDataSource(TADatabase appDatabase) {
        this.mAppDatabase = appDatabase;
    }

    @Override
    public Observable<List<User>> getAllUsers() {
        return Observable.fromCallable(new Callable<List<User>>() {
            @Override
            public List<User> call() throws Exception {
                return mAppDatabase.userDao().loadAll();
            }
        });
    }

    @Override
    public Observable<Boolean> insertUser(final User user) {
        return Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                mAppDatabase.userDao().insert(user);
                return true;
            }
        });
    }
}
