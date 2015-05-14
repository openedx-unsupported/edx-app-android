package org.edx.mobile.view;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;

import org.edx.mobile.R;
import org.edx.mobile.model.ISequential;


/**
 *
 */
public class CourseSequentialOutlineActivity extends CourseVideoListActivity{

    private CourseSequentialOutlineFragment fragment;
    private ISequential sequential;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setApplyPrevTransitionOnRestart(true);

        try{
            segIO.screenViewsTracking(getString(R.string.course_outline));
        }catch(Exception e){
            logger.error(e);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        if( sequential != null )
            outState.putSerializable(Router.EXTRA_SEQUENTIAL, sequential);
        super.onSaveInstanceState(outState);
    }

    protected void restore(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            sequential = (ISequential) savedInstanceState.getSerializable(Router.EXTRA_SEQUENTIAL);
        }
        super.restore(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sequential != null && sequential.getChapter() != null) {
            setTitle( sequential.getChapter().getName() );
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (savedInstanceState == null){
            try {

                fragment = new CourseSequentialOutlineFragment();
                fragment.setTaskProcessCallback(this);

                if (courseData != null &&  sequential != null ) {
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(Router.EXTRA_COURSE_DATA, courseData);
                    bundle.putSerializable(Router.EXTRA_COURSE, course);
                    bundle.putSerializable(Router.EXTRA_SEQUENTIAL, sequential);
                    fragment.setArguments(bundle);
                }
                //this activity will only ever hold this lone fragment, so we
                // can afford to retain the instance during activity recreation
                fragment.setRetainInstance(true);

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.add(R.id.fragment_container, fragment, CourseSequentialOutlineFragment.TAG);
                fragmentTransaction.disallowAddToBackStack();
                fragmentTransaction.commit();

            } catch (Exception e) {
                logger.error(e);
            }
        }else {
            fragment = (CourseSequentialOutlineFragment)
                getSupportFragmentManager().findFragmentByTag(CourseSequentialOutlineFragment.TAG);
        }
    }

    @Override
    public void updateListUI() {
        if( fragment != null )
            fragment.reloadList();
    }
}
