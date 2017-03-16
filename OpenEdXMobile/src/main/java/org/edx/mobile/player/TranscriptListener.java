package org.edx.mobile.player;

import android.support.annotation.NonNull;

import subtitleFile.TimedTextObject;

/**
 * Includes the callbacks related to Transcripts of a video.
 */
public interface TranscriptListener {
    /**
     * Callback for when the video's transcript is updated.
     *
     * @param transcript The new transcript.
     */
    void updateTranscript(@NonNull TimedTextObject transcript);

    /**
     * Callback for when a transcript item is selected.
     *
     * @param subtitleIndex The selected transcript item's index.
     */
    void updateSelection(int subtitleIndex);
}
