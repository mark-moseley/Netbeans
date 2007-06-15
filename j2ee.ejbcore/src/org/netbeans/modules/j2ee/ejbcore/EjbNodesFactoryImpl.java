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

package org.netbeans.modules.j2ee.ejbcore;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.entity.EntityNode;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.mdb.MessageNode;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.session.SessionNode;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbNodesFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Pavel Buzek
 */
public final class EjbNodesFactoryImpl implements EjbNodesFactory {
    
    public EjbNodesFactoryImpl() {
    }
    
    public Node createSessionNode(String ejbClass, EjbJar ejbModule, Project project) {
        return SessionNode.create(ejbClass, ejbModule, project);
    }
    
    public Node createEntityNode(String ejbClass, EjbJar ejbModule, Project project) {
        return EntityNode.create(ejbClass, ejbModule, project);
    }
    
    public Node createMessageNode(String ejbClass, EjbJar ejbModule, Project project) {
        return new MessageNode(ejbClass, ejbModule, project);
    }
}
