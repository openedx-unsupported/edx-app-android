package org.edx.mobile.view.adapters;

/**
 *  there are two types of pagination api
 *  1.   api provide  number of total records on the server.
 *
 *  2.   api does not provide  num of total record,
 *      edx mobile api belongs to this category. also page size is determined at server side.
 *      i believe this behavior maybe changed?
 */
public class BasePagination implements IPagination{
    private final int pageSize;
    private int numPages;
    private int totalRecords;
    private boolean mayHasMoreData;

    public BasePagination(){
        this(0);
    }

    /**
     * if pageSize is zero, the page size is provided at server side
     */
    public BasePagination(int pageSize){
        this.pageSize = pageSize;
        clear();
    }
    @Override
    public int pageSize() {
        return pageSize;
    }

    @Override
    public int numOfRecordsLoaded(){
        return totalRecords;
    }

    @Override
    public int numOfPagesLoaded() {
        return numPages;
    }

    @Override
    public boolean mayHasMorePages() {
        return mayHasMoreData;
    }

    @Override
    public void setHasMorePages(boolean hasMorePages){
        this.mayHasMoreData = hasMorePages;
    }

    @Override
    public void clear() {
        numPages = 0;
        totalRecords = 0;
        mayHasMoreData = false;
    }

    @Override
    public void addPage(int numOfRecordInPage) {
        numPages ++;
        totalRecords += numOfRecordInPage;
        if ( pageSize > 0 ) {
            mayHasMoreData = pageSize > numOfRecordInPage;
        }
    }
}
