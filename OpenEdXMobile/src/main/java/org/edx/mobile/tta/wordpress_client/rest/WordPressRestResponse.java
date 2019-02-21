package org.edx.mobile.tta.wordpress_client.rest;

/**
 * @author Arjun Singh
 *         Created on 2016/01/14.
 */
public interface WordPressRestResponse<T> {

    void onSuccess(T result);

    void onFailure(HttpServerErrorResponse errorResponse);


}
