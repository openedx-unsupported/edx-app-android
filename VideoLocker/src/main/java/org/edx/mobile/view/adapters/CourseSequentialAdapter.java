package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.model.IComponent;
import org.edx.mobile.model.ISequential;
import org.edx.mobile.model.IUnit;
import org.edx.mobile.model.IVertical;
import org.edx.mobile.third_party.iconify.Iconify;
import org.edx.mobile.third_party.view.PinnedSectionListView;

import java.util.List;

/**
 * Used for pinned behavior.
 */
public abstract  class CourseSequentialAdapter extends CourseBaseAdapter
    implements PinnedSectionListView.PinnedSectionListAdapter, SectionIndexer {


    public CourseSequentialAdapter(Context context) {
        super(context);
    }

    /**
     * component can be null.
     * @IComponent component should be ICourse
     */
    public void setData(IComponent component){
        if ( !(component instanceof ISequential ) )
            return;//
        this.rootComponent = component;
        mData.clear();
        this.sections = new SectionRow[0];
        if ( rootComponent != null ) {
            ISequential course = (ISequential)rootComponent;
            int sectionsNumber = course.getVerticals().size();
            sections = new SectionRow[sectionsNumber];

            int sectionPosition = 0, listPosition = 0;
            for (int i=0; i<sectionsNumber; i++) {
                IVertical vertical = course.getVerticals().get(i);
                SectionRow section = new SectionRow(SectionRow.SECTION, vertical );
                section.sectionPosition = sectionPosition;
                section.listPosition = listPosition++;
                sections[sectionPosition] = section;
                mData.add(section);

                List<IUnit> units = vertical.getUnits();
                for (int j=0;j<units.size();j++) {
                    SectionRow item = new SectionRow(SectionRow.ITEM, units.get(j) );
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
                IUnit unit = (IUnit)row.component;
                //TODO - we will move it using ViewHolder pattern
                TextView image = (TextView) convertView.findViewById(R.id.vertical_type);
                image.setVisibility(View.VISIBLE);
                if ( "video".equals(unit.getCategory())){
                    Iconify.setIcon(image, Iconify.IconValue.fa_film);
                } else {
                    Iconify.setIcon(image, Iconify.IconValue.fa_file_o);
                }

                image = (TextView) convertView.findViewById(R.id.bulk_download);
                if ( "video".equals(unit.getCategory())){
                    Iconify.setIcon(image, Iconify.IconValue.fa_download);
                    image.setVisibility(View.VISIBLE);
                } else {
                    image.setVisibility(View.INVISIBLE);
                }

                TextView view = (TextView) convertView.findViewById(R.id.chapter_name);
                view.setText(unit.getName());
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rowClicked(row);
                    }
                });
                break;
            case SectionRow.SECTION:
                IVertical vertical = (IVertical)row.component;
                ((TextView)convertView).setText(vertical.getName());
                break;
        }

        return convertView;
    }


}
