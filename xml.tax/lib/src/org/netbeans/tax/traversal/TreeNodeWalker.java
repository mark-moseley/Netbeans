/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.tax.traversal;

import org.netbeans.tax.TreeNode;
import org.netbeans.tax.NotSupportedException;

/**
 *
 * @author Libor Kramolis
 * @version 0.1
 */
public final class TreeNodeWalker {

    /** Root node of this iterator. */
    private TreeNode root;

    /** Determines which node types are presented. */
    private int whatToShow;

    /** Filter to screen nodes. */
    private TreeNodeFilter filter;

    /** Current node. */
    private TreeNode currentNode;


    //
    // init
    //

    /** Creates new TreeNodeIterator. */
    public TreeNodeWalker (TreeNode node, int wTS, TreeNodeFilter f) {
        root = node;
        whatToShow = wTS;
        filter = f;
        
        currentNode = root;
    }
    
    
    //
    // itself
    //
    
    /**
     */
    public TreeNode getRoot () {
        return root;
    }
    
    /**
     */
    public int getWhatToShow () {
        return whatToShow;
    }
    
    /**
     */
    public TreeNodeFilter getFilter () {
        return filter;
    }
    
    /**
     */
    public TreeNode getCurrentNode () {
        return currentNode;
    }
    
    /**
     */
    public void setCurrentNode (TreeNode curNode) throws NotSupportedException {
        if (curNode == null) {
            throw new NotSupportedException (Util.THIS.getString ("EXC_invalid_current_node_value"));
        }
        
        currentNode = curNode;
    }
    
    /**
     */
    public TreeNode parentNode () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.parentNode ()"); // NOI18N
        
        return null;
    }
    
    /**
     */
    public TreeNode firstChild () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.firstChild ()"); // NOI18N
        
        return null;
    }
    
    /**
     */
    public TreeNode lastChild () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.lastChild ()"); // NOI18N
        
        return null;
    }
    
    /**
     */
    public TreeNode previousSibling () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.previousSibling ()"); // NOI18N
        
        return null;
    }
    
    /**
     */
    public TreeNode nextSibling () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.nextSibling ()"); // NOI18N
        
        return null;
    }
    
    /**
     */
    public TreeNode previousNode () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.previousNode ()"); // NOI18N
        
        return null;
    }
    
    /**
     */
    public TreeNode nextNode () {
        if ( Util.THIS.isLoggable() ) /* then */ Util.THIS.debug ("[PENDING]: TreeNodeWalker.nextNode ()"); // NOI18N
        
        return null;
    }
    
}
