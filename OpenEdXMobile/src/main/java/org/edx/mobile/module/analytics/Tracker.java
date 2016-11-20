package org.edx.mobile.module.analytics;

public interface Tracker<P, T, O> {

    void resetIdentifyUser();

    /**
     * Calls track method of an Analytics provider.
     *
     * @param event
     * @param props
     */
    void track(String event, P props);

    /**
     * Calls screen method an Analytics provider.
     *
     * @param category
     * @param name
     */
    void screen(String category, String name, P properties);

    /**
     * Calls identify method of an Analytics provider.
     *
     * @param id
     * @param traits
     * @param options
     */
    void identify(String id, T traits, O options);
}
