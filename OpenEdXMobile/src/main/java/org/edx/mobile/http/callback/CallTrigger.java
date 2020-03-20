package org.edx.mobile.http.callback;

import androidx.annotation.NonNull;

import org.edx.mobile.view.common.MessageType;

/**
 * The HTTP request type, for determining what kind of error
 * message to deliver to the callback.
 */
public enum CallTrigger {
    /**
     * A request initiated by a user action.
     */
    USER_ACTION(MessageType.DIALOG),
    /**
     * A request initiated to load some data, that's being cached
     * by the application.
     */
    LOADING_CACHED(MessageType.FLYIN_ERROR),
    /**
     * A request initiated to load some data, that's not being
     * cached by the application.
     */
    LOADING_UNCACHED(MessageType.FLYIN_ERROR);

    /**
     * The message type that's associated with the request type.
     */
    @NonNull
    private final MessageType messageType;

    /**
     * Create a new instance of an HTTP request type.
     *
     * @param messageType The message type that's associated with
     *                    the request type.
     */
    CallTrigger(@NonNull final MessageType messageType) {
        this.messageType = messageType;
    }

    /**
     * @return The message type that's associated with the request
     *         type.
     */
    @NonNull
    MessageType getMessageType() {
        return messageType;
    }
}
