package org.edx.mobile.base;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.edx.mobile.R;
import org.edx.mobile.util.NetworkUtil;

public abstract class BaseSingleFragmentActivity extends BaseFragmentActivity {

    public static final String FIRST_FRAG_TAG = "first_frag";
    private View offlineBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups_list);

        offlineBar = findViewById(R.id.offline_bar);
        if(NetworkUtil.isConnected(this)){
            hideOfflineBar();
        }else{
            showOfflineBar();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null){
            try {
                this.loadFirstFragment();
            } catch (Exception e) {
                logger.error(e);
            }
        }

    }

    private void loadFirstFragment() throws Exception {
        Fragment singleFragment = getFirstFragment();

        //this activity will only ever hold this lone fragment, so we
        // can afford to retain the instance during activity recreation
        singleFragment.setRetainInstance(true);

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.my_groups_list_container, singleFragment, FIRST_FRAG_TAG);
        fragmentTransaction.disallowAddToBackStack();
        fragmentTransaction.commit();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem checkBox_menuItem = menu.findItem(R.id.delete_checkbox);
        checkBox_menuItem.setVisible(false);

        return true;

    }

    public abstract Fragment getFirstFragment();

    @Override
    protected void onOnline() {
        super.onOnline();
        hideOfflineBar();
        invalidateOptionsMenu();
    }

    @Override
    protected void onOffline() {
        super.onOffline();
        showOfflineBar();
        invalidateOptionsMenu();
    }

    private void showOfflineBar(){
        if(offlineBar!=null){
            offlineBar.setVisibility(View.VISIBLE);
        }
    }

    private void hideOfflineBar(){
        if(offlineBar!=null){
            offlineBar.setVisibility(View.GONE);
        }
    }
}
