package org.edx.mobile.tta.data.remote.api;

import android.os.AsyncTask;
import android.webkit.CookieManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.edx.mobile.util.BrowserUtil.config;
import static org.edx.mobile.util.BrowserUtil.loginAPI;
import static org.edx.mobile.util.BrowserUtil.loginPrefs;

public class MxCookiesAPI extends AsyncTask<Void, Void, String> {

    private  String hostUrl=config.getConnectUrl()+"wp-json/tta/v1/generateAuthCookie/"+loginPrefs.getUsername();

    public MxCookiesAPI()
    {
        /*this.connectWebView=wbview;
        this.ctx=mxContext;
        this.mCookieManager=cookieManager;*/
        //this.hostUrl=mhostUrl;
        loginAPI.getConnectCookies();
    }

    protected void onPreExecute() {

    }

    protected String doInBackground(Void... param) {
        // Do some validation here

        try {
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

                String cookieName = obj.getString("cookie_name");
                String cookieValue = obj.getString("session");

                if(cookieName!=null && !cookieName.equals("") && cookieValue!=null && !cookieValue.equals(""))
                {
                    String cookie_string = cookieName+"="+cookieValue;
                    //mCookieManager.setCookie(hostUrl, cookie_string);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
                    String currentTimeStamp = simpleDateFormat.format(new Date());

                    loginAPI.setConnectCookiesAndTimeStamp(cookie_string,currentTimeStamp);

                    final CookieManager cookieManager = CookieManager.getInstance();
                    cookieManager.setAcceptCookie(true);
                    String domain = config.getConnectDomainUrl();
                    cookieManager.setCookie(domain,loginAPI.getConnectCookies()+"; Domain="+domain);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
