package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.deeplink.ScreenDef;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.Config;

import javax.inject.Inject;

import static org.edx.mobile.view.Router.EXTRA_SCREEN_NAME;

public class TagsFragmentActivity extends BaseSingleFragmentActivity {
    protected Logger logger = new Logger(getClass().getSimpleName());
    public static final String SUBJECT = "subject";
    public static final String COLOR_CODE = "color_code";
    @com.google.inject.Inject
    LoginPrefs loginPrefs;

    @Inject
    private Config config;

    public static Intent newIntent(Context activity, @Nullable @ScreenDef String screenName, String subject, int colorCode) {
        final Intent intent = new Intent(activity, TagsFragmentActivity.class);
        intent.putExtra(EXTRA_SCREEN_NAME, screenName);
        intent.putExtra(SUBJECT, subject);
        intent.putExtra(COLOR_CODE, String.valueOf(colorCode));
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return intent;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public Fragment getFirstFragment() {
        return TagsFragment.newInstance(getIntent().getExtras());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_courses, menu);
        menu.findItem(R.id.menu_item_account).setVisible(true);
        menu.findItem(R.id.menu_item_search).setVisible(true);
        menu.findItem(R.id.menu_item_search).setIcon(
                new IconDrawable(this, FontAwesomeIcons.fa_search)
                        .colorRes(this, R.color.black)
                        .actionBarSize(this));
        menu.findItem(R.id.menu_item_account).setIcon(
                new IconDrawable(this, FontAwesomeIcons.fa_user)
                        .colorRes(this, R.color.black)
                        .actionBarSize(this));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_account: {
                environment.getRouter().showUserProfile(this, loginPrefs.getUsername(), loginPrefs.getUserType());
                return true;
            }
            case R.id.menu_item_search: {
                environment.getRouter().showSeachActivity(this);
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }
}
