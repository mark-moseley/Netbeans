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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.editor.ext.html.parser;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author marek
 */
public class AstPath {

    private AstNode first,  last;
    
    /** @param first may be null; in such case a path from the root is created */
    AstPath(AstNode first, AstNode last) {
        if(first != null && !isDescendant(first, last)) {
            throw new IllegalArgumentException("AstNode " + last + " is not an ancestor of AstNode " + first);
        }
        this.first = first;
        this.last = last;
    }

    public AstNode first() {
        return first;
    }
    
    public AstNode last() {
        return last;
    }
     
    /** returns a list of nodes from the first node to the last node including the boundaries. */
    public List<AstNode> path() {
        List<AstNode> path = new  ArrayList<AstNode>();
        AstNode node = last;
        while (node != null) {
            path.add(node);
            if(node == first) {
                break;
            }
            node = node.parent();
        }
        return path;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(AstNode node : path()) {
            AstNode parent = node.parent();
            //int myIndex = parent == null ? 0 : node.parent().children().indexOf(node);
            int myIndex = parent == null ? 0 : indexInSimilarNodes(node.parent(), node);
            sb.append(node.name() + "[" + node.type() + "]( " + myIndex + ")/");
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof AstPath)) {
            return false;
        }
        AstPath path = (AstPath)o;
        
        List<AstNode> p1 = path();
        List<AstNode> p2 = path.path();
        
        if(p1.size() != p2.size()) {
            return false;
        }
        
        for(int i = 0; i < p1.size(); i++) {
            AstNode n1 = p1.get(i);
            AstNode n2 = p2.get(i);
            
            AstNode n1Parent = n1.parent();
            AstNode n2Parent = n2.parent();
            
            if(n1Parent == null && n2Parent == null) {
                continue;
            }
            
//            int n1Index = n1Parent.children().indexOf(n1);
//            int n2Index = n2Parent.children().indexOf(n2);
            int n1Index = indexInSimilarNodes(n1Parent, n1);
            int n2Index = indexInSimilarNodes(n2Parent, n2);
            
            if(n1Index != n2Index) {
                return false;
            }
            
            if(!n1.signature().equals(n2.signature())) {
                return false;
            }
            
        }
        
        return true;
    }

    private int indexInSimilarNodes(AstNode parent, AstNode node) {
        int index = -1;
        for(AstNode child : parent.children()) {
            if(child.signature().equals(node.signature())) {
                index++;
            }
            if(child == node) {
                break;
            }
        }
        return index;
    }
    
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + (this.first != null ? this.first.hashCode() : 0);
        hash = 47 * hash + (this.last != null ? this.last.hashCode() : 0);
        return hash;
    }
    
    public static boolean isDescendant(AstNode amcestor, AstNode descendant) {
        if(amcestor == descendant) {
            return false;
        }
        AstNode node = descendant;
        while((node = node.parent()) != null) {
            if(amcestor == node) {
                return true;
            }
        }
        return false;
    }
    
}
