package org.edx.mobile.tta.utils;

import android.util.SparseArray;

import org.edx.mobile.tta.analytics.analytics_enums.Nav;
import static org.edx.mobile.util.BrowserUtil.appPref;

public class BreadcrumbUtil {

    public static String setBreadcrumb(int rank, String s){

        String current = appPref.getCurrentBreadcrumb();
        SparseArray<String> sparse = new SparseArray<>();
        if (!current.equals("")) {
            String[] chunks = current.split("/");
            for (String chunk: chunks){
                String[] bits = chunk.split("__");
                sparse.put(Integer.parseInt(bits[1]), bits[0]);
            }
        }

        sparse.put(rank, s);
        for (int i = sparse.indexOfKey(rank) + 1; i < sparse.size(); i++) {
            sparse.put(sparse.keyAt(i), "");
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < sparse.size(); i++) {
            int key = sparse.keyAt(i);
            String value = sparse.get(key);
            if (!value.equals("")) {
                builder.append(value).append("__").append(key).append("/");
            }
        }

        if (builder.length() > 0){
            builder.deleteCharAt(builder.length() - 1);
        }
        appPref.setCurrentBreadcrumb(builder.toString());
        return getBreadcrumb();

    }

    public static String getBreadcrumb(){

        StringBuilder builder = new StringBuilder();
        String current = appPref.getCurrentBreadcrumb();
        String[] chunks = current.split("/");
        for (String chunk: chunks){
            String[] bits = chunk.split("__");
            builder.append(bits[0]).append("/");
        }

        if (builder.length() > 0){
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();

    }

    public static int getCurrentRank(){

        int rank = -1;
        String breadcrumb = appPref.getCurrentBreadcrumb();
        if (!breadcrumb.equals("")) {
            String[] chunks = breadcrumb.split("/");
            for (String chunk: chunks) {
                String[] bits = chunk.split("__");
                int r = Integer.parseInt(bits[1]);
                if (r > rank){
                    rank = r;
                }
            }
        }
        return rank;

    }

}
