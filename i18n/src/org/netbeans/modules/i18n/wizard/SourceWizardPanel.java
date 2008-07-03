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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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


package org.netbeans.modules.i18n.wizard;


import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.util.Iterator;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;

import org.netbeans.modules.i18n.FactoryRegistry;
import org.netbeans.modules.i18n.I18nUtil;

import org.openide.awt.Mnemonics;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.nodes.NodeOperation;
import org.openide.util.HelpCtx;
import org.openide.util.UserCancelException;
import org.netbeans.modules.i18n.SelectorUtils;
import org.openide.WizardDescriptor;

/**
 * First panel used in I18N (test) Wizard.
 *
 * @author  Peter Zavadsky
 * @see Panel
 */
final class SourceWizardPanel extends JPanel {

    /** Sources selected by user. */
    private final Map<DataObject,SourceData> sourceMap
            = Util.createWizardSourceMap();
    
    /** This component panel wizard descriptor.
     * @see org.openide.WizardDescriptor.Panel 
     * @see Panel */
    private final Panel descPanel;

    /**
     * Panel role true (test wizard) false (i18n) wizard
     */
    private boolean testRole = false;
    
    /** Creates new form SourceChooserPanel.
     * @param it's panel wizard descriptor */
    private SourceWizardPanel(Panel descPanel, boolean testRole) {
        this.descPanel = descPanel;
        this.testRole = testRole;
        
        initComponents();        

        initAccessibility ();
        
        setPreferredSize(I18nWizardDescriptor.PREFERRED_DIMENSION);
        
        initList();
        
        putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(0)); // NOI18N
        
        if (testRole) {
            setName(Util.getString("TXT_SelecTestSources"));
        } else {
            setName(Util.getString("TXT_SelectSources"));                
        }        
    }
    

    /** Getter for <code>sources</code> property. */
    public Map<DataObject,SourceData> getSourceMap() {
        return sourceMap;
    }
    
    /** Setter for <code>sources</code> property. */
    public void setSourceMap(Map<DataObject,SourceData> sourceMap) {
        this.sourceMap.clear();
        this.sourceMap.putAll(sourceMap);
        
        sourcesList.setListData(sourceMap.keySet().toArray());
        
        descPanel.fireStateChanged();
    }

    /**
     * Panel description depend of its container test or i18n role
     */
    private String getPanelDescription() {
        if (testRole == false)   {
            return Util.getString("MSG_SourcesPanel_desc");
        } else {
            return Util.getString("MSG_SourcesPanel_test_desc");
        }        
    }

    /**
     * Accessible panel description depends of its container test or i18n role
     */
    private String getAccessibleListDescription() {
        if (testRole == false)   {
            return Util.getString("ACSD_sourcesList");
        } else {
            return Util.getString("ACSD_sourcesList_test");
        }        
    }
    
    
    /** 
     * List content drives remove button enableness.
     */
    private void initList() {
        sourcesList.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent evt) {
                    removeButton.setEnabled(!sourcesList.isSelectionEmpty());
                }
            }
        );
        
        removeButton.setEnabled(!sourcesList.isSelectionEmpty());
    }
    
    private void initAccessibility() {        
        getAccessibleContext().setAccessibleDescription(getPanelDescription());
        
        addButton.setToolTipText(Util.getString("CTL_AddSource_desc"));
        
        removeButton.setToolTipText(Util.getString("CTL_RemoveSource_desc"));
        
        sourcesList.getAccessibleContext().setAccessibleName(Util.getString("ACSN_sourcesList"));
        sourcesList.getAccessibleContext().setAccessibleDescription(getAccessibleListDescription());
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     */
    // <editor-fold defaultstate="collapsed" desc="UI initialization code">
    private void initComponents() {

        sourcesList = new JList();
        sourcesList.setCellRenderer(new DataObjectListCellRenderer());

        addButton = new JButton();
        Mnemonics.setLocalizedText(addButton,
                                   Util.getString("CTL_AddSource"));    //NOI18N
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        removeButton = new JButton();
        Mnemonics.setLocalizedText(removeButton,
                                   Util.getString("CTL_RemoveSource")); //NOI18N
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        Util.layoutSelectResourcePanel(
                this,
                getPanelDescription(),
                Util.getString("LBL_SelectedSourcesToI18N"),            //NOI18N
                sourcesList,
                addButton,
                removeButton);
    }// </editor-fold>

    private void removeButtonActionPerformed(ActionEvent evt) {
        Object[] selected = sourcesList.getSelectedValues();
        
        for(int i=0; i<selected.length; i++) {
            sourceMap.remove(selected[i]);
        }

        sourcesList.setListData(sourceMap.keySet().toArray());
        
        descPanel.fireStateChanged();
    }

    private void addButtonActionPerformed(ActionEvent evt) {

        // take actual project from first data object

        Project prj = descPanel.getProject();
  
        // Selects source data objects which could be i18n-ized.
        try {
            Node[] selectedNodes= NodeOperation.getDefault().select(
                Util.getString("LBL_SelectSources"),
                Util.getString("LBL_Filesystems"),
                SelectorUtils.sourcesNode(prj, SelectorUtils.ALL_FILTER),
                new NodeAcceptor() {
                    public boolean acceptNodes(Node[] nodes) {
                        if (nodes == null || nodes.length == 0) {
                            return false;
                        }

                        for (Node node : nodes) {
                            // Has to be data object.
                            Object dataObject = node.getCookie(DataObject.class);
                            if (dataObject == null) {
                                return false;
                            }
                            // if it is folder and constains some our data object.
                            if (dataObject instanceof DataFolder) {
                                if (I18nUtil.containsAcceptedDataObject((DataFolder) dataObject)) {
                                    return true;
                                }
                            } else if (FactoryRegistry.hasFactory(dataObject.getClass())) {
                                // Has to have registered i18n factory for that data object class name.
                                return true;
                            }
                        }
                        
                        return false;
                    }                    
                }
            );
            
            for(int i=0; i<selectedNodes.length; i++) {
                DataObject dataObject = selectedNodes[i].getCookie(DataObject.class);

                if (dataObject instanceof DataFolder) {
                    // recursively add folder content
                    Iterator<DataObject> it = I18nUtil.getAcceptedDataObjects((DataFolder) dataObject).iterator();
                    while (it.hasNext()) {
                        Util.addSource(sourceMap, it.next());
                    }
                } else {
                    Util.addSource(sourceMap, dataObject);
                }
            }
            
            sourcesList.setListData(sourceMap.keySet().toArray());
           
            descPanel.fireStateChanged();
        } catch (UserCancelException uce) {
           // ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, uce);
             // nobody is interested in the message
        }
    }

    private JButton addButton;
    private JLabel lblSelectedSources;
    private JButton removeButton;
    private JList sourcesList;


    /** List cell rendrerer which uses data object as values. */
    public static class DataObjectListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
        JList list,
        Object value,            // value to display
        int index,               // cell index
        boolean isSelected,      // is the cell selected
        boolean cellHasFocus)    // the list and the cell have the focus
        {
            JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            DataObject dataObject = (DataObject)value;

            if (dataObject != null) {
                ClassPath cp = ClassPath.getClassPath(dataObject.getPrimaryFile(), ClassPath.SOURCE );
                                
                label.setText(cp.getResourceName(dataObject.getPrimaryFile(), '.', false )); // NOI18N
                label.setIcon(new ImageIcon(dataObject.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16)));
            } else {
                label.setText(""); // NOI18N
                label.setIcon(null);
            }

            return label;
        }
    }

    
    
    /** <code>WizardDescriptor.Panel</code> used for <code>SourceChooserPanel</code>.
     * @see I18nWizardDescriptorPanel
     * @see org.openide.WizardDescriptor.Panel */
    public static class Panel extends I18nWizardDescriptor.Panel {

        /** Test wizard flag. */
        private final boolean testWizard;
        
        
        /** Constructor for i18n wizard. */
        public Panel() {
            this(false);
        }
        
        /** Constructor for specified i18n wizard. */
        public Panel(boolean testWizard) {
            this.testWizard = testWizard;
        }
        
        
        /** Gets component to display. Implements superclass abstract method. 
         * @return this instance */
        protected Component createComponent() {                                    
            Component component = new SourceWizardPanel(this, testWizard);            
            
            return component;
        }

        /** Gets if panel is valid. Overrides superclass method. */
        @Override
        public boolean isValid() {
            return !((SourceWizardPanel) getComponent()).getSourceMap().isEmpty();
        }
        
        /** Reads settings at the start when the panel comes to play. Overrides superclass method. */
        @Override
        public void readSettings(I18nWizardDescriptor.Settings settings) {
	  super.readSettings(settings);
	  ((SourceWizardPanel) getComponent()).setSourceMap(getMap());
        }

        /** Stores settings at the end of panel show. Overrides superclass method. */
        @Override
        public void storeSettings(I18nWizardDescriptor.Settings settings) {
	    super.storeSettings(settings);
	    super.storeSettings(settings);
            // Update sources.
            getMap().clear();
            getMap().putAll(((SourceWizardPanel) getComponent()).getSourceMap());
        }
        
        /** Gets help. Implements superclass abstract method. */
        public HelpCtx getHelp() {
            return new HelpCtx(testWizard
                               ? I18nUtil.HELP_ID_TESTING
                               : I18nUtil.HELP_ID_WIZARD);
        }

    } // End of nested Panel class.
    
}
