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
package org.netbeans.modules.subversion.ui.history;

import org.openide.nodes.Children;
import org.openide.nodes.Node;

import java.util.*;

/**
 * Represents children of a Revision Node in Search history results table.
 *
 * @author Maros Sandor
 */
class RevisionNodeChildren extends Children.Keys {

    private RepositoryRevision container;

    public RevisionNodeChildren(RepositoryRevision container) {
        this.container = container;
    }

    protected void addNotify() {
        refreshKeys();
    }

    protected void removeNotify() {
        setKeys (Collections.EMPTY_SET);
    }
    
    private void refreshKeys() {
        setKeys(container.getEvents());
    }
    
    protected Node[] createNodes(Object key) {
        RepositoryRevision.Event fn = (RepositoryRevision.Event) key;
        RevisionNode node = new RevisionNode(fn);
        return new Node[] { node };
    }

    public void refreshChildren() {
        refreshKeys();
    }
}

