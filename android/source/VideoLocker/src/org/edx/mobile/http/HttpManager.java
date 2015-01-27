package org.edx.mobile.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.edx.mobile.logger.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.http.AndroidHttpClient;
import android.os.Bundle;

public class HttpManager {
    protected final Logger logger = new Logger(getClass().getName());

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
    public String get(String urlWithAppendedParams, Bundle headers)
            throws ParseException, ClientProtocolException, IOException {
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                HttpVersion.HTTP_1_1);
        
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
        String strRes = IOUtils.toString(inputStream);

        // String response =
        // EntityUtils.toString(client.execute(get).getEntity(), "UTF-8");
        client.getConnectionManager().shutdown();

        return strRes;
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
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                HttpVersion.HTTP_1_1);

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
        if (statusCode == 204) {
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
        } else if (statusCode == 401) {
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
        String strRes = IOUtils.toString(inputStream);

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
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                HttpVersion.HTTP_1_1);

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
        InputStream inputStream = AndroidHttpClient
                .getUngzippedContent(response.getEntity());
        String strRes = IOUtils.toString(inputStream);

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
    
    /**
     * Return http params with timeout.
     * @return
     */
    @Deprecated
    private HttpParams getHttpParameters() {
        HttpParams httpParameters = new BasicHttpParams();
        // Set the timeout in milliseconds until a connection is established.
        // The default value is zero, that means the timeout is not used. 
        int timeoutConnection = 60 * 1000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        // Set the default socket timeout (SO_TIMEOUT) 
        // in milliseconds which is the timeout for waiting for data.
        int timeoutSocket = 5000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        return httpParameters;
    }
    
    /**
     * Returns Header for a given URL.
     * 
     * @param url
     * @throws ParseException
     * @throws ClientProtocolException
     * @throws IOException
     */
    public org.apache.http.Header getRequestHeader(String url)
            throws ParseException, ClientProtocolException, IOException {
        HttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                HttpVersion.HTTP_1_1);

        HttpGet get = new HttpGet(url);

        HttpResponse response = client.execute(get);
        org.apache.http.Header header=null;
        if(response.containsHeader("Set-Cookie")){
            header = response.getFirstHeader("Set-Cookie");
        }
        
        client.getConnectionManager().shutdown();

        return header;
    }
}
