package org.edx.mobile.view.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.base.MainApplication;
import org.edx.mobile.event.DownloadEvent;
import org.edx.mobile.model.IComponent;
import org.edx.mobile.model.ISequential;
import org.edx.mobile.model.IUnit;
import org.edx.mobile.model.IVertical;
import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.model.download.NativeDownloadModel;
import org.edx.mobile.module.db.DataCallback;
import org.edx.mobile.module.db.IDatabase;
import org.edx.mobile.module.prefs.PrefManager;
import org.edx.mobile.module.storage.IStorage;
import org.edx.mobile.third_party.iconify.Iconify;
import org.edx.mobile.third_party.view.PinnedSectionListView;
import org.edx.mobile.util.MemoryUtil;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Used for pinned behavior.
 */
public abstract  class CourseSequentialAdapter extends CourseBaseAdapter
    implements PinnedSectionListView.PinnedSectionListAdapter, SectionIndexer {


    public CourseSequentialAdapter(Context context, IDatabase dbStore, IStorage storage) {
        super(context, dbStore, storage);
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
            SectionRow[] sectionsHolder = new SectionRow[sectionsNumber];

            PrefManager.UserPrefManager userPrefManager = new PrefManager.UserPrefManager(MainApplication.instance());
            boolean currentVideoMode = userPrefManager.isUserPrefVideoModel();

            int sectionPosition = 0, listPosition = 0;
            for (int i=0; i<sectionsNumber; i++) {
                IVertical vertical = course.getVerticals().get(i);
                if( currentVideoMode && vertical.getVideoCount() == 0 )
                    continue;

                SectionRow section = new SectionRow(SectionRow.SECTION, vertical );
                section.sectionPosition = sectionPosition;
                section.listPosition = listPosition++;
                sectionsHolder[sectionPosition] = section;
                mData.add(section);

                List<IUnit> units = vertical.getUnits();
                for (int j=0;j<units.size();j++) {
                    if( currentVideoMode && !"video".equals( units.get(j).getCategory() ))
                        continue;
                    SectionRow item = new SectionRow(SectionRow.ITEM, units.get(j) );
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
        final ViewHolder viewHolder = (ViewHolder)convertView.getTag();
        final SectionRow row = this.getItem(position);

        IUnit unit = (IUnit)row.component;
        viewHolder.rowType.setVisibility(View.VISIBLE);
        if ( "video".equals(unit.getCategory())){
            updateUIForVideo(position, convertView, viewHolder, row);
        } else {
            Iconify.setIcon(viewHolder.rowType, Iconify.IconValue.fa_file_o);
            viewHolder.bulkDownload.setVisibility(View.INVISIBLE);
        }
        viewHolder.rowTitle.setText(unit.getName());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rowClicked(row);
            }
        });


        return convertView;
    }

    private void updateUIForVideo(int position, View convertView, final ViewHolder viewHolder, final SectionRow row ){
        IUnit unit = (IUnit)row.component;

        Iconify.setIcon(viewHolder.rowType, Iconify.IconValue.fa_film);
        viewHolder.bulkDownload.setVisibility(View.VISIBLE);
        Iconify.setIcon(viewHolder.bulkDownload, Iconify.IconValue.fa_arrow_down);

        final DownloadEntry videoData =  unit.getDownloadEntry(storage);

        viewHolder.rowSubtitle.setText(MemoryUtil.format(MainApplication.instance(), videoData.size) + " " +
            videoData.getDurationReadable());

        if (videoData.downloaded == DownloadEntry.DownloadedState.DOWNLOADING) {

            NativeDownloadModel downloadModel = storage.
                getNativeDownlaod(videoData.dmId);
            if(downloadModel!=null){
                int percent = downloadModel.getPercent();
                if(percent>=0 && percent < 100){
                    EventBus.getDefault().post(new DownloadEvent(DownloadEvent.DownloadStatus.STARTED));
                }
            }
        }

        dbStore.getWatchedStateForVideoId(videoData.videoId,
            new DataCallback<DownloadEntry.WatchedState>(true) {
                @Override
                public void onResult(DownloadEntry.WatchedState result) {
                    DownloadEntry.WatchedState ws = result;
                    if(ws == null || ws == DownloadEntry.WatchedState.UNWATCHED) {
                        viewHolder.rowType.setTextColor(context.getResources().getColor(R.color.cyan_3));
                    } else if(ws == DownloadEntry.WatchedState.PARTIALLY_WATCHED) {
                        viewHolder.rowType.setTextColor(context.getResources().getColor(R.color.cyan_2));
                    } else {
                        viewHolder.rowType.setTextColor(context.getResources().getColor(R.color.grey_3));
                    }
                }
                @Override
                public void onFail(Exception ex) {
                    logger.error(ex);
                }
            });

        dbStore.getDownloadedStateForVideoId(videoData.videoId,
            new DataCallback<DownloadEntry.DownloadedState>(true) {
                @Override
                public void onResult(DownloadEntry.DownloadedState result) {
                    DownloadEntry.DownloadedState ds = result;
                    if(ds == null || ds == DownloadEntry.DownloadedState.ONLINE) {
                        // not yet downloaded
                        viewHolder.bulkDownloadVideos.setVisibility(View.VISIBLE);
                        viewHolder.bulkDownloadVideos.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                EventBus.getDefault().post(new DownloadEvent(DownloadEvent.DownloadStatus.STARTED));
                                logger.debug("Download Button Clicked");
                                //notifyDataSetChanged();
                                download(videoData);
                            }
                        });
                    } else if(ds == DownloadEntry.DownloadedState.DOWNLOADING) {
                        // may be download in progress
                        EventBus.getDefault().post(new DownloadEvent(DownloadEvent.DownloadStatus.STARTED));
                        viewHolder.bulkDownloadVideos.setVisibility(View.GONE);
                        storage.getDownloadProgressByDmid(videoData.dmId, new DataCallback<Integer>(true) {
                            @Override
                            public void onResult(Integer result) {
                                if(result>=0 && result < 100){
                                    EventBus.getDefault().post(new DownloadEvent(DownloadEvent.DownloadStatus.STARTED));
                                }
                            }
                            @Override
                            public void onFail(Exception ex) {
                                logger.error(ex);
                                viewHolder.bulkDownloadVideos.setVisibility(View.VISIBLE);
                            }
                        });
                    } else if (ds == DownloadEntry.DownloadedState.DOWNLOADED) {
                        // downloaded
                        viewHolder.bulkDownloadVideos.setVisibility(View.GONE);
                    }

                }
                @Override
                public void onFail(Exception ex) {
                    logger.error(ex);
                    viewHolder.bulkDownloadVideos.setVisibility(View.VISIBLE);
                }
            });

    }

    public  View getHeaderView(int position, View convertView, ViewGroup parent){
        final SectionRow row = this.getItem(position);
        IVertical vertical = (IVertical)row.component;
        ((TextView)convertView).setText(vertical.getName());
        return convertView;
    }
}
