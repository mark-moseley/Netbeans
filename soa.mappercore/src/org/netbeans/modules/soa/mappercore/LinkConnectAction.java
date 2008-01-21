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

package org.netbeans.modules.soa.mappercore;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TargetPin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;

/**
 *
 * @author alex
 */
public class LinkConnectAction extends MapperKeyboardAction {
    private Canvas canvas;
    private TargetPin target;
    private SourcePin source;
    
    public LinkConnectAction(Canvas canvas) {
        this.canvas = canvas;
        target = null;
        source = null;
    }
    
    @Override
    public String getActionKey() {
        return "press-link-connect";
    }

    @Override
    public KeyStroke[] getShortcuts() {
        KeyStroke[] a = new KeyStroke[1];
          a[0] = KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK);
          return a;
    }

    public void actionPerformed(ActionEvent e) {
        SelectionModel selectionModel = canvas.getSelectionModel();
        TreePath treePath = selectionModel.getSelectedPath();
        if (treePath == null) return;
        
        List<Vertex> vertexes = selectionModel.getSelectedVerteces();
        if (vertexes != null && vertexes.size() != 0) {
            source = vertexes.get(0);
        }
        
        VertexItem vertexItem = selectionModel.getSelectedVertexItem();
        if (vertexItem != null) {
            target = vertexItem;
        }
              
        if (target != null && source != null) { 
            if (canvas.getMapperModel().canConnect(treePath, source, target, treePath, null)) {
                canvas.getMapperModel().connect(treePath, source,
                        target, treePath, null);
                target = null;
                source = null;
            }
        }
    }
}
