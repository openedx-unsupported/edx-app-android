package org.edx.mobile.tta.data.model;

import org.edx.mobile.tta.data.local.db.table.Content;

import java.util.List;

public class ContentResponse {

    private long count;

    private String next;

    private String previous;

    private List<Content> results;

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrevious() {
        return previous;
    }

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public List<Content> getResults() {
        return results;
    }

    public void setResults(List<Content> results) {
        this.results = results;
    }
}
