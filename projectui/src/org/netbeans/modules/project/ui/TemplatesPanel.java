/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.project.ui;

import java.awt.Component;

import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.TemplateWizard;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  tom
 */
public class TemplatesPanel implements WizardDescriptor.Panel, TemplatesPanelGUI.Builder {
    
    private ArrayList listeners;
    private TemplatesPanelGUI panel;
    
    /** Creates a new instance of TemplatesPanel */
    public TemplatesPanel() {
    }
    
    public void readSettings (Object settings) {
        TemplateWizard wd = (TemplateWizard) settings;
        wd.putProperty ("WizardPanel_contentSelectedIndex", new Integer (0)); // NOI18N
        wd.putProperty ("WizardPanel_contentData", new String[] { // NOI18N
                NbBundle.getBundle (TemplatesPanel.class).getString ("LBL_TemplatesPanel_Name"), // NOI18N
                NbBundle.getBundle (TemplatesPanel.class).getString ("LBL_TemplatesPanel_Dots")}); // NOI18N
        FileObject templatesFolder = (FileObject) wd.getProperty (TemplatesPanelGUI.TEMPLATES_FOLDER);
        if (templatesFolder != null && templatesFolder.isFolder()) {
            TemplatesPanelGUI gui = (TemplatesPanelGUI)this.getComponent();
            gui.setTemplatesFolder (templatesFolder);
            if (wd.getProperty(TemplatesPanelGUI.TARGET_TEMPLATE) == null) {
                //First run
                String selectedCategory = OpenProjectListSettings.getInstance().getLastSelectedProjectCategory ();
                gui.setSelectedCategoryByName(selectedCategory);
                String selectedTemplate = OpenProjectListSettings.getInstance().getLastSelectedProjectType ();
                gui.setSelectedTemplateByName(selectedTemplate);
            }
        }


    }
    
    public void storeSettings (Object settings) {
        TemplateWizard wd = (TemplateWizard) settings;
        TemplatesPanelGUI gui = (TemplatesPanelGUI)this.getComponent();
        FileObject fo = gui.getSelectedTemplate();
        if (fo != null) {
            try {
                wd.setTemplate (DataObject.find(fo));
            } catch (DataObjectNotFoundException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        String path = gui.getSelectedCategoryName();
        if (path != null) {
            OpenProjectListSettings.getInstance().setLastSelectedProjectCategory(path);
        }
        path = gui.getSelectedTemplateName();
        if (path != null) {
            OpenProjectListSettings.getInstance().setLastSelectedProjectType (path);
        }
    }
    
    public synchronized void addChangeListener(javax.swing.event.ChangeListener l) {
        if (this.listeners == null) {
            this.listeners = new ArrayList ();
        }
        this.listeners.add (l);
    }
    
    public synchronized void removeChangeListener(javax.swing.event.ChangeListener l) {
        if (this.listeners == null) {
            return;
        }
        this.listeners.remove (l);
    }
    
    public boolean isValid() {
        return ((TemplatesPanelGUI)this.getComponent()).getSelectedTemplate() != null;
    }
    
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public synchronized Component getComponent() {
        if (this.panel == null) {
            this.panel = new TemplatesPanelGUI (this);
            this.panel.setName (NbBundle.getBundle (TemplatesPanel.class).getString ("LBL_TemplatesPanel_Name")); // NOI18N
        }
        return this.panel;
    }
    
    public void fireChange() {
        Iterator  it = null;
        synchronized (this) {
            if (this.listeners == null) {
                return;
            }
            it = ((ArrayList)this.listeners.clone()).iterator();
        }
        ChangeEvent event = new ChangeEvent (this);
        while (it.hasNext ()) {
            ((ChangeListener)it.next()).stateChanged(event);
        }
    }
    
    public org.openide.nodes.Children createCategoriesChildren(org.openide.filesystems.FileObject fo) {
        assert fo != null && fo.isFolder() : "Root must be a folder";  //NOI18N
        DataFolder folder = DataFolder.findFolder(fo);
        return new CategoriesChildren (folder);
    }
    
    public org.openide.nodes.Children createTemplatesChildren(org.openide.filesystems.FileObject fo) {
        return new TemplateChildren (fo);
    }
    
    public char getCategoriesMnemonic() {
        return NbBundle.getMessage(TemplatesPanel.class,"MNE_Categories").charAt(0);
    }
    
    public String getCategoriesName() {
        return NbBundle.getMessage(TemplatesPanel.class,"CTL_Categories");
    }
    
    public char getTemplatesMnemonic() {
        return NbBundle.getMessage(TemplatesPanel.class,"MNE_Projects").charAt (0);
    }
    
    public String getTemplatesName() {
        return NbBundle.getMessage(TemplatesPanel.class,"CTL_Projects");
    }
    
    private static class CategoriesChildren extends Children.Keys {
        
        private DataFolder root;
                
        public CategoriesChildren (DataFolder folder) {
            this.root = folder;
        }
        
        protected void addNotify () {
            this.setKeys (this.root.getChildren());
        }
        
        protected void removeNotify () {
            this.setKeys (new Object[0]);
        }
        
        protected Node[] createNodes(Object key) {
            if (key instanceof DataObject) {
                DataObject dobj = (DataObject) key;
                if (dobj instanceof DataFolder) {
                    DataFolder folder = (DataFolder) dobj;
                    FileObject[] children = folder.getPrimaryFile().getChildren();
                    int type = children.length == 0 ? 0 : 1;   //Empty folder or File folder
                    for (int i=0; i< children.length; i++) {
                        if (children[i].isFolder()) {
                            type = 2;   //Folder folder
                            break;
                        }
                    }
                    if (type == 1) {
                        return new Node[] {
                            new FilterNode (dobj.getNodeDelegate(), Children.LEAF)
                        };
                    }
                    else if (type == 2) {
                        return new Node[] {                        
                            new FilterNode (dobj.getNodeDelegate(), new CategoriesChildren ((DataFolder)dobj))
                        };
                    }
                }
            }
            return new Node[0];
        }                
    }
    
    private static class TemplateChildren extends Children.Keys {
        
        private FileObject root;
                
        public TemplateChildren (FileObject folder) {
            this.root = folder;
            assert this.root != null : "Root can not be null";  //NOI18N
        }
        
        protected void addNotify () {
            this.setKeys (this.root.getChildren());
        }
        
        protected void removeNotify () {
            this.setKeys (new Object[0]);
        }
        
        protected Node[] createNodes(Object key) {
            if (key instanceof FileObject) {
                FileObject fo = (FileObject) key;
                if (fo.isData()) {
                    try {
                        DataObject dobj = DataObject.find (fo);
                        return new Node[] {
                            new FilterNode (dobj.getNodeDelegate(),Children.LEAF)
                        };
                    } catch (DataObjectNotFoundException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
            }
            return new Node[0];
        }        
        
    }
    
}
