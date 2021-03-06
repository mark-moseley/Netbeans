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


package org.netbeans.modules.i18n.wizard;


import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.netbeans.api.java.classpath.ClassPath;

import org.netbeans.modules.i18n.HardCodedString;
import org.netbeans.modules.i18n.I18nString;
import org.netbeans.modules.i18n.I18nSupport;
import org.netbeans.modules.i18n.I18nUtil;

import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 * <code>WizardDescriptor.Panel</code> used provide additional values modifying if avialble.
 * E.g. typically offers creation resource field or method in source used by replacing code.
 * It is the third panel of I18N Wizard.
 *
 * @author  Peter Zavadsky
 * @see Panel
 */
final class AdditionalWizardPanel extends JPanel {

    /** Local copy of i18n wizard data. */
    private final Map<DataObject,SourceData> sourceMap = Util.createWizardSourceMap();
    
    /** Sources on which additional modifications coudl happen in this panel. */
    private final Set<DataObject> viewedSources = new HashSet<DataObject>(0);

    /** Additional component. */
    private JComponent additionalComponent = EMPTY_COMPONENT;
    
    /** Empty component to show when no additional values are supported. */
    private static final JLabel EMPTY_COMPONENT = 
                         new JLabel(Util.getString("TXT_HasNoAdditonal"));
    
    
    /** Creates new form HardCodedStringsPanel */
    private AdditionalWizardPanel() {
        initComponents();
        initA11Y();
        
        // set customized model
        setComboModel(sourceMap);
    }

    /** Does additional init of components. */
    private void initA11Y() {
        sourceLabel.setLabelFor(sourceCombo);
        sourceCombo.getAccessibleContext().setAccessibleDescription(Util.getString("ACS_sourceCombo"));
    }
    
    
    /** Sets combo model only for source which support provides additional customizing. */
    private void setComboModel(Map<DataObject,SourceData> sourceMap) {
        DataObject[] sources = sourceMap.keySet().toArray(new DataObject[0]);
        
        List<DataObject> nonEmptySources = new ArrayList<DataObject>();

        for (DataObject source : sources) {
            if ((sourceMap.get(source)).getSupport().hasAdditionalCustomizer()) {
                nonEmptySources.add(source);
            }
        }
        
        sourceCombo.setModel(new DefaultComboBoxModel(nonEmptySources.toArray()));
        
        // update view
        Object selected = sourceCombo.getSelectedItem();
        updateAdditionalComponent((DataObject) selected);
        
    }

    /** Getter for <code>viewedSources</code> property. */
    Set<DataObject> getViewedSources() {
        return viewedSources;
    }

    /** Getter for <code>sourceMap</code> property. */
    Map<DataObject,SourceData> getSourceMap() {
        return sourceMap;
    }
    
    /** Setter for <code>resources</code> property. */
    void setSourceMap(Map<DataObject,SourceData> sourceMap) {
        this.sourceMap.clear();
        this.sourceMap.putAll(sourceMap);
        
        setComboModel(sourceMap);
    }
        
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sourceLabel = new javax.swing.JLabel();
        sourceCombo = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(sourceLabel, NbBundle.getBundle(AdditionalWizardPanel.class).getString("LBL_Source")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(sourceLabel, gridBagConstraints);

        sourceCombo.setRenderer(new SourceWizardPanel.DataObjectListCellRenderer());
        sourceCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 0);
        add(sourceCombo, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void sourceComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceComboActionPerformed
        Object selected = sourceCombo.getSelectedItem();
        updateAdditionalComponent((DataObject) selected);
    }//GEN-LAST:event_sourceComboActionPerformed

    private void updateAdditionalComponent(DataObject selected) {
        
        I18nSupport support = null;
        
        if (selected != null) {
            support = (sourceMap.get(selected)).getSupport();
        }

        // remove last one
        remove(additionalComponent);
        
        if(support != null && support.hasAdditionalCustomizer()) {
            additionalComponent = support.getAdditionalCustomizer();
            viewedSources.add(selected);
        } else {
            additionalComponent = EMPTY_COMPONENT;
        }

        // add it
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(additionalComponent, gridBagConstraints);
        
        revalidate();        
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox sourceCombo;
    private javax.swing.JLabel sourceLabel;
    // End of variables declaration//GEN-END:variables

    /** <code>WizardDescriptor.Panel</code> used for <code>AdditionalPanel</code>. 
     * @see org.openide.WizardDescriptor.Panel*/
    public static class Panel extends I18nWizardDescriptor.Panel {

        /** Empty label component. */
        private final JLabel emptyLabel;
        
        /** Component. */
        private transient AdditionalWizardPanel additionalPanel;

        Panel () {
            emptyLabel = new JLabel(Util.getString("TXT_HasNoAdditonal"));
            emptyLabel.setHorizontalAlignment(JLabel.CENTER);
            emptyLabel.setVerticalAlignment(JLabel.CENTER);
        }        
        
        
        /** Gets component to display. Implements superclass abstract method. 
         * @return <code>AdditionalPanel</code> instance */
        protected Component createComponent() {
            JPanel panel = new JPanel();
            
            //Accessibility
            panel.getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(AdditionalWizardPanel.class).getString("ACS_AdditionalWizardPanel"));                    
            
            panel.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(2)); // NOI18N
            panel.setName(NbBundle.getBundle(getClass()).getString("TXT_ModifyAdditional"));
            panel.setPreferredSize(I18nWizardDescriptor.PREFERRED_DIMENSION);

            panel.setLayout(new GridBagLayout());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            constraints.fill = GridBagConstraints.BOTH;
            panel.add(getUI(), constraints);
            
            return panel;
        }

        /** Reads settings at the start when the panel comes to play. Overrides superclass method. */
        @Override
        public void readSettings(I18nWizardDescriptor.Settings settings) {
   	    super.readSettings(settings);
            getUI().setSourceMap(getMap());
            
            JPanel panel = (JPanel)getComponent();
            if(hasAdditional(getMap())) {
                if(panel.isAncestorOf(emptyLabel)) {
                    panel.remove(emptyLabel);
                    GridBagConstraints constraints = new GridBagConstraints();
                    constraints.weightx = 1.0;
                    constraints.weighty = 1.0;
                    constraints.fill = GridBagConstraints.BOTH;
                    panel.add(getUI(), constraints);
                }
            } else {
                if(panel.isAncestorOf(getUI())) {
                    panel.remove(getUI());
                    GridBagConstraints constraints = new GridBagConstraints();
                    constraints.weightx = 1.0;
                    constraints.weighty = 1.0;
                    constraints.fill = GridBagConstraints.BOTH;
                    panel.add(emptyLabel, constraints);
                }
            }
        }

        /** Stores settings at the end of panel show. Overrides superclass abstract method. */
        @Override
        public void storeSettings(I18nWizardDescriptor.Settings settings) {
   	    super.storeSettings(settings);
            // Alter i18n string values if changing additional values could affect them.
            Map<DataObject,SourceData> sourceMap = getUI().getSourceMap();
            for (DataObject viewedSource : getUI().getViewedSources()) {
                SourceData sourceData = sourceMap.get(viewedSource);
                for (Map.Entry<HardCodedString,I18nString> entry : sourceData.getStringMap().entrySet()) {
                    entry.setValue(sourceData.getSupport().getDefaultI18nString(entry.getKey()));
                }
            }
            
            // Update sources.
            getMap().clear();
            getMap().putAll(sourceMap);
        }
        
        /** Searches hard coded strings in sources and puts found hard coded string - i18n string pairs
         * into settings. Implements <code>ProgressMonitor</code> interface method. */
        public void doLongTimeChanges() {
            // Replace panel.
            ProgressWizardPanel progressPanel = new ProgressWizardPanel(true);
            
            showProgressPanel(progressPanel);
            
            progressPanel.setMainText(NbBundle.getMessage(getClass(), "LBL_AdditionalIn"));
            progressPanel.setMainProgress(0);
            
            // Alter i18n string values if changing additional values could affect them.
            Map<DataObject,SourceData> sourceMap
                    = ((AdditionalWizardPanel) getComponent()).getSourceMap();
            Iterator<DataObject> it = ((AdditionalWizardPanel) getComponent()).getViewedSources().iterator();
            
            for (int i = 0; it.hasNext(); i++) {
                DataObject source = it.next();
                
                SourceData sourceData = sourceMap.get(source);
                
                ClassPath cp = ClassPath.getClassPath(source.getPrimaryFile(),
                                                      ClassPath.SOURCE );

                progressPanel.setMainText(
                        NbBundle.getMessage(
                                getClass(), "LBL_AdditionalIn")        // NOI18N
                                + " "
                                + cp.getResourceName(source.getPrimaryFile(),
                                                     '.',
                                                     false));
                
                HardCodedString[] hcStrings
                        = sourceData.getStringMap().keySet().toArray(new HardCodedString[0]);
                
                for(int j=0; i<hcStrings.length; j++) {     //PENDING - bug? 'i' vs. 'j'
                    // Actual replacing of default values.
                    sourceData.getStringMap().put(
                            hcStrings[j],
                            sourceData.getSupport().getDefaultI18nString(hcStrings[j]));
                } // End of inner for.
                
                progressPanel.setMainProgress((int)((i+1)/(float)sourceMap.size() * 100));                
            } // End of outer for.
        }

        /** Helper method. Places progress panel for monitoring search. */
        private void showProgressPanel(ProgressWizardPanel progressPanel) {
            ((Container) getComponent()).remove(getUI());
            GridBagConstraints constraints = new GridBagConstraints();
            constraints.weightx = 1.0;
            constraints.weighty = 1.0;
            constraints.fill = GridBagConstraints.BOTH;
            ((Container) getComponent()).add(progressPanel, constraints);
            ((JComponent) getComponent()).revalidate();
            getComponent().repaint();
        }
        
        /** Resets panel back after monitoring search. Implements <code>ProgressMonitor</code> interface method. */
        public void reset() {
            Container container = (Container) getComponent();
            
            if(!container.isAncestorOf(getUI())) {
                container.removeAll();
                GridBagConstraints constraints = new GridBagConstraints();
                constraints.weightx = 1.0;
                constraints.weighty = 1.0;
                constraints.fill = GridBagConstraints.BOTH;
                container.add(getUI(), constraints);
            }
        }
        
        /** Gets help. Implements superclass abstract method. */
        public HelpCtx getHelp() {
            return new HelpCtx(I18nUtil.HELP_ID_WIZARD);
        }
        
        /** Indicates if there are additional customizers in any of selected sources. 
         * @return true if at least one source suport has additional customizer. */
        private static boolean hasAdditional(Map<DataObject,SourceData> sourceMap) {
            for (Map.Entry<DataObject,SourceData> entry : sourceMap.entrySet()) {
                if (entry.getValue().getSupport().hasAdditionalCustomizer()) {
                    return true;
                }
            }

            return false;
        }
        
        private synchronized AdditionalWizardPanel getUI() {
            if (additionalPanel == null) {
                additionalPanel = new AdditionalWizardPanel();
            }
            return additionalPanel;
        }
    } // End of nested Panel class.
    
}
