package org.edx.mobile.tta.wordpress_client.util;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;;

import org.edx.mobile.tta.utils.MxHelper;
import org.edx.mobile.tta.wordpress_client.model.CustomFilter;
import org.edx.mobile.tta.wordpress_client.model.CustomFilterCache;

import java.util.ArrayList;
import java.util.List;

import static org.edx.mobile.util.BrowserUtil.loginPrefs;

/**
 * Created by KEPLER on 17-Sep-18.
 */

public class WordpressUtil {

    @NonNull
    private final Gson gson = new GsonBuilder().create();

    public String getDownloadablePath(List<CustomFilter> filter)
    {
        String download_url="";
        //find the downloaded obj
        if(filter!=null && filter.size()>0)
        {
            for (CustomFilter item:filter)
            {
                if(TextUtils.isEmpty(item.getName()))
                    continue;

                if(item.getName().toLowerCase().equals(String.valueOf(MxFilterType.MX_VIDEODOWNLOAD).toLowerCase()))
                {
                    download_url=item.getChoices()[0];
                    break;
                }
            }
        }

        return download_url;
    }

    public boolean isPostVideoExist(List<CustomFilter> filter)
    {
        boolean isexist=false;
        //find the downloaded obj
        if(filter!=null && filter.size()>0)
        {
            for (CustomFilter item:filter)
            {
                if(TextUtils.isEmpty(item.getName()))
                    continue;

                if(item.getName().toLowerCase().equals(String.valueOf(MxFilterType.MX_VIDEODOWNLOAD).toLowerCase())&&
                        item.getChoices()!=null && item.getChoices().length > 0)
                {
                    isexist=true;
                    break;
                }
            }
        }

        return isexist;
    }

    public ArrayList<CustomFilter> trimMXFilters(ArrayList<CustomFilter> arrayList)
    {
        ArrayList<CustomFilter> mFinalList=new ArrayList<>();
        if(arrayList==null|| arrayList.size()==0)
            return mFinalList;

        for (CustomFilter filterItem:arrayList) {

            if(TextUtils.isEmpty(filterItem.getName()))
                continue;

            if(!filterItem.getName().toLowerCase().trim().equals
                    (String.valueOf(MxFilterType.MX_VIDEODOWNLOAD).toLowerCase().trim()))
                mFinalList.add(filterItem);
        }

        return mFinalList;
    }

    public ArrayList<CustomFilter> getFilterFromJSON(String filter_json)
    {
        MxHelper helper=new MxHelper();
        return helper.getCustomFilterObjectFromJson(filter_json);
    }

    public String geJSONFromFilter(ArrayList<CustomFilter> filters)
    {
        MxHelper helper=new MxHelper();
        return helper.getJSONStringfromCustomFilterObj(filters);
    }

    public ArrayList<CustomFilter> addMXDownloadFilter(ArrayList<CustomFilter> arrayList)
    {
        ArrayList<CustomFilter> mFinalList=new ArrayList<>();
        if(arrayList==null)
            arrayList=new ArrayList<>();

        mFinalList=arrayList;
        CustomFilter item=new CustomFilter();
        item.setName("डाउनलोड की गयी फाइल");
        String[] choice=new String[1];
        choice[0]="डाउनलोड हो चूका है";
        item.setChoices(choice);
        mFinalList.add(item);

        return mFinalList;
    }

    public ArrayList<CustomFilterCache> addMXDownloadFilterCache(ArrayList<CustomFilterCache> arrayList)
    {
        ArrayList<CustomFilterCache> mFinalList=new ArrayList<>();
        if(arrayList==null)
            arrayList=new ArrayList<>();

        mFinalList=arrayList;
        CustomFilterCache item=new CustomFilterCache();
        item.setFilter_name("डाउनलोड की गयी फाइल");
        String[] choice=new String[1];
        choice[0]="डाउनलोड हो चूका है";
        item.setSelected_choices(choice);
        mFinalList.add(item);

        return mFinalList;
    }

    public ArrayList<CustomFilterCache> removeMXDownloadFilterCache(ArrayList<CustomFilterCache> arrayList)
    {
        ArrayList<CustomFilterCache> mFinalList=new ArrayList<>();
        if(arrayList==null)
            arrayList=new ArrayList<>();

        for (CustomFilterCache filterItem:arrayList) {

            if(TextUtils.isEmpty(filterItem.getFilter_name()))
                continue;

            if(!filterItem.getFilter_name().toLowerCase().trim().equals
                    (String.valueOf("डाउनलोड की गयी फाइल").toLowerCase().trim()))
                mFinalList.add(filterItem);
        }

        return mFinalList;
    }

    public ArrayList<CustomFilter> removeMXDownloadFilter(ArrayList<CustomFilter> arrayList)
    {
        ArrayList<CustomFilter> mFinalList=new ArrayList<>();
        if(arrayList==null)
            arrayList=new ArrayList<>();

        for (CustomFilter filterItem:arrayList) {

            if(TextUtils.isEmpty(filterItem.getName()))
                continue;

            if(!filterItem.getName().toLowerCase().trim().equals
                    (String.valueOf("डाउनलोड की गयी फाइल").toLowerCase().trim()))
                mFinalList.add(filterItem);
        }

        return mFinalList;
    }


    public boolean isMXDownloadFilterExist(ArrayList<CustomFilter> filters) {

        boolean isexist = false;
        //find the downloaded obj
        if (filters != null && filters.size() > 0) {
            for (CustomFilter item : filters) {
                if (TextUtils.isEmpty(item.getName()))
                    continue;

                if (item.getName().toLowerCase().equals(("डाउनलोड की गयी फाइल").toLowerCase())) {
                    isexist = true;
                    break;
                }
            }
        }
        return isexist;

    }

    public boolean isCacheMXDownloadFilterExist(ArrayList<CustomFilterCache> filters) {

        boolean isexist = false;
        //find the downloaded obj
        if (filters != null && filters.size() > 0) {
            for (CustomFilterCache item : filters) {
                if (TextUtils.isEmpty(item.getFilter_name()))
                    continue;

                if (item.getFilter_name().toLowerCase().equals(("डाउनलोड की गयी फाइल").toLowerCase())) {
                    isexist = true;
                    break;
                }
            }
        }
        return isexist;

    }

    public ArrayList<String>  removeDownloadFilterFromDBQuery(ArrayList<String> filter)
    {
        ArrayList<String> mFinalList=new ArrayList<>();
        if(filter==null)
            filter=new ArrayList<>();

        for (String filterItem:filter) {

            if(TextUtils.isEmpty(filterItem))
                continue;

            if(!filterItem.toLowerCase().trim().equals
                    (String.valueOf("डाउनलोड की गयी फाइल_डाउनलोड हो चूका है").toLowerCase().trim()))
                mFinalList.add(filterItem);
        }

        return mFinalList;
    }
}
