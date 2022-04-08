package org.edx.mobile.view;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.inject.Inject;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;

import org.edx.mobile.R;
import org.edx.mobile.base.BaseFragmentActivity;
import org.edx.mobile.deeplink.ScreenDef;
import org.edx.mobile.module.prefs.LoginPrefs;
import org.edx.mobile.util.IntentFactory;

import static org.edx.mobile.view.Router.EXTRA_PATH_ID;
import static org.edx.mobile.view.Router.EXTRA_SCREEN_NAME;
import static org.edx.mobile.view.TagsFragmentActivity.COLOR_CODE;
import static org.edx.mobile.view.TagsFragmentActivity.SUBJECT;

public class MainBottomDashboardFragment extends BaseFragmentActivity implements MyCoursesListFragment.OnExploreButtonClick {
    private BottomNavigationView bottomNavView;
    private LinearLayout ln_myDashboard;
    private LinearLayout ln_exploreCourse;
    private ImageView iv_space_my_dashboard;
    private ImageView iv_space_explore_course;
    private ExploreFragment exploreBottomFragment;
    private MyCoursesListFragment myCoursesListFragment;
    private MyProgramListFragment myProgramListFragment;
    private static ImageView suodhaIcon;
    private Toolbar toolbar;
    public static final String TAG = MainBottomDashboardFragment.class.getCanonicalName();
    @Inject
    private LoginPrefs loginPrefs;
    private MainBottomDashboardFragment mainBottomDashboardFragment;
    private static ImageView back_arrow;

    public static Intent newIntent(@Nullable @ScreenDef String screenName, @Nullable String pathId) {
        // These flags will make it so we only have a single instance of this activity,
        // but that instance will not be restarted if it is already running
        return IntentFactory.newIntentForComponent(MainBottomDashboardFragment.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .putExtra(EXTRA_SCREEN_NAME, screenName)
                .putExtra(EXTRA_PATH_ID, pathId);
    }

    public static ImageView suodhaIcon() {
        return suodhaIcon;
    }

    public static ImageView backIcon() {
        return back_arrow;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // finally change the color
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.status_bar_color));
        setContentView(R.layout.activity_main_bottom_dashboard_fragment);
        toolbar = findViewById(R.id.main_toolbar);
        back_arrow = findViewById(R.id.back_arrow);
        ln_myDashboard = findViewById(R.id.ln_my_dashboard);
        ln_exploreCourse = findViewById(R.id.ln_explore_course);
        iv_space_my_dashboard = findViewById(R.id.space_my_dashboard);
        iv_space_explore_course = findViewById(R.id.space_explore_course);
        suodhaIcon = findViewById(R.id.subodha_icon);
      /*  suodhaIcon.setVisibility(View.GONE);
        back_arrow.setVisibility(View.VISIBLE);*/
        back_arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        exploreBottomFragment = new ExploreFragment();
        myCoursesListFragment = new MyCoursesListFragment();
        myProgramListFragment = new MyProgramListFragment();
        myCoursesListFragment.setExploreButtonClick(this::onClick);
        myProgramListFragment.setExploreButtonClick(this::onClick);
        iv_space_my_dashboard.setVisibility(View.VISIBLE);
        iv_space_explore_course.setVisibility(View.INVISIBLE);
        ln_myDashboard.setSelected(true);
        ln_exploreCourse.setSelected(false);
      /*  getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment, myCoursesListFragment, MyCoursesListFragment.TAG)
                .commit();*/
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment, myProgramListFragment, MyProgramListFragment.TAG).
                addToBackStack(MyProgramListFragment.TAG)
                .commit();

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            setTitleAndSubtitle(this.getString(R.string.courses));
        }
        //  suodhaIcon.setVisibility(View.GONE);
        ln_myDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                suodhaIcon.setVisibility(View.VISIBLE);
                back_arrow.setVisibility(View.GONE);
                //   getSupportFragmentManager().popBackStack();
                /*  for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); ++i) {
                    getSupportFragmentManager().popBackStack();
                }*/
                iv_space_my_dashboard.setVisibility(View.VISIBLE);
                iv_space_explore_course.setVisibility(View.INVISIBLE);
                ln_myDashboard.setSelected(true);
                ln_exploreCourse.setSelected(false);
                //  showFragmentWithoutBackstack(myCoursesListFragment, MyCoursesListFragment.TAG);
                //  showFragmentWithoutBackstack(myProgramListFragment, MyProgramListFragment.TAG);
              /*  getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_fragment, myProgramListFragment, MyProgramListFragment.TAG).
                        addToBackStack(MyProgramListFragment.TAG)
                        .commit();*/
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment, myProgramListFragment, MyProgramListFragment.TAG).
                            addToBackStack(MyProgramListFragment.TAG)
                            .commit();
                } else {
                    for (int i = 1; i < getSupportFragmentManager().getBackStackEntryCount(); ++i) {
                        getSupportFragmentManager().popBackStack();
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment, myProgramListFragment, MyProgramListFragment.TAG)
                            .commit();
                }
            }
        });

        ln_exploreCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                suodhaIcon.setVisibility(View.VISIBLE);
                back_arrow.setVisibility(View.GONE);
              /*  for(int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); ++i) {
                    getSupportFragmentManager().popBackStack();
                }*/

                iv_space_my_dashboard.setVisibility(View.INVISIBLE);
                iv_space_explore_course.setVisibility(View.VISIBLE);
                ln_myDashboard.setSelected(false);
                ln_exploreCourse.setSelected(true);
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment, exploreBottomFragment, ExploreFragment.TAG).addToBackStack(ExploreFragment.TAG)
                            .commit();
                } else {
                    for (int i = 1; i < getSupportFragmentManager().getBackStackEntryCount(); ++i) {
                        getSupportFragmentManager().popBackStack();
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main_fragment, exploreBottomFragment, ExploreFragment.TAG)
                            .commit();
                }
            }
        });
    }

    private void setTitleAndSubtitle(String title) {
        if (toolbar != null) {
            toolbar.setTitle(title);
        }
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
                /*SeachScreenFragment seachScreenFragment = new SeachScreenFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_fragment, seachScreenFragment, SeachScreenFragment.TAG).addToBackStack(SeachScreenFragment.TAG)
                        .commit();*/
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //  this.finish();
        //  final Fragment fragment = (Fragment) getSupportFragmentManager().findFragmentByTag("tag");
        // int count = getSupportFragmentManager().getBackStackEntryCount();
        if (getSupportFragmentManager() != null && getSupportFragmentManager().getBackStackEntryCount() == 1) {
            suodhaIcon.setVisibility(View.VISIBLE);
            back_arrow.setVisibility(View.GONE);
        }
    }

    public void showFragmentWithoutBackstack(Fragment fragment, String tag) {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.slide_in_from_right,
                        R.anim.slide_out_to_left,
                        R.anim.slide_in_from_left,
                        R.anim.slide_out_to_right
                )
                .replace(R.id.main_fragment, fragment, tag)
                .commit();
    }

    @Override
    public void onClick() {
        iv_space_explore_course.setVisibility(View.VISIBLE);
        iv_space_my_dashboard.setVisibility(View.INVISIBLE);
        ln_exploreCourse.setSelected(true);
        ln_myDashboard.setSelected(false);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_fragment, exploreBottomFragment, ExploreFragment.TAG)
                .commit();
    }
}
