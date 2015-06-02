package org.edx.mobile.task;

import android.content.Context;

import org.apache.http.cookie.Cookie;
import org.edx.mobile.http.Api;

import java.util.List;

public abstract class GetSessesionExchangeCookieTask extends Task<List<Cookie>> {

    public GetSessesionExchangeCookieTask(Context context) {
        super(context);
    }

    @Override
    protected List<Cookie> doInBackground(Object... params) {
        try {
                Api api = new Api(context);
                return api.getSessionExchangeCookie();
        } catch (Exception ex) {
            handle(ex);
            logger.error(ex, true);
        }
        return null;
    }

}
