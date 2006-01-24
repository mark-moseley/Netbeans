/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.subversion.ui.browser;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 * Subclassed to get custom icon.
 *
 * @author Petr Kuzel
 */
final class WaitNode extends AbstractNode {

    public WaitNode(String name) {
        super(Children.LEAF);
        setDisplayName(name);
        setIconBaseWithExtension("org/netbeans/modules/subversion/ui/browser/wait.gif");  // NOI18N
    }
}
