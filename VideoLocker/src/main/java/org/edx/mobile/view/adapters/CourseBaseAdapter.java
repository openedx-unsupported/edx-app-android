package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.IComponent;
import org.edx.mobile.model.api.VideoResponseModel;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.third_party.view.PinnedSectionListView;
import org.edx.mobile.view.custom.ETextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for pinned behavior.
 */
public abstract  class CourseBaseAdapter extends BaseAdapter
    implements PinnedSectionListView.PinnedSectionListAdapter, SectionIndexer {

    protected final Logger logger = new Logger(getClass().getName());

    protected IComponent rootComponent;
    protected SectionRow[] sections;
    protected LayoutInflater mInflater;
    protected List<CourseBaseAdapter.SectionRow> mData;

    protected IDatabase dbStore;
    protected IStorage storage;
    protected Context context;

    public CourseBaseAdapter(Context context, IDatabase dbStore, IStorage storage) {
        this.context = context;
        this.dbStore = dbStore;
        this.storage = storage;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);;
        this.sections = new SectionRow[0];
        mData = new ArrayList();
    }

    public abstract void setData(IComponent component);

    public void reloadData(){
        if (  this.rootComponent != null )
            setData(this.rootComponent);
    }


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


    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {

        int type = getItemViewType(position);

        if (convertView == null) {
            switch (type) {
                case SectionRow.ITEM:
                    convertView = mInflater.inflate(R.layout.row_course_outline_list, null);
                    // apply a tag to this list row
                    ViewHolder tag = getTag(convertView);
                    convertView.setTag(tag);
                    break;
                default : //SectionRow.SECTION:
                    convertView = mInflater.inflate(R.layout.row_section_header, null);
                    break;
            }
        }

        switch (type) {
            case SectionRow.ITEM:
                return  getRowView(position, convertView, parent);
            default : //SectionRow.SECTION:
                return  getHeaderView(position, convertView, parent);
        }
    }

    /**
     *  subclass should implement this method instead,
     *  convertView is guaranteed not null
     */
    public abstract View getRowView(int position, View convertView, ViewGroup parent);

    /**
     *  subclass should implement this method for section header
     *   convertView is guaranteed not null
     */
    public abstract View getHeaderView(int position, View convertView, ViewGroup parent);

    /**
     *  handle the click of the row
     */
    public abstract void rowClicked(SectionRow row) ;

    /**
     * download all the videos
     */
    public abstract void download(List<VideoResponseModel> models);

    public abstract void download(DownloadEntry videoData);


    public ViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.rowType = (TextView) convertView
            .findViewById(R.id.row_type);
        holder.rowTitle = (ETextView) convertView
            .findViewById(R.id.row_title);
        holder.rowSubtitle = (ETextView) convertView
            .findViewById(R.id.row_subtitle);
        holder.rowSubtitleIcon = (TextView) convertView
            .findViewById(R.id.row_subtitle_icon);
        holder.noOfVideos = (TextView) convertView
            .findViewById(R.id.no_of_videos);
        holder.bulkDownload = (TextView) convertView
            .findViewById(R.id.bulk_download);
        holder.bulkDownloadVideos = (LinearLayout) convertView
            .findViewById(R.id.bulk_download_layout);
        return holder;
    }

    public static class ViewHolder{

        TextView  rowType;
        ETextView rowTitle;
        ETextView rowSubtitle;
        TextView  rowSubtitleIcon;
        TextView bulkDownload;
        TextView noOfVideos;
        LinearLayout bulkDownloadVideos;
    }



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
