package org.edx.mobile.tta.ui.base;

import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

import androidx.annotation.NonNull;

public class BaseArrayAdapter<T> extends ArrayAdapter<T> {
    public BaseArrayAdapter(@NonNull Context context, int resource) {
        super(context, resource);
    }

    public void setItems(List<T> ts){
        clear();
        addAll(ts);
    }
}
