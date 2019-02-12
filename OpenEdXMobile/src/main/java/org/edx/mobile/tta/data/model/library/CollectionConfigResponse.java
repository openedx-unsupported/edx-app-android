package org.edx.mobile.tta.data.model.library;

import org.edx.mobile.tta.data.local.db.table.Category;
import org.edx.mobile.tta.data.local.db.table.ContentList;
import org.edx.mobile.tta.data.local.db.table.Source;

import java.util.List;

public class CollectionConfigResponse
{
    private List<Category> category;

    private List<Source> source;

    private List<ContentList> content_list;

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

    public List<ContentList> getContent_list()
    {
        return content_list;
    }

    public void setContent_list(List<ContentList> content_list)
    {
        this.content_list = content_list;
    }

    @Override
    public String toString()
    {
        return "CollectionConfigResponse [category = "+category+", source = "+source+", content_list = "+ content_list +"]";
    }
}
