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
 * Software is Sun Microsystems, Inc. Portions Copyright 2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.junit.wizards;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 * Displays folders and Java source files under a source node.
 * @author Marian Petras, Jesse Glick
 */
public class JavaChildren extends FilterNode.Children {

    private static final String JAVA_MIME_TYPE = "text/x-java";         //NOI18N

    public JavaChildren(Node parent) {
        super(parent);
    }

    @Override
    protected Node[] createNodes(Node originalNode) {
        Node newNode;
        
        DataObject dataObj = originalNode.getCookie(DataObject.class);
        if (dataObj == null) {
            newNode = copyNode(originalNode);
        } else {
            FileObject primaryFile = dataObj.getPrimaryFile();
            if (primaryFile.isFolder()) {
                newNode = new FilterNode(originalNode, new JavaChildren(originalNode));
            } else if (primaryFile.getMIMEType().equals(JAVA_MIME_TYPE)) {
                newNode = new FilterNode(originalNode, Children.LEAF);
                newNode.setDisplayName(primaryFile.getName());
            } else {
                newNode = null;
            }
        }

        return (newNode != null) ? new Node[] {newNode}
                                 : new Node[0];
    }
    
}
