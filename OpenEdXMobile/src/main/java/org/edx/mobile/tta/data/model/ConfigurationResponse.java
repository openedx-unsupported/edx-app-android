package org.edx.mobile.tta.data.model;

import org.edx.mobile.tta.data.local.db.table.Category;
import org.edx.mobile.tta.data.local.db.table.ContentList;
import org.edx.mobile.tta.data.local.db.table.Source;

import java.util.List;

public class ConfigurationResponse
{
    private List<Category> category;

    private List<Source> source;

    private List<ContentList> list;

    public List<Category> getCategory ()
    {
        return category;
    }

    public void setCategory (List<Category> category)
    {
        this.category = category;
    }

    public List<Source> getSource ()
    {
        return source;
    }

    public void setSource (List<Source> source)
    {
        this.source = source;
    }

    public List<ContentList> getList ()
    {
        return list;
    }

    public void setList (List<ContentList> list)
    {
        this.list = list;
    }

    @Override
    public String toString()
    {
        return "ConfigurationResponse [category = "+category+", source = "+source+", list = "+list+"]";
    }
}
