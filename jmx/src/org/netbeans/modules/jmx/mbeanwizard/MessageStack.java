/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.mbeanwizard;

/**
 * Manager of layered messages.
 * It serves for management of messages layered over. When a message is to be
 * displayed, it is put into a certain layer. When there is another message
 * set in an upper layer, the topmost message does not change. Conversely,
 * when the topmost message is cleared, another message becomes the topmost
 * message and needs to be displayed. This class handles all these changes
 * and returns information what message needs to be displayed after each
 * addition/removal of message.
 * <p>
 * The stack has a fixed number of layers
 * (specified by calling the constructor). Each layer has its number,
 * beginning with <code>0</code> (the topmost layer) and ending with
 * <code><var>n</var> - 1</code> where <code><var>n</var></code>
 * is the number of layers.
 * <p>
 * There is one special layer for displaying volatile messages.
 * Volatile messages are those messages that are always displayed
 * on the top of all other messages but are cleared/overwritten
 * as soon as another message is to be displayed.
 *
 * @see  #setMessage
 * @author Marian Petras
 */
public final class MessageStack {
    
    /*
     * The class is final only for performance reasons.
     */
    
    /** layer number of the special layer for volatile messages */
    public static final int LAYER_VOLATILE = -1;
    
    /**
     * messages in layers (index <code>0</code> means the topmost layer)
     */
    private final String[] messageLayers;
    /**
     * index of the visible (topmost non-empty) layer.
     * If the stack is empty, it has value <code>-1</code>.
     */
    private int visibleLayerIndex = -1;
    /** currently displayed volatile message (or <code>null</code>) */
    private String volatileMsg;
    
    /**
     * Creates a new <code>MessageStack</code> with a given number of layers.
     *
     * @param  size  number of layers in the stack
     */
    public MessageStack(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException(
                    "number of layers must be positive");               //NOI18N
        }
        messageLayers = new String[size];
    }

    /**
     * Returns the currently &quot;displayed&quot; message.
     *
     * @return  message that should be currently displayed according to
     *          performed display modifications;
     *          or <code>null</code> if no message should be displayed
     */
    public String getDisplayedMessage() {
        return volatileMsg != null ? volatileMsg
                                   : getTopmostStackMessage();
    }
    
    /**
     * Returns the topmost message on the stack.
     *
     * @return  the topmost non-empty message on the stack;
     *          or <code>null</code> if the stack is empty
     */
    private String getTopmostStackMessage() {
        return visibleLayerIndex != -1 ? messageLayers[visibleLayerIndex]
                                       : null;
    }
    
    /**
     * Clears this message stack.
     * After cleaning, both volatile message and all of the stack messages
     * are empty.
     */
    public void clear() {
        volatileMsg = null;
        if (visibleLayerIndex != -1) {
            for (int i = visibleLayerIndex; i < messageLayers.length; i++) {
                messageLayers[i] = null;
            }
            visibleLayerIndex = -1;
        }
    }

    /**
     * Puts a message on the given layer of the stack.
     * Putting a message on the special layer for volatile messages
     * (layer number <code>LAYER_VOLATILE</code>) has the same effect
     * as calling {@link #setVolatileMessage(String)}.
     *
     * @param  layer  layer number
     * @param  message  message to be displayed, or <code>null</code>
     *                  if the existing message should be removed from the layer
     * @return  message that needs to be displayed in order to show the topmost
     *          message of the (updated) stack, or <code>null</code> if no
     *          change of display is necessary
     * @exception  java.lang.IllegalArgumentException
     *             if value of the <code>msgType</code> parameter is illegal
     */
    public String setMessage(final int layer, String message)
            throws IllegalArgumentException {
        
        /* check parameters: */
        if (layer == LAYER_VOLATILE) {
            return setVolatileMessage(message);
        }
        if (layer < 0 || layer >= messageLayers.length) {
            throw new IllegalArgumentException(
                    java.text.MessageFormat.format(
                        "Message type out of bounds (0 .. {1}): {0}",   //NOI18N
                        new Object[] { new Integer(layer),
                                       new Integer(messageLayers.length) }));
        }
        
        /* unify parameters: */
        if ((message != null) && (message.trim().length() == 0)) {
            message = null;
        }

        final String oldDisplayed = getDisplayedMessage();

        /* update the register of messages: */
        volatileMsg = null;
        messageLayers[layer] = message;

        /* update the visible layer index: */
        if (message != null) {
            if ((visibleLayerIndex == -1) || (layer < visibleLayerIndex)) {
                visibleLayerIndex = layer;
            }
        } else if (layer == visibleLayerIndex) {
            for (int i = layer + 1; i < messageLayers.length; i++) {
                if (messageLayers[i] != null) {
                    visibleLayerIndex = i;
                    break;
                }
            }
            if (visibleLayerIndex == layer) {    //no visible layer found
                visibleLayerIndex = -1;
            }
        }

        /* compare the old and new display: */
        return checkDisplayModified(oldDisplayed, getDisplayedMessage());
    }

    /**
     * Clears a message on the given layer of the stack.
     * Calling this method has the same effect as calling
     * <code>setMessage(layer, null)</code> (on the same layer).
     *
     * @param  layer  layer number
     * @return  message that needs to be displayed in order to show the topmost
     *          message of the (updated) stack, or <code>null</code> if no
     *          change of display is necessary
     * @see  #setMessage
     */
    public String clearMessage(final int layer) {
        return setMessage(layer, null);
    }
    
    /**
     * Displays a volatile message on top of all existing messages.
     *
     * @param  message to be displayed, or <code>null</code>
     *         if the previous volatile message should be removed
     * @return  message that needs to be displayed in order to show the message
     *          on the top of the stack, or <code>null</code> if no change
     *          of display is necessary
     */
    public String setVolatileMessage(String message) {
        
        /* unify parameters: */
        if ((message != null) && (message.trim().length() == 0)) {
            message = null;
        }

        final String oldDisplayed = getDisplayedMessage();

        /* update the register of messages: */
        volatileMsg = message;

        /* compare the old and new display: */
        return checkDisplayModified(oldDisplayed, getDisplayedMessage());
    }
    
    /**
     * Clears the previous volatile message (if any).
     * Calling this method has the same effect as calling
     * <code>setVolatileMessage(null)</code>.
     *
     * @return  message that needs to be displayed in order to show the message
     *          on the top of the stack or an empty string
     *          (<code>&quot;&quot;</code>) if no message should be displayed
     * @see  #setVolatileMessage
     */
    public String clearVolatileMessage() {
        return setVolatileMessage(null);
    }

    /**
     * Compares the previous and the new displayed message.
     *
     * @return <code>null</code> if both messages are the same (possibly empty),
     *         an empty string (<code>&quot;&quot;</code>) if the previous
     *         message was non-empty and the new message is empty,
     *         or the new message if it is non-empty and different from
     *         the old message
     */
    private String checkDisplayModified(String oldDisplay, String newDisplay) {
        if ((newDisplay == null) && (oldDisplay != null)) {
            return "";                                                  //NOI18N
        }
        if ((newDisplay != null) && !newDisplay.equals(oldDisplay)) {
            return newDisplay;
        }
        return null;
    }
    
}
