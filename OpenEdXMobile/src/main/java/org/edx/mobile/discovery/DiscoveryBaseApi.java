package org.edx.mobile.discovery;

import com.google.inject.Inject;

import org.edx.mobile.core.IEdxEnvironment;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DiscoveryBaseApi {
    @Inject
    protected IEdxEnvironment environment;
    private boolean isAuthRequired=true;

}
