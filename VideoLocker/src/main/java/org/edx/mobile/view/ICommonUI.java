package org.edx.mobile.view;

/**
 * Created by hanning on 3/18/15.
 */
public interface ICommonUI {
    /**
     * we can block whole panel, or just part of the UI controls
     * @param enable
     * @return operation failed or not
     */
    boolean tryToSetUIInteraction(boolean enable);
}
