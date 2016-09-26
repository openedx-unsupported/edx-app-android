package org.edx.mobile.model.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;


@SuppressWarnings("serial")
public class SectionEntry implements Serializable {

    public String chapter;
    public boolean isChapter=false;
    public HashMap<String, ArrayList<VideoResponseModel>> sections = new LinkedHashMap<String, ArrayList<VideoResponseModel>>();
    /**
     * section_url is to be used for "open in browser" feature
     * @return
     */
    public String section_url;
    
    public Set<String> getSectionNames() {
        return sections.keySet();
    }
    
    /**
     * Returns list of videos in the given section name under this chapter.
     * @param sectionName
     * @return
     */
    public ArrayList<VideoResponseModel> getVideosForSection(String sectionName) {
        return sections.get(sectionName);
    }
    
    /**
     * Returns count of videos in this Chapter.
     * @return
     */
    public int getVideoCount() {
        if (sections == null) 
            return 0;
        
        int count = 0;
        for(Entry<String, ArrayList<VideoResponseModel>> entry : sections.entrySet()) {
            if (entry.getValue() != null) {
                count += entry.getValue().size();
            }
        }
        return count;
    }
    
    public ArrayList<VideoResponseModel> getAllVideos() {
        if (sections == null) 
            return null;

        ArrayList<VideoResponseModel> list = new ArrayList<VideoResponseModel>();
        for(Entry<String, ArrayList<VideoResponseModel>> entry : sections.entrySet()) {
            if (entry.getValue() != null) {
                list.addAll(entry.getValue());
            }
        } 
        
        return list;
    }

}
