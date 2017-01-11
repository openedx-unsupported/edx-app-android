package org.edx.mobile.view.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;

import org.edx.mobile.R;
import org.edx.mobile.core.IEdxEnvironment;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.view.adapters.ClosedCaptionAdapter;

import java.util.HashMap;
import java.util.LinkedHashMap;

import roboguice.fragment.RoboDialogFragment;

public class CCLanguageDialogFragment extends RoboDialogFragment {

    private final Logger logger = new Logger(getClass().getName());
    private IListDialogCallback callback;
    private LinkedHashMap<String, String> langList;

    @Inject
    IEdxEnvironment environment;

    public CCLanguageDialogFragment(){
    }

    public static CCLanguageDialogFragment getInstance(LinkedHashMap<String, String> dialogMap,
            IListDialogCallback callback, @Nullable String languageSelected) {
        CCLanguageDialogFragment d = new CCLanguageDialogFragment();

        d.callback = callback;
        d.langList = dialogMap;
        Bundle args = new Bundle();
        d.langList = dialogMap;
        args.putString("selectedLanguage", languageSelected);
        d.setArguments(args);

        return d;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        View v = inflater.inflate(R.layout.panel_cc_popup, container,
                false);
        try{
            ListView lv_ccLang = (ListView) v.findViewById(R.id.cc_list);
            ClosedCaptionAdapter ccAdaptor = new
                    ClosedCaptionAdapter(getActivity().getBaseContext(), environment) {
                @Override
                public void onItemClicked(HashMap<String, String> language) {
                    if (callback != null) {
                        callback.onItemClicked(language);
                    }
                    dismiss();
                }
            };
            lv_ccLang.setAdapter(ccAdaptor);
            lv_ccLang.setOnItemClickListener(ccAdaptor);
            //ArrayList<String> langList =  getArguments().getStringArrayList("langs");

            if(langList!=null){
                HashMap<String, String> hm;
                for(int i=0; i<langList.size();i++){
                    hm = new HashMap<String, String>();
                    hm.put(langList.keySet().toArray()[i].toString(), 
                            langList.values().toArray()[i].toString());
                    ccAdaptor.add(hm);
                }
            }
            String langSelected = getArguments().getString("selectedLanguage");

            ccAdaptor.selectedLanguage = langSelected;
            ccAdaptor.notifyDataSetChanged();


            TextView tvNone = (TextView) v.findViewById(R.id.tv_cc_cancel);
            final String tvNoneTxt = getString(R.string.lbl_cc_cancel);
            if(langSelected!=null){
                if(langSelected.equalsIgnoreCase(tvNoneTxt)){
                    tvNone.setBackgroundResource(R.color.cyan_text_navigation_20);
                }else{
                    tvNone.setBackgroundResource(R.drawable.white_bottom_rounded_selector);
                }
            }else{
                tvNone.setBackgroundResource(R.color.cyan_text_navigation_20);
            }

            tvNone.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (callback != null) {
                        callback.onCancelClicked();
                    }
                    dismiss();
                }
            });

        }catch(Exception e){
            logger.error(e);
        }
        return v;
    }

}
