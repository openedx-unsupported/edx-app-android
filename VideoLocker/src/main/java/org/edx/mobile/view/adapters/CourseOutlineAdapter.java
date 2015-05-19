package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.edx.mobile.base.MainApplication;
import org.edx.mobile.model.IChapter;
import org.edx.mobile.model.IComponent;
import org.edx.mobile.model.ICourse;
import org.edx.mobile.model.ISequential;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.third_party.iconify.Iconify;
import org.edx.mobile.third_party.view.PinnedSectionListView;
import org.edx.mobile.util.AppConstants;

import java.util.List;

/**
 * Used for pinned behavior.
 */
public abstract  class CourseOutlineAdapter extends CourseBaseAdapter
    implements PinnedSectionListView.PinnedSectionListAdapter, SectionIndexer {


    public CourseOutlineAdapter(Context context, IDatabase dbStore, IStorage storage) {
        super(context, dbStore, storage);
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
            SectionRow[] sectionsHolder = new SectionRow[sectionsNumber];
            PrefManager.UserPrefManager userPrefManager = new PrefManager.UserPrefManager(MainApplication.instance());
            boolean currentVideoMode = userPrefManager.isUserPrefVideoModel();

            int sectionPosition = 0, listPosition = 0;
            for (int i=0; i<sectionsNumber; i++) {
                IChapter chapter = course.getChapters().get(i);
                if ( currentVideoMode && chapter.getVideoCount() == 0 )
                    continue;
                SectionRow section = new SectionRow(SectionRow.SECTION, chapter );
                section.sectionPosition = sectionPosition;
                section.listPosition = listPosition++;
                sectionsHolder[sectionPosition] = section;
                mData.add(section);

                List<ISequential> sequentials = chapter.getSequential();
                for (int j=0;j<sequentials.size();j++) {
                    if ( currentVideoMode && sequentials.get(j).getVideoCount() == 0 )
                        continue;
                    SectionRow item = new SectionRow(SectionRow.ITEM, sequentials.get(j) );
                    item.sectionPosition = sectionPosition;
                    item.listPosition = listPosition++;
                    mData.add(item);
                }

                sectionPosition++;
            }

            if ( sectionPosition > 1 ){
                this.sections = new SectionRow[sectionPosition -1 ];
                System.arraycopy(sectionsHolder, 0, this.sections, 0, this.sections.length);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public View getRowView(int position, View convertView, ViewGroup parent) {
        final SectionRow row = this.getItem(position);
        final ISequential sequential = (ISequential)row.component;
        String courseId = sequential.getChapter().getCourse().getId();
        String chapterId = sequential.getChapter().getId();
        String sequentialId = sequential.getId();

        ViewHolder holder = (ViewHolder)convertView.getTag();

        Iconify.setIcon(holder.bulkDownload, Iconify.IconValue.fa_arrow_down);

        holder.rowTitle.setText(sequential.getName());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rowClicked(row);
            }
        });

        //support video download for video type
        final int totalCount = sequential.getVideoCount();
        if (totalCount == 0){
            holder.noOfVideos.setVisibility(View.INVISIBLE);
            holder.bulkDownloadVideos.setVisibility(View.GONE);
        } else {
            holder.noOfVideos.setVisibility(View.VISIBLE);
            holder.noOfVideos.setText("" + totalCount);

            int inProcessCount = dbStore.getVideosCountBySection(courseId, chapterId, sequentialId, null);
            int webOnlyCount = dbStore.getWebOnlyVideosCountBySection(courseId, chapterId, sequentialId, null);
            int videoCount = totalCount - inProcessCount - webOnlyCount;
            if (videoCount > 0) {
                holder.bulkDownloadVideos.setVisibility(View.VISIBLE);
                holder.bulkDownloadVideos
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View downloadView) {
                            download(sequential.getVideos());
                        }
                    });
            } else {
                holder.bulkDownloadVideos.setVisibility(View.GONE);
            }
        }

        if (AppConstants.offline_flag) {
            holder.noOfVideos.setVisibility(View.GONE);
            holder.bulkDownloadVideos.setVisibility(View.GONE);
            boolean isVideoDownloaded = dbStore.isVideoDownloadedInSection
                (courseId, chapterId, sequentialId, null);
            if(isVideoDownloaded)
            {
                //TODO - any UI update
            }else{
                //TODO - any UI update
            }

        } else {
           //TOOD - any UI update?
        }

        return convertView;
    }

    public  View getHeaderView(int position, View convertView, ViewGroup parent){
        final SectionRow row = this.getItem(position);
        IChapter chapter = (IChapter)row.component;
        ((TextView)convertView).setText(chapter.getName());
        return convertView;
    }

}
