package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.model.IChapter;
import org.edx.mobile.model.IComponent;
import org.edx.mobile.model.ICourse;
import org.edx.mobile.model.ISequential;
import org.edx.mobile.third_party.view.PinnedSectionListView;

import java.util.List;

/**
 * Used for pinned behavior.
 */
public abstract  class CourseOutlineAdapter extends CourseBaseAdapter
    implements PinnedSectionListView.PinnedSectionListAdapter, SectionIndexer {


    public CourseOutlineAdapter(Context context) {
        super(context);
    }

    /**
     * component can be null.
     * @IComponent component should be ICourse
     */
    public void setData(IComponent component){
        if ( !(component instanceof ICourse ) )
            return;//
        this.rootComponent = component;
        mData.clear();
        this.sections = new SectionRow[0];
        if ( rootComponent != null ) {
            ICourse course = (ICourse)rootComponent;
            int sectionsNumber = course.getChapters().size();
            sections = new SectionRow[sectionsNumber];

            int sectionPosition = 0, listPosition = 0;
            for (int i=0; i<sectionsNumber; i++) {
                IChapter chapter = course.getChapters().get(i);
                SectionRow section = new SectionRow(SectionRow.SECTION, chapter );
                section.sectionPosition = sectionPosition;
                section.listPosition = listPosition++;
                sections[sectionPosition] = section;
                mData.add(section);

                List<ISequential> sequentials = chapter.getSequential();
                for (int j=0;j<sequentials.size();j++) {
                    SectionRow item = new SectionRow(SectionRow.ITEM, sequentials.get(j) );
                    item.sectionPosition = sectionPosition;
                    item.listPosition = listPosition++;
                    mData.add(item);
                }

                sectionPosition++;
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);

        if (convertView == null) {
            switch (type) {
                case SectionRow.ITEM:
                    convertView = mInflater.inflate(R.layout.row_course_outline_list, null);

                    break;
                case SectionRow.SECTION:
                    convertView = mInflater.inflate(R.layout.row_section_header, null);
                    break;
            }
        }
        final SectionRow row = this.getItem(position);

        switch (type) {
            case SectionRow.ITEM:
                ISequential sequential = (ISequential)row.component;
                //TODO - we will move it using ViewHolder pattern
                TextView view = (TextView) convertView.findViewById(R.id.chapter_name);
                view.setText(sequential.getName());
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rowClicked(row);
                    }
                });
                break;
            case SectionRow.SECTION:
                IChapter chapter = (IChapter)row.component;
                ((TextView)convertView).setText(chapter.getName());
                break;
        }

        return convertView;
    }


}
