package org.edx.mobile.module.serverapi.http;

import android.net.http.AndroidHttpClient;
import android.os.Bundle;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.module.serverapi.IRequest;
import org.edx.mobile.module.serverapi.IResponse;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rohan on 2/6/15.
 */
class IHttpImpl implements IHttp {

    private static final String KEY_SET_COOKIE      = "Set-Cookie";
    private static final String KEY_CSRFTOKEN       = "CSRFToken";
    private static final String CONTENT_TYPE_JSON   = "application/json";
    public static Logger logger = new Logger(IHttpImpl.class);

    @Override
    public IResponse get(IRequest request) throws IOException {
        HttpClient client = getHttpClient();

        HttpGet get = new HttpGet(toGETUrl(request.getEndpoint(), request.getParameters()));
        // allow GZip
        AndroidHttpClient.modifyRequestToAcceptGzipResponse(get);
        // allow redirects
        HttpClientParams.setRedirecting(get.getParams(), true);

        // set request headers
        setHeaders(request, get);

        // being a GET, parameters are already appended to the endpoint

        // execute request
        HttpResponse response = client.execute(get);
        IResponse result = getResponse(response);

        // shutdown the client
        client.getConnectionManager().shutdown();

        return result;
    }

    @Override
    public IResponse post(IRequest request) throws IOException {
        HttpPost post = new HttpPost(request.getEndpoint());
        return executePost(request, post);
    }

    @Override
    public IResponse patch(IRequest request) throws IOException {
        HttpPost patch = new HttpPatch(request.getEndpoint());
        return executePost(request, patch);
    }

    /**
     * Returns GET url with appended parameters.
     *
     * @param url
     * @param params
     * @return
     */
    private static String toGETUrl(String url, Bundle params) {
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
     * Returns {@link org.edx.mobile.module.serverapi.IResponse} representation of
     * the given {@link org.apache.http.HttpResponse} object.
     * @param response
     * @return
     * @throws IOException
     */
    private IResponse getResponse(HttpResponse response) throws IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        logger.debug("StatusCode for get request= " + statusCode);

        // read cookies
        Bundle cookies = new Bundle();
        if(response.containsHeader(KEY_SET_COOKIE)){
            Header header = response.getFirstHeader(KEY_SET_COOKIE);
            cookies.putString(header.getName(), header.getValue());

            // prepare for "Cookie" and "X-CSRFToken" elements
            HeaderElement[] elements = header.getElements();
            if (elements != null) {
                for (int i = 0; i < elements.length; i++) {
                    HeaderElement element = elements[i];
                    if (element.getName().equalsIgnoreCase(KEY_CSRFTOKEN)) {
                        cookies.putString(KEY_COOKIE, element.getName()
                                + "=" + element.getValue());
                        cookies.putString(KEY_X_CSRFTOKEN, element.getValue());
                    }
                }
            }
        }

        InputStream inputStream = AndroidHttpClient
                .getUngzippedContent(response.getEntity());
        String strResponse = IOUtils.toString(inputStream);

        // prepare response object and return
        IResponseImpl result = new IResponseImpl();
        result.setStatusCode(statusCode);
        result.setResponse(strResponse);
        result.setCookies(cookies);

        return result;
    }

    /**
     * Returns HttpClient instance initialized with HTTP 1.1 and redirects enabled.
     * @return
     */
    private HttpClient getHttpClient() {
        DefaultHttpClient client = new DefaultHttpClient();
        client.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
                HttpVersion.HTTP_1_1);
        HttpClientParams.setRedirecting(client.getParams(), true);
        return client;
    }

    /**
     * Sets headers to the httpRequest.
     * @param request
     * @param httpRequest
     */
    private void setHeaders(IRequest request, HttpRequest httpRequest) {
        // set request headers
        if (request.getHeaders() != null) {
            Bundle headers = request.getHeaders();
            for (String key : headers.keySet()) {
                httpRequest.setHeader(key, headers.getString(key));
            }
        }
    }

    /**
     * Executes a POST request.
     * @param request
     * @param post
     * @return
     * @throws IOException
     */
    private IResponse executePost(IRequest request, HttpPost post) throws IOException {
        HttpClient client = getHttpClient();

        // allow GZip
        AndroidHttpClient.modifyRequestToAcceptGzipResponse(post);
        // allow redirects
        HttpClientParams.setRedirecting(post.getParams(), true);

        // set request headers
        setHeaders(request, post);

        // set request parameters
        if (request.getParameters() != null) {
            Bundle params = request.getParameters();
            List<NameValuePair> pairs = new ArrayList<>();
            for (String key : params.keySet()) {
                pairs.add(new BasicNameValuePair(key, params.getString(key)));
            }
            post.setEntity(new UrlEncodedFormEntity(pairs));
        }

        // set request body if provided in request
        if (request.getPostBody() != null) {
            StringEntity se = new StringEntity(request.getPostBody());
            se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, CONTENT_TYPE_JSON));
            post.setEntity(se);
        }

        // execute request
        HttpResponse httpResponse = client.execute(post);
        IResponse response = getResponse(httpResponse);

        // shutdown the client
        client.getConnectionManager().shutdown();

        return response;
    }
}
