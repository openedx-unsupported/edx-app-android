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
import org.edx.mobile.model.course.BlockType;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.HtmlBlockModel;
import org.edx.mobile.model.course.VideoBlockModel;
import org.edx.mobile.view.common.PageViewStateCallback;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;


/**
 *
 */
public class CourseUnitNavigationActivity extends CourseBaseActivity {

    protected Logger logger = new Logger(getClass().getSimpleName());

    private ViewPager pager;
    private CourseComponent selectedUnit;

    private List<CourseComponent> unitList = new ArrayList<>();
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


        updateDataModel();
        
        setApplyPrevTransitionOnRestart(true);

        try{
            segIO.screenViewsTracking("Assessment");
        }catch(Exception e){
            logger.error(e);
        }

    }


    protected  String getUrlForWebView(){
        if ( selectedUnit == null ){
            return courseComponent == null ? "" : courseComponent.getWebUrl();
        } else {
            return selectedUnit.getWebUrl();
        }
    }

    private void tryToUpdateForEndOfSequential(){
        int curIndex = pager.getCurrentItem();
        selectedUnit = pagerAdapter.getUnit(curIndex);
        db.updateAccess(null, selectedUnit.getId(), true);
        CourseComponent nextUnit = pagerAdapter.getUnit(curIndex +1);
        View prevButton = findViewById(R.id.goto_prev);
        View nextButton = findViewById(R.id.goto_next);
        nextButton.setVisibility(View.VISIBLE);
        prevButton.setVisibility(View.VISIBLE);
        View newUnitReminder = findViewById(R.id.new_unit_reminder);
        if( selectedUnit.isLastChild() ){
            newUnitReminder.setVisibility(View.VISIBLE);
            TextView textView = (TextView)findViewById(R.id.next_unit_title);
            textView.setText(nextUnit.getParent().getDisplayName());
        } else {
            newUnitReminder.setVisibility(View.GONE);
        }
        if ( curIndex == 0 ){
            prevButton.setVisibility(View.GONE);
        } else if ( curIndex >= pagerAdapter.getCount() -1 ){
            nextButton.setVisibility(View.GONE);
        }
        findViewById(R.id.course_unit_nav_bar).requestLayout();

        setTitle(selectedUnit.getDisplayName());
    }

    protected void initialize(Bundle arg){
        super.initialize(arg);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if( selectedUnit != null )
            outState.putSerializable(Router.EXTRA_COURSE_UNIT, selectedUnit);
        super.onSaveInstanceState(outState);
    }

    protected void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            selectedUnit = (CourseComponent) savedInstanceState.getSerializable(Router.EXTRA_COURSE_UNIT);
        }
        super.restore(savedInstanceState);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }


    protected void onResume() {
        super.onResume();
    }

    private void updateDataModel(){
        unitList.clear();
        if( selectedUnit == null || selectedUnit.getRoot() == null ) {
            logger.warn("selectedUnit is null?");
            return;   //should not happen
        }
        //TODO - courseComponent is not necessary the course object.
        //if we want to navigate through all unit of within the parent node,
        //we should use courseComponent instead.   Requirement maybe changed?
       // unitList.addAll( courseComponent.getChildLeafs() );
        List<CourseComponent> leaves = new ArrayList<>();
        EnumSet<BlockType> types = EnumSet.allOf(BlockType.class);
        ((CourseComponent) selectedUnit.getRoot()).fetchAllLeafComponents(leaves, types);
        unitList.addAll( leaves );

        int index = unitList.indexOf(selectedUnit);
        if ( index >= 0 ){
            pager.setCurrentItem( index );
            tryToUpdateForEndOfSequential();
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


        public CourseComponent getUnit(int pos){
            if ( pos >= unitList.size() )
                pos = unitList.size() -1;
            if ( pos < 0 )
                pos = 0;
            return unitList.get(pos);
        }
        @Override
        public Fragment getItem(int pos) {
            CourseComponent unit = getUnit(pos);

            if ( !unit.isMobileSupported() ){
                return CourseUnitMobileNotSupportedFragment.newInstance(unit);
            }

            if ( unit.getType() != BlockType.VIDEO &&
                unit.getType() != BlockType.HTML &&
                unit.getType() != BlockType.OTHERS &&
                unit.getType() != BlockType.DISCUSSION ) {
                return CourseUnitEmptyFragment.newInstance(unit);
            }

            if ( unit instanceof VideoBlockModel) {
                return  CourseUnitVideoFragment.newInstance((VideoBlockModel)unit);
            }

            if ( unit instanceof HtmlBlockModel ){
                CourseUnitWebviewFragment fragment = CourseUnitWebviewFragment.newInstance((HtmlBlockModel)unit);
                fragment.callback = CourseUnitNavigationActivity.this;
                return fragment;
            }

            //fallback
            return CourseUnitMobileNotSupportedFragment.newInstance(unit);

        }

        @Override
        public int getCount() {
            return unitList.size();
        }
    }

}
