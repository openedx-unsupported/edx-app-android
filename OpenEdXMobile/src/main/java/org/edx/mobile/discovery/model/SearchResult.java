package org.edx.mobile.discovery.model;

import java.util.List;

public class SearchResult {
    private int count;
    private String next;
    private String previous;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
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

    public List<SearchResultList> getResults() {
        return results;
    }

    public void setResults(List<SearchResultList> results) {
        this.results = results;
    }

    private List<SearchResultList> results;
}
