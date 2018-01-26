package org.edx.mobile.model.course;

import android.support.annotation.Nullable;
import android.webkit.URLUtil;

import org.edx.mobile.model.db.DownloadEntry;
import org.edx.mobile.module.storage.IStorage;

/**
 * common base class for all type of units
 */
public class AudioBlockModel extends CourseComponent implements HasDownloadEntry {

    private DownloadEntry downloadEntry;
    private AudioData data;

    public AudioBlockModel(BlockModel blockModel, CourseComponent parent){
        super(blockModel,parent);
        this.data = (AudioData) blockModel.data;
    }

    @Nullable
    public DownloadEntry getDownloadEntry(IStorage storage) {
        if (data.encodedAudios.getPreferredPlaybackUrl() == null) {
            return null;
        }
        if ( storage != null ) {
            downloadEntry = (DownloadEntry) storage
                .getDownloadEntryFromAudioModel(this);
        }
        return downloadEntry;
    }

    public AudioData getData() {
        return data;
    }

    public void setData(AudioData data) {
        this.data = data;
    }

    public boolean isPlayableAudio(){
        return URLUtil.isNetworkUrl(data.encodedAudios.getPreferredPlaybackUrl());
    }
}
