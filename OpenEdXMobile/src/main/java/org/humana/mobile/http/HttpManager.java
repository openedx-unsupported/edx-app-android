package org.humana.mobile.http;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.humana.mobile.BuildConfig;
import org.humana.mobile.R;
import org.humana.mobile.logger.Logger;
import org.humana.mobile.util.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Singleton
@Deprecated // Deprecated because this uses org.apache.http, which is itself deprecated
public class HttpManager {

    protected final Logger logger = new Logger(getClass().getName());
    public boolean isCanceled;

    Context context;

    /**
     * Executes a GET request to given URL with given parameters.
     *
     * @param urlWithAppendedParams
     * @param headers
     * @return
     * @throws ParseException
     * @throws ClientProtocolException
     * @throws IOException
     */

    @Inject
    public HttpManager(Context context) {
        this.context = context;
    }

    public HttpResult get(String urlWithAppendedParams, Bundle headers)
            throws ParseException, ClientProtocolException, IOException {
        final DefaultHttpClient client = newClient();
        
        HttpGet get = new HttpGet(urlWithAppendedParams);
        AndroidHttpClient.modifyRequestToAcceptGzipResponse(get);

        // allow redirects
        HttpClientParams.setRedirecting(client.getParams(), true);
        HttpClientParams.setRedirecting(get.getParams(), true);
        
        // set request headers
        if (headers != null) {
            for (String key : headers.keySet()) {
                logger.debug(key + ": " + headers.getString(key));
                get.setHeader(key, headers.getString(key));
            }
        }
        
        HttpResponse response = client.execute(get);
        
        logger.debug("StatusCode for get request= " + response.getStatusLine().getStatusCode());
        
        InputStream inputStream = AndroidHttpClient
                .getUngzippedContent(response.getEntity());
        String strRes = IOUtils.toString(inputStream, Charset.defaultCharset());

        // String response =
        // EntityUtils.toString(client.execute(get).getEntity(), "UTF-8");
        client.getConnectionManager().shutdown();
        HttpResult result = new HttpResult();
        result.body = strRes;
        result.statusCode = response.getStatusLine().getStatusCode();
        return result;
    }

    public HttpResult download(String urlWithAppendedParams,String filePath, Bundle headers)
            throws ParseException, ClientProtocolException, IOException {
        final DefaultHttpClient client = newClient();

         isCanceled = false;

        BroadcastReceiver scormCancelReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isCanceled =true;
                Log.d("intent cancel","::"+isCanceled);
            }
        };

        IntentFilter scormCancelIntentFilter= new IntentFilter("org.firki.mobile.scormCancel");
        scormCancelIntentFilter.setPriority(1000);
       LocalBroadcastManager.getInstance(context).registerReceiver(scormCancelReceiver, scormCancelIntentFilter);

        HttpGet get = new HttpGet(urlWithAppendedParams);
        AndroidHttpClient.modifyRequestToAcceptGzipResponse(get);

        // allow redirects
        HttpClientParams.setRedirecting(client.getParams(), true);
        HttpClientParams.setRedirecting(get.getParams(), true);

        // set request headers
        if (headers != null) {
            for (String key : headers.keySet()) {
                logger.debug(key + ": " + headers.getString(key));
                get.setHeader(key, headers.getString(key));
            }
        }

        HttpResponse response = client.execute(get);

        logger.debug("StatusCode for get request= " + response.getStatusLine().getStatusCode());

        InputStream is = response.getEntity().getContent();
        BufferedInputStream zis = new BufferedInputStream(is);
        FileOutputStream fout = new FileOutputStream(new File(filePath));
        long lenghtOfFile = response.getEntity().getContentLength();

        byte[] buffer = new byte[4096];
        int count;
        long total = 0;

        while ((count = zis.read(buffer)) != -1)
        {
            if(isCanceled){
               // Log.d("cancel","::"+isCanceled);
                //Log.d("Download","Yes i Recived and now canceling request.");

                //zis.close();
                client.getConnectionManager().shutdown();

                throw new ClientProtocolException("Connection need to stop.");
            }
            total += count;

            if (lenghtOfFile > 0) {
                    String progress = ("" + (int) ((total * 100) / lenghtOfFile));
                if(Integer.parseInt(progress) <= 99){
                    Intent intent = new Intent("org.firki.mobile.scorm");
                    intent.putExtra("Progress", progress);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
                fout.write(buffer, 0, count);
            }
        }
        zis.close();
        fout.close();

        client.getConnectionManager().shutdown();
        HttpResult result = new HttpResult();
        result.body = filePath;
        result.statusCode = response.getStatusLine().getStatusCode();

        LocalBroadcastManager.getInstance(context).unregisterReceiver(scormCancelReceiver);

        if(isCanceled){
            new File(filePath).delete();
            return null;
        }else {
            return result;
        }
    }

    /**
     * Executes a POST request to given URL with given parameters.
     * Returns "cookie" in a JSON object if response is HTTP 204 NO CONTENT.
     * Returns "error=401" in JSON format if response code is 401.
     * @param url
     * @param params
     * @param headers
     * @return
     * @throws ParseException
     * @throws ClientProtocolException
     * @throws IOException
     */
    public String post(String url, Bundle params, Bundle headers)
            throws ParseException, ClientProtocolException, IOException {
        final DefaultHttpClient client = newClient();

        HttpPost post = new HttpPost(url);
        AndroidHttpClient.modifyRequestToAcceptGzipResponse(post);

        // set request headers
        if (headers != null) {
            for (String key : headers.keySet()) {
                post.addHeader(key, headers.getString(key));
            }
        }

        if (params != null) {
            List<NameValuePair> pairs = new ArrayList<NameValuePair>();
            for (String key : params.keySet()) {
                pairs.add(new BasicNameValuePair(key, params.getString(key)));
            }
            post.setEntity(new UrlEncodedFormEntity(pairs));
        }

        HttpResponse response = client.execute(post);

        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.NO_CONTENT) {
            // that means response has "NO CONTENTS"
            // so return empty string
            // this is SUCCESS response for login by google/FB account
            logger.debug("HTTP 204 NO CONTENT");
            
            if(response.containsHeader("Set-Cookie")){
                Header header = response.getFirstHeader("Set-Cookie");
                JSONObject json = new JSONObject();
                try {
                    json.put("cookie", header.getValue());
                } catch (JSONException e) {
                    logger.error(e);
                }

                // end connection and return
                client.getConnectionManager().shutdown();
                return json.toString();
            }
        } else if (statusCode == HttpStatus.UNAUTHORIZED) {
            // for google/FB login, this means google/FB account is not associated with edX
            logger.debug("Response of HTTP 401");

            JSONObject json = new JSONObject();
            try {
                json.put("error", "401");
            } catch (JSONException e) {
                logger.error(e);
            }

            // end connection and return
            client.getConnectionManager().shutdown();
            return json.toString();
        }

        InputStream inputStream = AndroidHttpClient
                .getUngzippedContent(response.getEntity());
        String strRes = IOUtils.toString(inputStream, Charset.defaultCharset());

        client.getConnectionManager().shutdown();

        return strRes;
    }

    /**
     * Performs a HTTP POST with given postBody and headers.
     * @param url
     * @param postBody
     * @param headers
     * @return
     * @throws ParseException
     * @throws ClientProtocolException
     * @throws IOException
     */
    public String post(String url, String postBody, Bundle headers)
            throws ParseException, ClientProtocolException, IOException {
        // this is POST, so isPathRequest=false
        return post(url, postBody, headers, false);
    }
    
    /**
     * Performs a HTTP POST or PATCH request with given postBody and headers.
     * @param url
     * @param postBody
     * @param headers
     * @param isPatchRequest - If true, then performs PATCH request, POST otherwise.
     * @return
     * @throws ParseException
     * @throws ClientProtocolException
     * @throws IOException
     */
    public String post(String url, String postBody, Bundle headers, boolean isPatchRequest)
            throws ParseException, ClientProtocolException, IOException {
        final DefaultHttpClient client = newClient();

        HttpPost post = null;
        if (isPatchRequest) {
            post = new HttpPatch(url);
        } else {
            post = new HttpPost(url);
        }
        AndroidHttpClient.modifyRequestToAcceptGzipResponse(post);

        // set request headers
        if (headers != null) {
            for (String key : headers.keySet()) {
                post.addHeader(key, headers.getString(key));
            }
        }

        StringEntity se = new StringEntity(postBody);
        se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        post.setEntity(se);

        HttpResponse response = client.execute(post);
        int statusCode = response.getStatusLine().getStatusCode();
        //make this change to handle it consistent with iOS app
        if (statusCode != HttpStatus.OK ){
            // Enroll endpoint may return 404 and 400 errors
            logger.debug("Response of HTTP " + statusCode);

            JSONObject json = new JSONObject();
            try {
                json.put("error", String.valueOf(statusCode));
            } catch (JSONException e) {
                logger.error(e);
            }

            // end connection and return
            client.getConnectionManager().shutdown();
            return json.toString();
        }
        InputStream inputStream = AndroidHttpClient
                .getUngzippedContent(response.getEntity());
        String strRes = IOUtils.toString(inputStream, Charset.defaultCharset());

        client.getConnectionManager().shutdown();

        return strRes;
    }

    /**
     * Returns GET url with appended parameters.
     * 
     * @param url
     * @param params
     * @return
     */
    public static String toGetUrl(String url, Bundle params) {
        if (params != null) {
            if (!url.endsWith("?")) {
                url = url + "?";
            }

            for (String key : params.keySet()) {
                url = url + key + "=" + params.getString(key) + "&";
            }
        }
        return url;
    }

    public List<HttpCookie> getCookies(String url, Bundle headers, boolean isGet)
        throws ParseException, ClientProtocolException, IOException {
        final DefaultHttpClient client = newClient();

        CookieStore cookieStore = new BasicCookieStore();
        HttpContext localContext = new BasicHttpContext();
        // Bind custom cookie store to the local context
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        HttpRequestBase get = isGet ? new HttpGet(url) : new HttpPost(url);

        // set request headers
        if (headers != null) {
            for (String key : headers.keySet()) {
                get.addHeader(key, headers.getString(key));
            }
        }
        // Pass local context as a parameter
        try {
            client.execute(get, localContext);
        }catch (Exception ex){
            logger.error(ex);
        }
        client.getConnectionManager().shutdown();
        List<HttpCookie> cookieList = new ArrayList<>();
        for(Cookie cookie : cookieStore.getCookies()){
            HttpCookie copy = OkHttpUtil.servletCookieFromApacheCookie(cookie);
            if( copy != null ){
                cookieList.add(copy);
            }
        }
        return cookieList;
    }

    public static class HttpResult {
        public String body;
        public int statusCode;
    }

    private DefaultHttpClient newClient() {
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                HttpVersion.HTTP_1_1);
        client.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
                System.getProperty("http.agent") + " " +
                        context.getString(R.string.app_name) + "/" +
                        BuildConfig.APPLICATION_ID + "/" +
                        BuildConfig.VERSION_NAME);
        return client;
    }
}
