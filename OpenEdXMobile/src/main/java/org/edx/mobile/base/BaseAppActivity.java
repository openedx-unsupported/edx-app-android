package org.edx.mobile.base;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.inject.Inject;
import org.edx.mobile.event.NewRelicEvent;

import de.greenrobot.event.EventBus;
import org.edx.mobile.module.Language.LanguageHelper;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class BaseAppActivity extends RoboAppCompatActivity {

    @Inject
    LanguageHelper languageHelper;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().post(new NewRelicEvent(getClass().getSimpleName()));
        resetTitle();
        // this is needed for when the language changes during runtime
        // the title's get cached in the previous language
    }

    @Override
    protected void onResume(){
        super.onResume();
        languageHelper.configureLanguage(this);
    }

    private void resetTitle() {
        try {
            int label = getPackageManager().getActivityInfo(getComponentName(), PackageManager.GET_META_DATA).labelRes;
            if (label != 0) {
                setTitle(label);
            }
        } catch (PackageManager.NameNotFoundException e) { }
    }
}
