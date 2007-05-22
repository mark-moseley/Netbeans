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

package org.netbeans.spi.project.ui.support;

import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.nodes.Node;

/**
 * Represents a series of nodes which can be spliced into a children list.
 * @param K the type of key you would like to use to represent nodes
 * @author mkleint
 * @since org.netbeans.modules.projectuiapi/1 1.18
 * @see NodeFactory
 * @see NodeFactorySupport
 * @see org.openide.nodes.Children.Keys
 */
public interface NodeList<K> {
    /**
     * Obtains child keys which will be passed to {@link #node}.
     * If there is a change in the set of keys based on external events,
     * fire a <code>ChangeEvent</code>.
     * @return list of zero or more keys to display
     */
    List<K> keys();
    /**
     * Adds a listener to a change in keys.
     * @param l a listener to add
     */
    void addChangeListener(ChangeListener l);
    /**
     * Removes a change listener.
     * @param l a listener to remove
     */
    void removeChangeListener(ChangeListener l);
    /**
     * Creates a node for a given key.
     * @param key a key which was included in {@link #keys}
     * @return a node which should represent that key visually
     */
    Node node(K key);
    /**
     * Called when the node list is to be active.
     * If there is any need to register listeners or begin caching of state, do it here.
     * @see org.openide.nodes.Children#addNotify
     */
    void addNotify();
    /**
     * Called when the node list is to be deactivated.
     * Unregister any listeners and perform any general cleanup.
     * @see org.openide.nodes.Children#removeNotify
     */
    void removeNotify();
}
