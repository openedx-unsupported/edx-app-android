package org.edx.mobile.tta.wordpress_client.model;

import android.util.SparseBooleanArray;

/**
 * Created by JARVICE on 04-04-2018.
 */

public class CustomFilterCache
{
    private String[] selected_choices;

    private String filter_name;

    public String[] getSelected_choices ()
    {
        return selected_choices;
    }

    public void setSelected_choices (String[] selected_choices)
    {
        this.selected_choices = selected_choices;
    }

    public String getFilter_name ()
    {
        return filter_name;
    }

    public void setFilter_name (String filter_name)
    {
        this.filter_name = filter_name;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [selected_choices = "+selected_choices+", filter_name = "+filter_name+"]";
    }
}