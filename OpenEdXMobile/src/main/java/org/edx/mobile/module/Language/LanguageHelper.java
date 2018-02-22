package org.edx.mobile.module.Language;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.http.callback.Callback;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.user.Preferences;
import org.edx.mobile.user.UserService;
import org.edx.mobile.view.MyCoursesListActivity;
import roboguice.RoboGuice;

import java.util.Locale;

/**
 * Created by adamkatz on 2018/02/01.
 */

@Singleton
public class LanguageHelper {
  @Inject
  LoginPrefs loginPrefs;

  @NonNull
  private final PrefManager.AppInfoPrefManager pref;

  @javax.inject.Inject
  public LanguageHelper() {
    pref = new PrefManager.AppInfoPrefManager(MainApplication.instance());
  }

  public void configureLanguage(Activity activity) {
    getAppLanguageFromLocalStorage(activity);
    getAppLanguageByApi(activity);
  }

  public void configureLanguage(String language, Activity activity){
    saveLanguage(language);
    changeLanguage(language, activity);
  }

  private void saveLanguage(String language){
    pref.setLanguage(language);
  }

  private void setLanguage(Locale newLocale, Activity activity){
    Locale.setDefault(newLocale);
    Configuration config = new Configuration();
    config.locale = newLocale;
    MainApplication.instance().getResources().updateConfiguration(config, MainApplication.instance().getResources().getDisplayMetrics());
    makeAlert(activity);
  }

  private void changeLanguage(String language, Activity activity){
    Locale phoneLocale = Resources.getSystem().getConfiguration().locale;
    Locale displayLocale = Locale.getDefault();
    Locale newLocale = new Locale(language);
    if (!language.equals("en") && !displayLocale.equals(newLocale)){
      setLanguage(newLocale, activity);
    }
    else if (language.equals("en")  && !displayLocale.equals(phoneLocale)){
      setLanguage(phoneLocale, activity);
    }
  }

  private void getAppLanguageByApi(final Activity activity){
    final Injector injector = RoboGuice.getInjector(MainApplication.instance());
    UserService userService = injector.getInstance(UserService.class);
    userService.getPreferences(loginPrefs.getUsername()).enqueue(new Callback<Preferences>() {
      @Override
      protected void onResponse(@NonNull Preferences preferences) {
        String displayLanguage = Locale.getDefault().getDisplayLanguage().substring(0,2);
        if (!displayLanguage.toLowerCase().equals(preferences.getPrefLang())) {
          configureLanguage(preferences.getPrefLang(), activity);
        }
      }

      @Override
      protected void onFailure(@NonNull Throwable error) {

      }
    });
  }

  private void getAppLanguageFromLocalStorage(Activity activity){
    String language = pref.getUserLanguages();
    if(language!=null) {
      changeLanguage(language, activity);
    }
  }

  private void makeAlert(final Activity activity){
    new AlertDialog.Builder(activity)
      .setTitle(activity.getResources().getString(R.string.language_changed_title))
      .setMessage(activity.getResources().getString(R.string.language_changed_message))
      .setPositiveButton(activity.getResources().getString(R.string.Okay), new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          restartApp();
        }
      }).show();
  }

  private void restartApp(){
    //This must point to the opening activity after login
    Intent myIntent = new Intent(MainApplication.instance(), MyCoursesListActivity.class);
    myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent
      .FLAG_ACTIVITY_CLEAR_TOP);
    MainApplication.instance().startActivity(myIntent);
  }

}
