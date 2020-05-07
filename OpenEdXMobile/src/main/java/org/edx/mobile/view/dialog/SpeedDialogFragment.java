package org.edx.mobile.view.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.util.VideoPlaybackSpeed;
import org.edx.mobile.view.adapters.SpeedAdapter;

import java.util.ArrayList;
import java.util.List;

import roboguice.fragment.RoboDialogFragment;

public class SpeedDialogFragment extends RoboDialogFragment {

    public interface IListDialogCallback {
        void onItemClicked(Float speed);

        void onCancelClicked();
    }

    public static final String PLAYBACK_SPEED = "playback_speed";
    private IListDialogCallback callback;
    private List<Float> speeds;

    @Inject
    IEdxEnvironment environment;

    private void setupWindow() {
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Holo_Dialog);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    public SpeedDialogFragment() {
        speeds = new ArrayList<>();
        for (VideoPlaybackSpeed playbackSpeed : VideoPlaybackSpeed.values()) {
            speeds.add(playbackSpeed.getSpeedValue());
        }
    }

    public static SpeedDialogFragment getInstance(IListDialogCallback callback,
                                                  float selectedPlaybackSpeed) {
        SpeedDialogFragment speedDialogFragment = new SpeedDialogFragment();
        speedDialogFragment.callback = callback;
        Bundle args = new Bundle();
        args.putFloat(PLAYBACK_SPEED, selectedPlaybackSpeed);
        speedDialogFragment.setArguments(args);
        return speedDialogFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setupWindow();

        View view = inflater.inflate(R.layout.panel_speed_popup_dialog_fragment, container, false);
        float selectedPlaybackSpeed = VideoPlaybackSpeed.SPEED_1_0X.getSpeedValue();
        if (getArguments() != null) {
            selectedPlaybackSpeed = getArguments().getFloat(PLAYBACK_SPEED, VideoPlaybackSpeed.SPEED_1_0X.getSpeedValue());
        }
        SpeedAdapter adapter = new SpeedAdapter(getContext(), environment, selectedPlaybackSpeed) {
            @Override
            public void onItemClicked(Float speed) {
                if (callback != null) {
                    callback.onItemClicked(speed);
                }
                dismiss();
            }
        };
        adapter.setItems(speeds);
        ListView list = (ListView) view.findViewById(R.id.speed_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(adapter);

        TextView tvCancel = (TextView) view.findViewById(R.id.tv_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onCancelClicked();
                }
            }
        });
        return view;
    }
}
