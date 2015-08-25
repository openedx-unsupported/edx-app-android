package org.edx.mobile.view.adapters;

/**
 *
 */
public interface IPagination {
    public static final int DEFAULT_PAGE_SIZE = 3;
    int pageSize();
    int numOfPagesLoaded();
    int numOfRecordsLoaded();
    boolean mayHasMorePages();
    void setHasMorePages(boolean hasMorePages);
    void clear();
    void addPage(int numOfRecordInPage);
}
