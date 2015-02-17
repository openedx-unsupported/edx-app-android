package org.edx.mobile.view.custom.speed;

import java.util.List;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.exoplayer.R;

public class SpeedDialogFragment extends DialogFragment {

    private IListDialogCallback callback;
    private SpeedAdapter adapter;
    
    private void setupWindow() {
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Dialog);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private SpeedDialogFragment() {
    }
    
    public SpeedDialogFragment(Context context, List<Float> speeds, IListDialogCallback c) {
        super();
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Dialog);
        
        this.callback = c;
        adapter = new 
                SpeedAdapter(context) {
            @Override
            public void onItemClicked(Float lang) {
                if (callback != null) {
                    callback.onItemClicked(lang);
                }
                dismiss();
            }
        };
        adapter.setItems(speeds);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setupWindow();
        
        View v = inflater.inflate(R.layout.panel_speed_popup_dialog_fragment, container,
                false);
        try{
            ListView list = (ListView) v.findViewById(R.id.speed_list);
            list.setAdapter(adapter);
            list.setOnItemClickListener(adapter);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return v;
    }
    
    /**
     * Marks given speed value as selected in the adapter.
     * @param speed
     */
    public void setSelected(Float speed) {
        adapter.unselectAll();
        if (speed == null) {
            return;
        }
        for (int i=0; i<adapter.getCount(); i++) {
            if (adapter.getItem(i).compareTo(speed) == 0) {
                adapter.select(i);
                break;
            }
        }
    }

    public static interface IListDialogCallback {
        public void onItemClicked(Float lang);
        public void onNoneClicked();
    }

}
