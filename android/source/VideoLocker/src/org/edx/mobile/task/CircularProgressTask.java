package org.edx.mobile.task;

import android.os.AsyncTask;
import android.view.View;

import org.edx.mobile.view.custom.ProgressWheel;

public class CircularProgressTask extends AsyncTask<Object, Object, Object> {

    ProgressWheel progressBar;
    public CircularProgressTask() {
    }

    public void setProgressBar(ProgressWheel progressBar) {
        this.progressBar = progressBar;
    }
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    protected Object doInBackground(Object... params) {
        for(int i=0;i<=100;i++){
            publishProgress(i);
            i++;
            
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        progressBar.setProgressPercent((Integer) values[0]);
    }
    
    @Override
    protected void onPostExecute(Object result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        if(progressBar!=null){
            progressBar.setVisibility(View.GONE);
        }
    }
}
