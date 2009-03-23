/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.kenai.ui.spi;

import java.beans.PropertyChangeListener;

/**
 * Abstraction of messaging info associated with a kenai project.
 * 
 * @author S. Aubrecht
 */
public abstract class MessagingHandle {

    /**
     * The name of Integer property which is fired when the count of online members
     * has changed for this project. The property value is the new count of online members.
     */
    public static final String PROP_ONLINE_COUNT = "onlineCount"; // NOI18N
    /**
     * The name of Integer property which is fired when the count of messages
     * has changed for this project. The property value is the new count of messages.
     */
    public static final String PROP_MESSAGE_COUNT = "messageCount"; // NOI18N

    /**
     * @return Number of online project members or -1 if the user isn't logged in
     * or the user isn't a member of project this handle is associated with.
     */
    public abstract int getOnlineCount();

    /**
     *
     * @return Number of available messages or -1 if the user isn't logged in
     * or the user isn't a member of project this handle is associated with.
     */
    public abstract int getMessageCount();

    public abstract void addPropertyChangeListener( PropertyChangeListener l );

    public abstract void removePropertyChangeListener( PropertyChangeListener l );
}
