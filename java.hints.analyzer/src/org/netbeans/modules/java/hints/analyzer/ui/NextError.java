/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.java.hints.analyzer.ui;
import java.awt.event.ActionEvent;
import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.AbstractAction;
import org.netbeans.api.java.source.UiUtils;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.nodes.FilterNode.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class NextError extends AbstractAction {

    private AnalyzerTopComponent comp;

    public NextError(AnalyzerTopComponent comp) {
        this.comp = comp;
    }
    
    @Override
    public boolean isEnabled() {
        Node node = getNextMeaningfullNode();
        boolean enabled = node != null;
        
        if (node != null) {
            comp.nodesForNext.add(0, node);
        }
        
        return enabled;
    }

    public void actionPerformed(ActionEvent e) {
        Node node = getNextMeaningfullNode();
        
        if (node == null) {
            //should not happen
            fireEnabledChanged();
            return ;
        }
        
        FixDescription fd = node.getLookup().lookup(FixDescription.class);
            
        assert fd != null;
        
        addToSeenNodes(node);
        
        try {
            comp.getExplorerManager().setSelectedNodes(new Node[]{node});
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        if (comp.fixOnNext() && !fd.isFixed()) {
            try {
                fd.implement();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        ErrorDescription ed = fd.getErrors();

        UiUtils.open(ed.getFile(), ed.getRange().getBegin().getOffset());
        fireEnabledChanged();
    }
    
    private Node getNextMeaningfullNode() {
        if (comp.nodesForNext == null) {
            comp.nodesForNext = new LinkedList<Node>();
            comp.nodesForNext.add(comp.getExplorerManager().getRootContext());
        }

        List<Node> nodesForNext = comp.nodesForNext;

        while (!nodesForNext.isEmpty()) {
            Node top = nodesForNext.remove(0);

            if (top.getChildren() != Children.LEAF) {
                nodesForNext.addAll(0, Arrays.asList(top.getChildren().getNodes(true)));
                continue;
            }

            FixDescription fd = top.getLookup().lookup(FixDescription.class);

            if (fd != null) {
                Node[] selected = comp.getExplorerManager().getSelectedNodes();

                if (selected.length == 1 && selected[0] == top) {
                    addToSeenNodes(top);
                    continue;
                }
                
                if (comp.goOverFixed() && !fd.isFixed()) {
                    addToSeenNodes(top);
                    continue;
                }
                
                return top;
            }
        }
        
        return null;
    }

    private void addToSeenNodes(Node n) {
        if (comp.seenNodes == null) {
            comp.seenNodes = new LinkedList<Node>();
        }

        comp.seenNodes.add(0, n);
    }
    
    void fireEnabledChanged() {
        firePropertyChange("enabled", null, isEnabled());
    }
    
}
