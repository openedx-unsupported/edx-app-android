package org.edx.mobile.tta.data.local.db;

import org.edx.mobile.tta.data.local.db.table.Category;
import org.edx.mobile.tta.data.local.db.table.ContentList;
import org.edx.mobile.tta.data.local.db.table.Feed;
import org.edx.mobile.tta.data.local.db.table.User;
import org.edx.mobile.tta.data.local.db.table.Content;
import org.edx.mobile.tta.data.model.library.CollectionConfigResponse;

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
    public void clear() {
        mAppDatabase.clearAllTables();
    }

    @Override
    public CollectionConfigResponse getConfiguration() {
        CollectionConfigResponse response = new CollectionConfigResponse();
        response.setCategory(mAppDatabase.categoryDao().getAll());
        response.setContent_list(mAppDatabase.contentListDao().getAll());
        response.setSource(mAppDatabase.sourceDao().getAll());

        return response;
    }

    @Override
    public void insertConfiguration(CollectionConfigResponse response) {
        mAppDatabase.categoryDao().insert(response.getCategory());
        mAppDatabase.contentListDao().insert(response.getContent_list());
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

    @Override
    public List<Feed> getFeeds() {
        return mAppDatabase.feedDao().getAll();
    }

    @Override
    public void insertFeeds(List<Feed> feeds) {
        mAppDatabase.feedDao().insert(feeds);
    }

    @Override
    public Category getCategoryBySourceId(long sourceId) {
        return mAppDatabase.categoryDao().getBySourceId(sourceId);
    }

    @Override
    public List<ContentList> getContentListsByCategoryId(long categoryId) {
        return mAppDatabase.contentListDao().getAllByCategoryId(categoryId);
    }

    @Override
    public List<ContentList> getContentListsByCategoryIdAndMode(long categoryId, String mode) {
        return mAppDatabase.contentListDao().getAllByCategoryIdAndMode(categoryId, mode);
    }
}
