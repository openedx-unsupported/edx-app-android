package org.edx.mobile.discussion;

import com.google.gson.annotations.SerializedName;

import java.util.List;


public class TopicThreads {

    @SerializedName("text_search_rewrite")
    public String textSearchRewrite;

    public int count;

    public String next;

    public String previous;

    private List<DiscussionThread> results;

    public List<DiscussionThread> getResults() {
        return results;
    }
}
