package org.edx.mobile.module.serverapi;

import android.content.Context;

/**
 * Created by rohan on 2/7/15.
 */
public class ApiFactory {

    /**
     * Returns new instance of {@link org.edx.mobile.module.serverapi.IApi} class.
     * @param Context
     * @return
     */
    public static IApi getInstance(Context context) {
        return new IApiImpl(context);
    }
}
