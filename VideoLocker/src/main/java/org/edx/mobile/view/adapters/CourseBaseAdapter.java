package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.model.course.CourseComponent;
import org.edx.mobile.model.course.HasDownloadEntry;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.third_party.iconify.IconView;
import org.edx.mobile.view.custom.ETextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for pinned behavior.
 */
public abstract  class CourseBaseAdapter extends BaseAdapter{

    protected final Logger logger = new Logger(getClass().getName());

    protected CourseComponent rootComponent;
    protected LayoutInflater mInflater;
    protected List<CourseBaseAdapter.SectionRow> mData;
    protected SectionRow selectedRow;

    protected IDatabase dbStore;
    protected IStorage storage;
    protected Context context;

    public CourseBaseAdapter(Context context, IDatabase dbStore, IStorage storage) {
        this.context = context;
        this.dbStore = dbStore;
        this.storage = storage;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mData = new ArrayList();
    }

    public abstract void setData(CourseComponent component);

    public void reloadData(){
        if (  this.rootComponent != null )
            setData(this.rootComponent);
    }


    @Override public int getItemViewType(int position) {
        return getItem(position).type;
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
    public CourseBaseAdapter.SectionRow getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
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
     *  handle the click of the row.
     *  NOTE - subclass method should call super.rowClicked(row).
     */
    public void rowClicked(SectionRow row){
        selectedRow = row;
    }

    public SectionRow getSelectedRow(){
        return selectedRow;
    }
    /**
     * download all the videos
     */
    public abstract void download(List<HasDownloadEntry> model);

    public abstract void download(DownloadEntry videoData);


    public ViewHolder getTag(View convertView) {
        ViewHolder holder = new ViewHolder();
        holder.rowType = (IconView) convertView
            .findViewById(R.id.row_type);
        holder.rowTitle = (ETextView) convertView
            .findViewById(R.id.row_title);
        holder.rowSubtitle = (ETextView) convertView
            .findViewById(R.id.row_subtitle);
        holder.rowSubtitleIcon = (IconView) convertView
            .findViewById(R.id.row_subtitle_icon);
        holder.noOfVideos = (TextView) convertView
            .findViewById(R.id.no_of_videos);
        holder.bulkDownload = (IconView) convertView
            .findViewById(R.id.bulk_download);
        holder.bulkDownloadVideos = (LinearLayout) convertView
            .findViewById(R.id.bulk_download_layout);
        holder.rowSubtitlePanel =convertView.findViewById(R.id.row_subtitle_panel);
        holder.halfSeparator = convertView.findViewById(R.id.row_half_separator);

        return holder;
    }

    public static class ViewHolder{

        IconView rowType;
        ETextView rowTitle;
        ETextView rowSubtitle;
        IconView rowSubtitleIcon;
        IconView bulkDownload;
        TextView noOfVideos;
        LinearLayout bulkDownloadVideos;
        View rowSubtitlePanel;
        View halfSeparator;
    }



    public static class SectionRow {

        public static final int ITEM = 0;
        public static final int SECTION = 1;


        public final int type;
        public final boolean topComponent;
        public final CourseComponent component;

        //field to cache the temp value
        public int numOfVideoNotDownloaded = -1;

        public SectionRow(int type, CourseComponent component) {
            this(type, false, component);
        }

        public SectionRow(int type, boolean topComponent, CourseComponent component) {
            this.type = type;
            this.topComponent = topComponent;
            this.component = component;
        }

    }

}
