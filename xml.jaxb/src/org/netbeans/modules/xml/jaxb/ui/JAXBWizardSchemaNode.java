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

package org.netbeans.modules.xml.jaxb.ui;

import java.awt.Image;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.jaxb.actions.JAXBDeleteSchemaAction;
import org.netbeans.modules.xml.jaxb.actions.OpenJAXBCustomizerAction;
import org.netbeans.modules.xml.jaxb.cfg.schema.Binding;
import org.netbeans.modules.xml.jaxb.cfg.schema.Bindings;
import org.netbeans.modules.xml.jaxb.cfg.schema.Catalog;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.netbeans.modules.xml.jaxb.cfg.schema.Schema;
import org.netbeans.modules.xml.jaxb.cfg.schema.SchemaSource;
import org.netbeans.modules.xml.jaxb.cfg.schema.SchemaSources;
import org.netbeans.modules.xml.jaxb.util.ProjectHelper;
import org.openide.actions.DeleteAction;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * @author lgao
 * @author $Author$ 
 */
public class JAXBWizardSchemaNode extends AbstractNode {
    private String schemaName;
    private Project project;
    private Schema schema;
    private static Action[] actions = null;
    
    public JAXBWizardSchemaNode(Project project, Schema schema) {
        this(project, schema, new InstanceContent());
        this.schemaName = schema.getName();
        this.project = project;
        this.schema = schema;
        this.initActions();                    
    }

    private JAXBWizardSchemaNode(Project project, Schema schema, InstanceContent content) {
        super(new JAXBWizardSchemaNodeChildren( project, schema ), 
                new AbstractLookup(content));
        // adds the node to our own lookup
        content.add (this);
        // adds additional items to the lookup
        //content.add (...);
    }
    
    public Action[] getActions(boolean b) {
        return actions;
    }
    
    public Schema getSchema(){
        return this.schema;
    }

    public void setSchema(Schema schm){
        this.schema = schm;
        Children c = this.getChildren();
        if (c instanceof JAXBWizardSchemaNodeChildren){
            JAXBWizardSchemaNodeChildren jaxbC = (JAXBWizardSchemaNodeChildren)
                    c;
            jaxbC.setSchema(this.schema);
        }
    }
    
    public Project getProject(){
        return this.project;
    }    
    private void initActions() {
        if ( actions == null ) {
            actions = new Action[] {
                SystemAction.get(OpenJAXBCustomizerAction.class),
                null,
                SystemAction.get(DeleteAction.class)
            };
        }
    }
    
    public String getDisplayName() {
        return schemaName;
    }
    
    public boolean canDestroy(){
        return true;
    }
    
    public void destroy() throws IOException {
        super.destroy();
        // Delete schema
        JAXBDeleteSchemaAction delAction = SystemAction.get(
                                                JAXBDeleteSchemaAction.class);
        delAction.performAction(new Node[] {this});
    }
    
    public Image getIcon(int type) {
        return Utilities.loadImage( "org/netbeans/modules/xml/jaxb/resources/package.gif" ); // No I18N
    }
    
    public Image getOpenedIcon(int type) {
        return Utilities.loadImage( "org/netbeans/modules/xml/jaxb/resources/packageOpen.gif" ); // No I18N
    }
    
    public static class JAXBWizardSchemaNodeChildren extends Children.Keys{
        private Project project;
        private Schema schema;
        
        public JAXBWizardSchemaNodeChildren(Project prj, Schema schema) {
            super();
            this.schema = schema;
            this.project = prj;
            this.addNodify();
        }

        public void addNodify() {
            updateKeys();
            super.addNotify();
        }        

        public void removeNotify() {
        }
                
        private void updateKeys(){
            List childrenNodes = new ArrayList();
            SchemaSources sss = this.schema.getSchemaSources();
            SchemaSource[] ss = sss.getSchemaSource();
            Bindings bs  = this.schema.getBindings();
            Catalog cat = this.schema.getCatalog();
            
            if (ss != null){
                for (int i = 0; i < ss.length; i++){
                     childrenNodes.add(ss[i]);
                }
            }
            
            if ((bs != null) && (bs.sizeBinding() > 0)){
                Binding[] binding = bs.getBinding();
                for (int i = 0; i < binding.length; i++){
                     childrenNodes.add(binding[i]);
                }
            }
            
            if ((cat != null) && (cat.getLocation() != null)){
                   childrenNodes.add(cat);
            }
            
            super.setKeys(childrenNodes );
        }
        
        public void setSchema(Schema schm){
            this.schema = schm;
            updateKeys();
        }
       
        public void refreshChildren(){
            updateKeys();
        }
        
        protected Node[] createNodes(Object key) {
            Node[] xsdNodes = null;
            try {
                FileObject prjRoot = project.getProjectDirectory();                    
                FileObject xsdFolder = 
                        ProjectHelper.getFOProjectSchemaDir(project);
                File projDir = FileUtil.toFile(prjRoot);
                FileObject locSchemaRoot = 
                        xsdFolder.getFileObject(schema.getName());
                
                Node xsdNode = null;
                FileObject fo = null;

                if ( key instanceof SchemaSource ) {
                    SchemaSource ss = (SchemaSource) key;
                    File tmpFile = null;
                    String originLocType = null;
                    Boolean isURL = Boolean.FALSE;
                    
                    originLocType = ss.getOrigLocationType();
                    if ((originLocType != null) && 
                            ("url".equals(originLocType))){ //NOI18N
                        isURL = Boolean.TRUE;
                    }

                    fo = FileUtil.toFileObject(new File(projDir, 
                            ss.getLocation()));
                    if (fo != null){
                        xsdNode = new JAXBBindingSupportFileNode(project, fo, 
                                locSchemaRoot, isURL, ss.getOrigLocation());
                        try {
                            DataObject dataObj = DataObject.find(fo);
                            Node delegate = dataObj.getNodeDelegate();
                            ((JAXBBindingSupportFileNode)xsdNode).
                                    setNodeDelegate(delegate);
                        } catch (DataObjectNotFoundException ex){
                            // Use JAXBBindingSupportFileNode
                        }
                        
                    } else {
                        // Log
                        tmpFile = new File(ss.getLocation());
                        fo = xsdFolder.getFileObject(tmpFile.getName());
                        if (fo != null){
                            xsdNode = new JAXBBindingSupportFileNode(project, 
                                    fo, locSchemaRoot, isURL, 
                                    ss.getOrigLocation());
                        }
                    }    
                }
                
                if (key instanceof Binding ) {
                    Binding bndg = (Binding)key;
                    fo = FileUtil.toFileObject(new File(projDir, 
                            bndg.getLocation()));
                    if (fo != null){
                        xsdNode = new JAXBBindingSupportFileNode(project, 
                                fo, locSchemaRoot, false, 
                                bndg.getOrigLocation());                        
                        try {
                            DataObject dataObj = DataObject.find(fo);
                            Node delegate = dataObj.getNodeDelegate();
                            ((JAXBBindingSupportFileNode)xsdNode).
                                    setNodeDelegate(delegate);
                        } catch (DataObjectNotFoundException ex){
                            // Use JAXBBindingSupportFileNode
                        }
                    }                    
                }
                
                if (key instanceof Catalog ) {
                    Catalog cat = (Catalog) key;
                    fo = FileUtil.toFileObject(new File(projDir, 
                            cat.getLocation()));
                    if (fo != null){
                        xsdNode = new JAXBBindingSupportFileNode(project, 
                                fo, locSchemaRoot, false, 
                                cat.getOrigLocation());
                        try {
                            DataObject dataObj = DataObject.find(fo);
                            Node delegate  = dataObj.getNodeDelegate();
                            ((JAXBBindingSupportFileNode)xsdNode).
                                    setNodeDelegate(delegate);                            
                        } catch (DataObjectNotFoundException ex){
                            // Use JAXBBindingSupportFileNode
                        }
                    }
                }
                
                if (xsdNode != null){
                    xsdNodes = new Node[]{xsdNode};
                }                
            } catch ( IntrospectionException inse ) {
                Exceptions.printStackTrace(inse);
            }
            
            return xsdNodes;
        }
    }    
}
