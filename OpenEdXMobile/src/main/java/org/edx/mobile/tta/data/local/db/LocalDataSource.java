package org.edx.mobile.tta.data.local.db;

import org.edx.mobile.tta.data.local.db.table.User;
import org.edx.mobile.tta.data.model.ConfigurationResponse;
import org.edx.mobile.tta.data.local.db.table.Content;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;

/**
 * Created by Arjun on 2018/9/18.
 */
public class LocalDataSource implements ILocalDataSource {

    private final TADatabase mAppDatabase;

    public LocalDataSource(TADatabase appDatabase) {
        this.mAppDatabase = appDatabase;
    }

    @Override
    public Observable<List<User>> getAllUsers() {
        return Observable.fromCallable(new Callable<List<User>>() {
            @Override
            public List<User> call() throws Exception {
                return mAppDatabase.userDao().getAll();
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

    @Override
    public ConfigurationResponse getConfiguration() {
        ConfigurationResponse response = new ConfigurationResponse();
        response.setCategory(mAppDatabase.categoryDao().getAll());
        response.setList(mAppDatabase.contentListDao().getAll());
        response.setSource(mAppDatabase.sourceDao().getAll());

        return response;
    }

    @Override
    public void insertConfiguration(ConfigurationResponse response) {
        mAppDatabase.categoryDao().insert(response.getCategory());
        mAppDatabase.contentListDao().insert(response.getList());
        mAppDatabase.sourceDao().insert(response.getSource());
    }

    @Override
    public List<Content> getContents() {
        return mAppDatabase.contentDao().getAll();
    }

    @Override
    public void insertContents(List<Content> contents) {
        mAppDatabase.contentDao().insert(contents);
    }
}
