package org.edx.mobile.discussion;

import java.util.List;

public class ThreadComments {

    public int count;

    public String next;

    public String previous;

    private List<DiscussionComment> results;

    public List<DiscussionComment> getResults() {
        return results;
    }
}
