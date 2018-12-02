package org.edx.mobile.tta.data.local.db;

import org.edx.mobile.tta.data.local.db.table.User;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by Arjun on 2018/9/18.
 */
public interface ILocalDataSource {
    Observable<List<User>> getAllUsers();
    Observable<Boolean> insertUser(final User user);
}