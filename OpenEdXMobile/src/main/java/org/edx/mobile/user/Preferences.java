package org.edx.mobile.user;

import android.support.annotation.NonNull;
import com.google.gson.annotations.SerializedName;

/**
 * Created by adamkatz on 2018/01/29.
 */

public class Preferences {

  @SerializedName("pref-lang")
  @NonNull
  private String prefLang;

  public String getPrefLang() {
    return prefLang;
  }
}



