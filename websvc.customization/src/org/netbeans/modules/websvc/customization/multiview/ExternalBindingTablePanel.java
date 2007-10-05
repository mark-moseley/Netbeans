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
/*
 * ExternalBindingTablePanel.java
 *
 * Created on March 8, 2006, 9:42 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.config.Binding;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Roderico Cruz
 */
public class ExternalBindingTablePanel extends DefaultTablePanel{
    private static final String[] columnName = {NbBundle.getMessage(ExternalBindingTablePanel.class,
            "TITLE_CUSTOMIZATION_FILES")};
    private EBTableModel model;
    private String previousDirectory = "";
    private static final FileFilter XML_FILE_FILTER = new XmlFileFilter();
    private Node node;
    private JaxWsModel jmodel;
    private Map<String, FileObject> addedBindings;
    private RemoveActionListener removeActionListener;
    private AddActionListener addActionListener;
    
    /** Creates a new instance of ExternalBindingTablePanel */
    public ExternalBindingTablePanel(EBTableModel model,
            Node node, JaxWsModel jmodel) {
        super(model);
        this.model = model;
        this.node = node;
        this.jmodel = jmodel;
        this.editButton.setVisible(false); //can't edit an entry
        addedBindings = new HashMap<String, FileObject>();
        
        addActionListener = new AddActionListener();
        ActionListener addListener = (ActionListener)WeakListeners.create(ActionListener.class,
                addActionListener, addButton);
        addButton.addActionListener(addListener);
        
        removeActionListener = new RemoveActionListener();
        ActionListener removeListener = (ActionListener)WeakListeners.create(ActionListener.class,
                removeActionListener, removeButton);
        removeButton.addActionListener(removeListener);
    }
    
    public String getRelativePathToWsdl(){
        String relativePath = "";
        FileObject srcRoot = (FileObject)node.getLookup().lookup(FileObject.class);
        FileObject localWsdlFile = null;
        FileObject wsdlFolder = null;
        Client client = (Client)node.getLookup().lookup(Client.class);
        if(client != null){
            JAXWSClientSupport support = JAXWSClientSupport.getJaxWsClientSupport(srcRoot);
            wsdlFolder = support.getLocalWsdlFolderForClient(client.getName(),false);
            localWsdlFile =
                    wsdlFolder.getFileObject(client.getLocalWsdlFile());
            
        }
        else{
            JAXWSSupport support = JAXWSSupport.getJAXWSSupport(srcRoot);
            Service service = (Service)node.getLookup().lookup(Service.class);
            wsdlFolder = support.getLocalWsdlFolderForService(service.getName(), false);
            localWsdlFile =
                    wsdlFolder.getFileObject(service.getLocalWsdlFile());
            
        }
        try{
            relativePath = Utilities.relativize(FileUtil.toFile(wsdlFolder).toURI(),
                    new URI(localWsdlFile.getURL().toExternalForm()));
        }catch(Exception e){
            return "Unable to obtain relative path";
        }
        
        return "../" + relativePath;
    }
    
    public Map<String, FileObject> getAddedBindings(){
        return addedBindings;
    }
    
    public Set<String> getRemovedBindings(){
        Set<String> bindingsRemoved = new HashSet<String>();
        Binding[] bindingsInModel = null;
        Client client = (Client)node.getLookup().lookup(Client.class);
        if(client != null){
            bindingsInModel = client.getBindings();
        } else{
            Service service = (Service)node.getLookup().lookup(Service.class);
            if(service != null){
                bindingsInModel= service.getBindings();
            }
        }
        if(bindingsInModel == null){ //this can't happen
            return Collections.emptySet();
        }
        @SuppressWarnings("unchecked")
        List<String> bindingsInTable = model.getChildren();
        for(int i = 0; i < bindingsInModel.length; i++){
            String bindingInModel = bindingsInModel[i].getFileName();
            boolean found = false;
            for(String bindingInTable: bindingsInTable){
                if(bindingInTable.equals(bindingInModel)){
                    found = true;
                    break;
                }
            }
            if(!found){
                bindingsRemoved.add(bindingInModel);
            }
        }
        return bindingsRemoved;
    }
    
    public List getChildren(){
        return model.getChildren();
    }
    
    class RemoveActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            int row = getTable().getSelectedRow();
            if(row == -1) return;
            String fileName = (String)getTable().getValueAt(row, 0);
            if(confirmDeletion(fileName)){
                addedBindings.remove(fileName);
                ExternalBindingTablePanel.this.model.removeRow(row);
            }
        }
        
        private boolean confirmDeletion(String fileName) {
            NotifyDescriptor.Confirmation notifyDesc =
                    new NotifyDescriptor.Confirmation(NbBundle.getMessage
                    (ExternalBindingTablePanel.class, "MSG_CONFIRM_DELETE", fileName),
                    NotifyDescriptor.YES_NO_OPTION);
            DialogDisplayer.getDefault().notify(notifyDesc);
            return (notifyDesc.getValue() == NotifyDescriptor.YES_OPTION);
        }
    }
    
    class AddActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            //Display information about wsdlLocation
            NotifyDescriptor.Confirmation notifyDesc =
                    new NotifyDescriptor.Confirmation(NbBundle.getMessage
                    (ExternalBindingTablePanel.class, "MSG_EXTERNAL_BINDING", getRelativePathToWsdl()),
                    NotifyDescriptor.YES_NO_OPTION);
            DialogDisplayer.getDefault().notify(notifyDesc);
            if(notifyDesc.getValue() == NotifyDescriptor.NO_OPTION) return;
            
            JFileChooser chooser = new JFileChooser(previousDirectory);
            chooser.setMultiSelectionEnabled(false);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.addChoosableFileFilter(XML_FILE_FILTER);
            chooser.setFileFilter(XML_FILE_FILTER);
            
            if(chooser.showOpenDialog(ExternalBindingTablePanel.this) == JFileChooser.APPROVE_OPTION) {
                File bindingFile = chooser.getSelectedFile();
                if(bindingFile.exists()){
                    FileObject bindingFO = FileUtil.toFileObject(bindingFile);
                    String bindingName = bindingFO.getName();
                    bindingName = FileUtil.findFreeFileName(getBindingsFolder(node), bindingName, bindingFO.getExt());
                    bindingName = bindingFO.getExt().equals("") ? bindingName : bindingName + "." + bindingFO.getExt();
                    addedBindings.put(bindingName, bindingFO);
                    ExternalBindingTablePanel.this.model.addRow(bindingName);
                    previousDirectory = bindingFile.getPath();
                }
            }
        }
        private FileObject getBindingsFolder(Node node){
            FileObject srcRoot = (FileObject)node.getLookup().lookup(FileObject.class);
            assert srcRoot != null : "Cannot find srcRoot";
            FileObject bindingsFolder = null;
            Client client = (Client)node.getLookup().lookup(Client.class);
            if(client != null){
                JAXWSClientSupport support = JAXWSClientSupport.getJaxWsClientSupport(srcRoot);
                bindingsFolder = support.getBindingsFolderForClient(node.getName(), true);
            } else{
                Service service = (Service)node.getLookup().lookup(Service.class);
                if(service != null){
                    JAXWSSupport support = JAXWSSupport.getJAXWSSupport(srcRoot);
                    bindingsFolder = support.getBindingsFolderForService(node.getName(), true);
                }
            }
            return bindingsFolder;
        }
    }
    
    void populateModel(){
        model.setData(node);
    }
    
    public static class EBTableModel extends AbstractTableModel{
        
        List<String> children;
        public Object getValueAt(int row, int column) {
            return children.get(row);
        }
        
        public int getRowCount() {
            if(children != null){
                return children.size();
            }
            return 0;
        }
        
        public int getColumnCount() {
            return columnName.length;
        }
        
        public void removeRow(int row){
            children.remove(row);
            fireTableRowsDeleted(row, row);
        }
        
        public void addRow(String value){
            children.add(value);
            fireTableRowsInserted(getRowCount() - 1, getRowCount() - 1);
        }
        
        public void setData(Node node){
            children = new ArrayList<String>();
            List<String> list = new ArrayList<String>();
            Client client = (Client)node.getLookup().lookup(Client.class);
            if(client != null){
                Binding[] bindings = client.getBindings();
                for(int i = 0; i < bindings.length; i++){
                    list.add(bindings[i].getFileName());
                }
            } else{
                Service service = (Service)node.getLookup().lookup(Service.class);
                if(service != null){
                    Binding[] bindings = service.getBindings();
                    for(int i = 0; i < bindings.length; i++){
                        list.add(bindings[i].getFileName());
                    }
                }
            }
            children.addAll(list);
            this.fireTableDataChanged(); //do we need to do this?
        }
        
        public String getColumnName(int column) {
            return columnName[column];
        }
        
        public List getChildren(){
            return children;
        }
        
    }
    
    private static class XmlFileFilter extends FileFilter {
        public boolean accept(File f) {
            boolean result;
            if(f.isDirectory() || "xml".equalsIgnoreCase(FileUtil.getExtension(f.getName()))) { // NOI18N
                result = true;
            } else {
                result = false;
            }
            return result;
        }
        
        public String getDescription() {
           return NbBundle.getMessage(ExternalBindingTablePanel.class, "DESC_CUSTOMIZATION_FILE_FILTER");
        }
    }
}
