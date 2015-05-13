package org.edx.mobile.view;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.ISequential;
import org.edx.mobile.model.IUnit;
import org.edx.mobile.model.IVertical;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.view.common.PageViewStateCallback;

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
    private ISequential sequential;

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

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if ( state == ViewPager.SCROLL_STATE_DRAGGING ){
                    int curIndex = pager.getCurrentItem();
                    PageViewStateCallback curView = (PageViewStateCallback) pagerAdapter.instantiateItem(pager, curIndex);
                    if( curView != null )
                        curView.onPageDisappear();
                }
                if ( state == ViewPager.SCROLL_STATE_IDLE ){
                    int curIndex = pager.getCurrentItem();
                    PageViewStateCallback curView = (PageViewStateCallback) pagerAdapter.instantiateItem(pager, curIndex);
                     if( curView != null )
                         curView.onPageShow();
                    tryToUpdateForEndOfSequential();
                }
            }
        });

        findViewById(R.id.course_unit_nav_bar).setVisibility(View.VISIBLE);

        Button button = (Button) findViewById(R.id.goto_prev);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = pager.getCurrentItem();
                if ( index >  0 ) {
                    PageViewStateCallback curView = (PageViewStateCallback) pagerAdapter.instantiateItem(pager, index);
                    if( curView != null )
                        curView.onPageDisappear();
                    pager.setCurrentItem(index - 1);
                }
            }
        });
        button = (Button) findViewById(R.id.goto_next);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = pager.getCurrentItem();
                if( index < pagerAdapter.getCount() - 1 ) {
                    PageViewStateCallback curView = (PageViewStateCallback) pagerAdapter.instantiateItem(pager, index);
                    if( curView != null )
                        curView.onPageDisappear();
                    pager.setCurrentItem(index + 1);
                }
            }
        });

        setApplyPrevTransitionOnRestart(true);

        try{
            segIO.screenViewsTracking("Assessment");
        }catch(Exception e){
            logger.error(e);
        }

    }

    private void tryToUpdateForEndOfSequential(){
        int curIndex = pager.getCurrentItem();
        IUnit unit = pagerAdapter.getUnit(curIndex);
        IUnit nextUnit = pagerAdapter.getUnit(curIndex +1);
        View prevButton = findViewById(R.id.goto_prev);
        View nextButton = findViewById(R.id.goto_next);
        nextButton.setVisibility(View.VISIBLE);
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
        if ( curIndex == 0 ){
            prevButton.setVisibility(View.GONE);
        } else if ( curIndex >= pagerAdapter.getCount() -1 ){
            nextButton.setVisibility(View.GONE);
        }
        findViewById(R.id.course_unit_nav_bar).requestLayout();
    }

    protected void initialize(Bundle arg){
        super.initialize(arg);
        setData(sequential, unit);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if( sequential != null )
            outState.putSerializable(Router.EXTRA_SEQUENTIAL, sequential);
        if( unit != null )
            outState.putSerializable(Router.EXTRA_COURSE_UNIT, sequential);
        super.onSaveInstanceState(outState);
    }

    protected void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            sequential = (ISequential) savedInstanceState.getSerializable(Router.EXTRA_SEQUENTIAL);
            unit = (IUnit) savedInstanceState.getSerializable(Router.EXTRA_COURSE_UNIT);
        }
        super.restore(savedInstanceState);
    }


    @Override
    protected void onStart() {
        super.onStart();
        setTitle("");
    }


    protected void onResume() {
        super.onResume();
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
        for (IUnit unit : unitList) {
            if (unit.getCategory().equals("video")  ) {
                VideoResponseModel vidmodel = (VideoResponseModel) unit.getVideoResponseModel();
                DownloadEntry downloadEntry = (DownloadEntry) storage
                    .getDownloadEntryfromVideoResponseModel(vidmodel);
                unit.setDownloadEntry(downloadEntry);
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



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //TODO - should we use load different layout file?
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setActionBarVisible(false);
            findViewById(R.id.course_unit_nav_bar).setVisibility(View.GONE);
        } else {
            setActionBarVisible(true);
            findViewById(R.id.course_unit_nav_bar).setVisibility(View.VISIBLE);
        }
    }

    private class CourseUnitPagerAdapter extends FragmentStatePagerAdapter {

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
                return  CourseUnitVideoFragment.newInstance(unit);
            }
            return CourseUnitWebviewFragment.newInstance(unit);
        }

        @Override
        public int getCount() {
            return unitList.size();
        }
    }

}
