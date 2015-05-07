package org.edx.mobile.view;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.http.Api;
import org.edx.mobile.interfaces.SectionItemInterface;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.IChapter;
import org.edx.mobile.model.ICourse;
import org.edx.mobile.model.ISequential;
import org.edx.mobile.model.IUnit;
import org.edx.mobile.model.IVertical;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.services.CourseManager;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public class CourseUnitNavigationActivity extends CourseBaseActivity {

    protected Logger logger = new Logger(getClass().getSimpleName());

    private CourseOutlineFragment fragment;

    private ViewPager pager;
    private IUnit unit;
    private List<IUnit> unitList = new ArrayList<>();
    private CourseUnitPagerAdapter pagerAdapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RelativeLayout insertPoint = (RelativeLayout)findViewById(R.id.fragment_container);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.view_course_unit_pager, null);
        insertPoint.addView(v, 0,
            new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        pager = (ViewPager)findViewById(R.id.pager);
        pagerAdapter = new CourseUnitPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setPageTransformer(false, new ViewPager.PageTransformer() {

            public void transformPage(View page, float position) {
                int pageWidth = page.getWidth();


                if (position < -1) { // [-Infinity,-1)
                    // This page is way off-screen to the left.
                    page.setAlpha(1);

                } else if (position <= 1) { // [-1,1]

                    // dummyImageView.setTranslationX(-position * (pageWidth / 2)); //Half the normal speed

                } else { // (1,+Infinity]
                    // This page is way off-screen to the right.
                    page.setAlpha(1);
                }

            }
        });

        findViewById(R.id.course_unit_nav_bar).setVisibility(View.VISIBLE);

       Button button = (Button) findViewById(R.id.goto_prev);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(pager.getCurrentItem() + 1);
                tryToUpdateForEndOfSequential();
            }
        });
        button = (Button) findViewById(R.id.goto_next);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pager.setCurrentItem(pager.getCurrentItem() + 1);
                tryToUpdateForEndOfSequential();
            }
        });


        setApplyPrevTransitionOnRestart(true);
        // configure slider layout. This should be called only once and
        // hence is shifted to onCreate() function
      //  configureDrawer();

        try{
            segIO.screenViewsTracking(getString(R.string.course_outline));
        }catch(Exception e){
            logger.error(e);
        }

    }

    private void tryToUpdateForEndOfSequential(){
        int curIndex = pager.getCurrentItem();
        IUnit unit = pagerAdapter.getUnit(curIndex);
        IUnit nextUnit = pagerAdapter.getUnit(curIndex +1);
        View prevButton = findViewById(R.id.goto_prev);
        View newUnitReminder = findViewById(R.id.new_unit_reminder);
        if( unit.getVertical().getId().equalsIgnoreCase(nextUnit.getVertical().getId())){
            prevButton.setVisibility(View.VISIBLE);
            newUnitReminder.setVisibility(View.GONE);
        } else {
            prevButton.setVisibility(View.GONE);
            newUnitReminder.setVisibility(View.VISIBLE);
            TextView textView = (TextView)findViewById(R.id.next_unit_title);
            textView.setText(nextUnit.getVertical().getName());
        }
        findViewById(R.id.course_unit_nav_bar).requestLayout();
    }

    protected void initialize(Bundle arg){
        super.initialize(arg);
        unit = (IUnit) bundle.getSerializable(Router.EXTRA_COURSE_UNIT);
        setData(CourseManager.getSharedInstance().getSequentialInView(), unit);
    }


    @Override
    protected void onStart() {
        super.onStart();
        setTitle("");
    }


    public void setData(ISequential sequential, IUnit selected){
        unitList.clear();
        if ( sequential == null ) {
            return;
        }

        for(IVertical vertical : sequential.getVerticals() ){
            if ( vertical.getUnits().size() > 0 )
                unitList.addAll(vertical.getUnits());
        }
        //populate video detail for
        Api  api = new Api(this);
        ArrayList<SectionItemInterface> list = api
            .getLiveOrganizedVideosByChapter(sequential.getChapter().getId() , sequential.getChapter().getName());

        for (SectionItemInterface m : list) {
            if (m.isVideo()) {
                VideoResponseModel vidmodel = (VideoResponseModel) m;
                DownloadEntry downloadEntry = (DownloadEntry) storage
                    .getDownloadEntryfromVideoResponseModel(vidmodel);
                for(IUnit unit : unitList){
                    if ( unit.getId().equalsIgnoreCase(downloadEntry.getVideoId())){
                        unit.setDownloadEntry(downloadEntry);
                        break;
                    }
                }
            }
        }

        if ( selected != null ){
            int index = unitList.indexOf(selected);
            if ( index >= 0 ){
                pager.setCurrentItem( index );
                tryToUpdateForEndOfSequential();
            }
        }

        if ( pagerAdapter  != null )
            pagerAdapter.notifyDataSetChanged();


    }



    /**
     * This function sets text to the title in Action Bar
     * @param title
     */
    private void setActivityTitle(String title){
        try{
            setTitle(title);
        }catch(Exception e){
            logger.error(e);
        }
    }

    @Override
    protected boolean createOptionMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.course_detail, menu);
        return true;
    }

    private class CourseUnitPagerAdapter extends FragmentPagerAdapter {

        public CourseUnitPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        public IUnit getUnit(int pos){
            if ( pos >= unitList.size() )
                pos = unitList.size() -1;
            if ( pos < 0 )
                pos = 0;
            return unitList.get(pos);
        }
        @Override
        public Fragment getItem(int pos) {
            IUnit unit = getUnit(pos);

            if ( "video".equalsIgnoreCase(unit.getCategory())) {
                return CourseUnitVideoFragment.newInstance(unit);
            }
            return CourseUnitWebviewFragment.newInstance(unit);
        }

        @Override
        public int getCount() {
            return unitList.size();
        }
    }
   /** public static class MyAdapter extends FragmentStatePagerAdapter {
        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Fragment getItem(int position) {
            return null;//ArrayListFragment.newInstance(position);
        }
    } **/
}
