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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.cookies;

import org.openide.nodes.Node;

import java.io.IOException;

import java.util.EventListener;


/** Cookie that allows connection between two objects. Also supporting
* persistent connections.
*
* @author Jaroslav Tulach
 * @deprecated Should no longer be used.
*/
public interface ConnectionCookie extends Node.Cookie {
    /** Attaches new node to listen to events produced by this
    * event. The type must be one of event types supported by this
    * cookie and the listener should have ConnectionCookie.Listener cookie
    * attached so it can be notified when event of requested type occurs.
    *
    * @param type the type of event, must be supported by the cookie
    * @param listener the node that should be notified
    *
    * @exception InvalidObjectException if the type is not supported by the cookie
    * @exception IOException if the type is persistent and the listener does not
    *    have serializable handle (listener.getHandle () is null or its serialization
    *    throws an exception)
    */
    public void register(Type type, Node listener) throws IOException;

    /** Unregisters an listener.
    * @param type type of event to unregister the listener from listening to
    * @param listener to unregister
    * @exception IOException if there is I/O operation error when the removing
    *   the listener from persistent storage
    */
    public void unregister(Type type, Node listener) throws IOException;

    /** Unmutable set of types supported by this connection source.
    * @return a set of Type objects
    */
    public java.util.Set getTypes();

    /** Cookie that must be provided by a node that is willing to register
    * itself as a listener to a ConnectionCookie.
    */
    public interface Listener extends Node.Cookie, EventListener {
        /** Notifies that the an event happended.
        * @param ev event that describes the action
        * @exception IllegalArgumentException if the event is not of valid type, then the
        *    caller should call the listener no more
        * @exception ClassCastException if the event is not of valid type, then the
        *    caller should call the listener no more
        */
        public void notify(ConnectionCookie.Event ev) throws IllegalArgumentException, ClassCastException;
    }

    /** Interface describing cookie type of event a cookie can produce.
    */
    public interface Type extends java.io.Serializable {
        /** The class that is passed into the listener's <CODE>notify</CODE>
        * method when an event of this type is fired.
        *
        * @return event class
        */
        public Class getEventClass();

        /** Getter whether the registration to this type of event  is persistent
        * or is valid only till the source disappears (the IDE shutdowns).
        */
        public boolean isPersistent();

        // Jesse, please improve the comment. [Petr]

        /** Test whether the specified type could be accepted by this type.
        * This method is similar to <CODE>equals(Object)</CODE> method,
        * so default implementation could be delegated to it.
        * @return <CODE>true</CODE> if type is similar to this type.
        */
        public boolean overlaps(Type type);
    }

    /** Event that is fired to listeners.
    */
    public class Event extends java.util.EventObject {
        static final long serialVersionUID = 7177610435688865839L;
        private Type type;

        /** @param n the node that produced the action
        * @param t type of the event
        */
        public Event(Node n, Type t) {
            super(n);
            type = t;
        }

        /** Getter for the node that produced the action.
        * The node can be used to obtain additional information like cookies, etc.
        * @return the node
        */
        public Node getNode() {
            return (Node) getSource();
        }

        /** Getter for the type of the event.
        * There can be more types of events and the listener can compare
        * if two events are of the same type by using type1.equals (type2)
        *
        * @return type of the event
        */
        public Type getType() {
            return type;
        }
    }
}
