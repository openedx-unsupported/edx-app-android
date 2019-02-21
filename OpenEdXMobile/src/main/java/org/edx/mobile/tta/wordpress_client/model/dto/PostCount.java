package org.edx.mobile.tta.wordpress_client.model.dto;

/**
 * @author Arjun Singh
 *         Created on 2016/04/29.
 */
public class PostCount {

    /*
    * {"publish":"1","draft":0,"privatePub":0}
    */

    /**
     * Page count for published posts
     */
    public int publish;

    /**
     * Page count for draft posts
     */
    public int draft;

    /**
     * Page count for privately published posts
     */
    public int privatePub;

}
