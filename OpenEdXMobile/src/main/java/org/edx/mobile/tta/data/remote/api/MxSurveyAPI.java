package org.edx.mobile.tta.data.remote.api;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import org.edx.mobile.tta.data.enums.SurveyType;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.edx.mobile.util.BrowserUtil.config;
import static org.edx.mobile.util.BrowserUtil.loginPrefs;
import static org.edx.mobile.util.BrowserUtil.environment;

public class MxSurveyAPI extends AsyncTask<Void, Void, String> {

    private Exception exception;
    // private WebView surveyWebView;
    private Activity activity;
    private Context ctx;
    private SurveyType surveyType;

    //private ProgressBar progressWheel;
    private String loginSurveyUrl=config.getConnectUrl()+"wp-json/tta/v1/GeMyloginSurvey/"+loginPrefs.getUsername();
    private String registrationSurveyUrl=config.getConnectUrl()+"wp-json/tta/v1/GeMyRegistrationSurvey/"+loginPrefs.getUsername();

    public MxSurveyAPI(Context mxContext,Activity mSourceActivity,SurveyType mxsurveyType)
    {
        // this.surveyWebView=wbview;
        this.ctx=mxContext;
        this.surveyType=mxsurveyType;
        // this.progressWheel=surveyProgress;
        this.activity=mSourceActivity;
    }

    protected void onPreExecute() {

    }

    protected String doInBackground(Void... mUrl) {
        // Do some validation here
        try {
            String hostUrl=new String();

            if(surveyType.equals(SurveyType.Login))
            {
                hostUrl=loginSurveyUrl;
            }
            else if(surveyType.equals(SurveyType.Registration))
            {
                hostUrl=registrationSurveyUrl;
            }

            URL url = new URL(hostUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                bufferedReader.close();
                return stringBuilder.toString();
            }
            finally{
                urlConnection.disconnect();
            }
        }
        catch(Exception e) {
            //Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }

    protected void onPostExecute(String response) {
        if(response != null)
        {

            try {
                JSONObject obj = new JSONObject(response);
                String mxSurveyUrl = obj.getString("surveyurl");

                if (!mxSurveyUrl.equals("") && mxSurveyUrl!=null) {

                   /* Intent myIntent = new Intent(ctx, UserSurveyActivity.class);
                    myIntent.putExtra("mxSurveyUrl", mxSurveyUrl);
                    ctx.startActivity(myIntent);*/

                    environment.getRouter().getSurveyFeedbackActivity(activity,mxSurveyUrl);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
