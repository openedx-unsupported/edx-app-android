package org.edx.mobile.view.custom.cc;

import java.util.List;

import org.edx.mobile.player.IVideo.IClosedCaption;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.exoplayer.R;

public class CCLanguageDialogFragment extends DialogFragment {

    private IListDialogCallback callback;
    private ClosedCaptionAdapter adapter;
    
    private void setupWindow() {
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Holo_Dialog);
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    private CCLanguageDialogFragment(){
    }

    public CCLanguageDialogFragment(Context context, List<IClosedCaption> languages, IListDialogCallback c) {
        super();
        setStyle(DialogFragment.STYLE_NO_FRAME, android.R.style.Theme_Holo_Dialog);
        
        this.callback = c;
        adapter = new 
                ClosedCaptionAdapter(context) {
            @Override
            public void onItemClicked(IClosedCaption lang) {
                if (callback != null) {
                    callback.onItemClicked(lang);
                }
                dismiss();
            }
        };
        adapter.setItems(languages);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setupWindow();
        
        View v = inflater.inflate(R.layout.panel_cc_popup_dialog_fragment, container,
                false);
        try{
            ListView list = (ListView) v.findViewById(R.id.cc_list);
            list.setAdapter(adapter);
            list.setOnItemClickListener(adapter);

            TextView tvNone = (TextView) v.findViewById(R.id.tv_cc_none);
            if (adapter.isAnythingSelected()) {
                // some language is selected
                tvNone.setBackgroundResource(R.drawable.selector_rounded_bottom_white_gray);
            } else {
                // NONE is selected
                tvNone.setBackgroundResource(R.color.cc_lang_selected);
            }
            tvNone.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (callback != null) {
                        callback.onNoneClicked();
                    }
                    dismiss();
                }
            });

        }catch(Exception e){
            e.printStackTrace();
        }
        return v;
    }
    
    /**
     * Marks given cc as selected in the adapter.
     * @param cc
     */
    public void setSelected(IClosedCaption cc) {
        adapter.unselectAll();
        if (cc == null) {
            return;
        }
        for (int i=0; i<adapter.getCount(); i++) {
            if (adapter.getItem(i).getLanguage().equals(cc.getLanguage())) {
                adapter.select(i);
                break;
            }
        }
    }

    public static interface IListDialogCallback {
        public void onItemClicked(IClosedCaption lang);
        public void onNoneClicked();
    }

}
