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

/*
 * GlobalElementsNode.java
 *
 * Created on September 18, 2006, 3:07 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.abe.nodes;

import java.awt.Image;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author Samaresh
 */
public class ReferencedSchemasNode extends AbstractNode {
    
    /**
     * Creates a new instance of ReferencedSchemasNode
     */
    public ReferencedSchemasNode(ABEUIContext context,
            AXIDocument doc, List<Class> childFilters) {
        super(new ReferencedSchemas(context, doc, childFilters));
        setName(NbBundle.getMessage(ReferencedSchemasNode.class,
                "LBL_CategoryNode_ReferencedSchemasNode"));
    }
    
    public boolean canRename() {
        return false;
    }
    
    public boolean canDestroy() {
        return false;
    }
    
    public boolean canCut() {
        return false;
    }
    
    public boolean canCopy() {
        return false;
    }
    
    public Image getOpenedIcon(int i) {
        return org.netbeans.modules.xml.schema.ui.nodes.categorized.
                CategorizedChildren.getBadgedFolderIcon(i, SchemaModelReference.class);
    }
    
    public Image getIcon(int i) {
        return org.netbeans.modules.xml.schema.ui.nodes.categorized.
                CategorizedChildren.getOpenedBadgedFolderIcon(i, SchemaModelReference.class);
    }    
    
    private static class ReferencedSchemas extends Children.Keys {
        ReferencedSchemas(ABEUIContext context,
                AXIDocument doc, List<Class> childFilters) {
            super();
            this.context = context;
            this.doc = doc;
            this.childFilters = childFilters;
        }
        protected Node[] createNodes(Object key) {
            if(key instanceof AXIModel) {
                Node node = null;
                AXIDocument refDoc = ((AXIModel)key).getRoot();
                if(isIncludedSchema(refDoc)) {
                    node = new IncludeNode(context, refDoc, childFilters);
                } else {
                    node = new ImportNode(context, refDoc, childFilters);
                }
                return new Node[] {node};
            }
            assert false;
            return new Node[]{};
        }
        
        protected void addNotify() {
            setKeys(doc.getModel().getReferencedModels());
        }
        
        private boolean isIncludedSchema(AXIDocument thisDoc) {
            AXIDocument original = context.getModel().getRoot();
            if(original.getTargetNamespace().equals(thisDoc.getTargetNamespace())) {
                return true;
            }
            
            return false;
        }
        
        private AXIDocument doc;
        private ABEUIContext context;
        private List<Class> childFilters;
    }
}
