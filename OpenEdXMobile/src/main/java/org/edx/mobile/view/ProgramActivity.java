package org.edx.mobile.view;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseSingleFragmentActivity;
import org.edx.mobile.deeplink.ScreenDef;
import org.edx.mobile.module.prefs.LoginPrefs;

import static org.edx.mobile.view.Router.EXTRA_SCREEN_NAME;

public class ProgramActivity extends BaseSingleFragmentActivity {
    @com.google.inject.Inject
    LoginPrefs loginPrefs;
    public static final String PROGRAM = "program";
    public static final String PROGRAM_UUID = "program_uuid";
    public static Intent newIntent(Context activity, @Nullable @ScreenDef String screenName,String program,String program_uuid) {
        final Intent intent = new Intent(activity, ProgramActivity.class);
        intent.putExtra(EXTRA_SCREEN_NAME, screenName);
        intent.putExtra(PROGRAM, program);
        intent.putExtra(PROGRAM_UUID, program_uuid);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        return intent;
    }

    @Override 
    public Fragment getFirstFragment() {
        return ProgramFragment.newInstance(getIntent().getExtras());
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
