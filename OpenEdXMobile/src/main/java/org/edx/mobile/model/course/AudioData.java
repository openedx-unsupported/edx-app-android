package org.edx.mobile.model.course;

import com.google.gson.annotations.SerializedName;

import org.edx.mobile.model.api.TranscriptModel;

/**
 *
 */
public class AudioData extends BlockData {

    @SerializedName("transcripts")
    private String transcriptUrl;

    @SerializedName("playable_audio")
    public EncodedAudios encodedAudios;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AudioData videoData = (AudioData) o;
        if (!getTranscripts().equals(videoData.getTranscripts())) return false;
        return encodedAudios.equals(videoData.encodedAudios);

    }

    public TranscriptModel getTranscripts() {
        TranscriptModel transcriptModel = new TranscriptModel();
        transcriptModel.put("en", transcriptUrl);
        return transcriptModel;
    }
}
