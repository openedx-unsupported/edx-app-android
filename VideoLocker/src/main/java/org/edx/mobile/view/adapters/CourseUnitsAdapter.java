package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import org.edx.mobile.model.IChapter;
import org.edx.mobile.model.ICourse;
import org.edx.mobile.model.ISequential;
import org.edx.mobile.model.IUnit;
import org.edx.mobile.model.IVertical;

import java.util.ArrayList;
import java.util.List;

/**
 *  we support two types of views.  webview and video view
 */
public abstract  class CourseUnitsAdapter extends BaseAdapter  {
    private static final int TYPE_VIDEOVIEW = 0;
    private static final int TYPE_WEBVIEW = 1;


    protected LayoutInflater mInflater;
    protected List<IUnit> mData;

    public CourseUnitsAdapter(Context context) {
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);;
        mData = new ArrayList();
    }

    public void setData(ICourse course){
        mData.clear();
        if ( course == null ) {
            notifyDataSetChanged();
            return;
        }
        for(IChapter chapter : course.getChapters()){
            for(ISequential sequential : chapter.getSequential() ){
                for(IVertical vertical : sequential.getVerticals() ){
                    if ( vertical.getUnits().size() > 0 )
                        mData.addAll(vertical.getUnits());
                }
            }
        }
        notifyDataSetChanged();
    }




    @Override public int getItemViewType(int position) {
        IUnit unit = getItem(position);
        //TODO - we should create enum for different type of units
        return  "video".equalsIgnoreCase(unit.getCategory()) ? TYPE_VIDEOVIEW : TYPE_WEBVIEW;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public  IUnit getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
