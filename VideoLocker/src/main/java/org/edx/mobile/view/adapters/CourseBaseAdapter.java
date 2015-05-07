package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import org.edx.mobile.model.IComponent;
import org.edx.mobile.third_party.view.PinnedSectionListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for pinned behavior.
 */
public abstract  class CourseBaseAdapter extends BaseAdapter
    implements PinnedSectionListView.PinnedSectionListAdapter, SectionIndexer {

    protected IComponent rootComponent;
    protected SectionRow[] sections;
    protected LayoutInflater mInflater;
    protected List<CourseBaseAdapter.SectionRow> mData;

    public CourseBaseAdapter(Context context) {
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);;
        this.sections = new SectionRow[0];
        mData = new ArrayList();
    }

    public abstract void setData(IComponent component);


    @Override public SectionRow[] getSections() {
       return sections;
    }

    @Override public int getPositionForSection(int section) {
        if ( sections.length <= section )
            section = sections.length -1;
        return sections[section].listPosition;
    }

    @Override public int getSectionForPosition(int position) {
        if (position >= getCount()) {
            position = getCount() - 1;
        }
        return getItem(position).sectionPosition;
    }

    @Override public int getItemViewType(int position) {
        return getItem(position).type;
    }

    @Override
    public int getViewTypeCount() {
        return 2;  //TODO - we will change it
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public CourseBaseAdapter.SectionRow getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public boolean isItemViewTypePinned(int viewType) {
        return viewType == SectionRow.SECTION;
    }



    public abstract void rowClicked(SectionRow row) ;


    public static class SectionRow {

        public static final int ITEM = 0;
        public static final int SECTION = 1;

        public final int type;
        public final IComponent component;

        public int sectionPosition;
        public int listPosition;

        public SectionRow(int type, IComponent component) {
            this.type = type;
            this.component = component;
        }

    }

}
