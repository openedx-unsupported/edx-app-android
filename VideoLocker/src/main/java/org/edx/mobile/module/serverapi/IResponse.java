package org.edx.mobile.module.serverapi;

import android.os.Bundle;

/**
 * Created by rohan on 2/6/15.
 */
public interface IResponse {

    String getResponse();
    int getStatusCode();
    Bundle getCookies();
}
