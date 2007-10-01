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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.test.xml.schema.core.lib.dom.parser;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;
import org.w3c.dom.Node;

/**
 *
 * @author ca@netbeans.org
 */
public class NodeIterator {
    private Iterator m_iterator;
    private LinkedList<Node> m_trace = new LinkedList<Node>();
    
    /** Creates a new instance of NodeIterator */
    public NodeIterator(TreeMap<String, Node> map) {
        Collection c = map.values();
        m_iterator   = c.iterator();
    }
    
    public Node next() {
        boolean bSeekChildren = true;
        
        while (true) {
            if (m_trace.size() == 0) {
                Node node = null;
                if (m_iterator.hasNext()) {
                    node = (Node) m_iterator.next();
                    if (node != null) {
                        m_trace.add(node);
                    }
                }
                return node;
            } else {
                Node node = m_trace.getLast();
                Node nextNode = null;
                if (node.hasChildNodes() && bSeekChildren) {
                    nextNode = node.getFirstChild();
                    m_trace.add(nextNode);
                } else {
                    if (m_trace.size() > 1) {
                        nextNode = node.getNextSibling();
                    }
                    m_trace.removeLast();
                    if (nextNode != null) {
                        bSeekChildren = true;
                        m_trace.add(nextNode);
                    } else {
                        bSeekChildren = false;
                        continue;
                    }
                }
                
                return nextNode;
            }
        }
    }
}
