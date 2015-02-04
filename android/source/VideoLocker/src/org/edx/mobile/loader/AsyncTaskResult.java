package org.edx.mobile.loader;

public class AsyncTaskResult<T> {

    private T result;
    private Exception ex;

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Exception getEx() {
        return ex;
    }

    public void setEx(Exception ex) {
        this.ex = ex;
    }

}
