/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.diff.builtin;

import java.awt.Component;
//import java.awt.event.ItemEvent;
//import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.beans.PropertyEditorManager;
import java.io.Reader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openide.TopManager;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyModel;
import org.openide.explorer.propertysheet.PropertyPanel;
import org.openide.explorer.propertysheet.DefaultPropertyModel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

import org.netbeans.api.diff.Difference;
import org.netbeans.spi.diff.*;

/**
 * This panel is to be used as a wrapper for diff visualizers.
 * @author  Martin Entlicher
 */
public class DiffPresenter extends javax.swing.JPanel {
    
    public static final String PROP_PROVIDER = "provider"; // NOI18N
    public static final String PROP_VISUALIZER = "visualizer"; // NOI18N
    
    private DiffPresenter.Info diffInfo;
    private DiffProvider defaultProvider;
    private DiffVisualizer defaultVisualizer;
    private PropertyChangeSupport propSupport;
    
    /** Creates new form DiffPresenter */
    public DiffPresenter(DiffPresenter.Info diffInfo) {
        this.diffInfo = diffInfo;
        propSupport = new PropertyChangeSupport(this);
        initComponents();
        initMyComponents();
        providerLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(DiffPresenter.class, "LBL_Provider_Mnemonic").charAt(0));  // NOI18N
        visualizerLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(DiffPresenter.class, "LBL_Visualizer_Mnemonic").charAt(0));  // NOI18N
        providerLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DiffPresenter.class, "ACS_ProviderA11yDesc"));  // NOI18N
        visualizerLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DiffPresenter.class, "ACS_VisualizerA11yDesc"));  // NOI18N
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        servicesPanel = new javax.swing.JPanel();
        providerLabel = new javax.swing.JLabel();
        visualizerLabel = new javax.swing.JLabel();
        emptyLabel = new javax.swing.JLabel();
        visualizerPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        servicesPanel.setLayout(new java.awt.GridBagLayout());

        providerLabel.setText(org.openide.util.NbBundle.getMessage(DiffPresenter.class, "LBL_Provider"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        servicesPanel.add(providerLabel, gridBagConstraints);

        visualizerLabel.setText(org.openide.util.NbBundle.getMessage(DiffPresenter.class, "LBL_Visualizer"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 5);
        servicesPanel.add(visualizerLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        servicesPanel.add(emptyLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 10, 10, 10);
        add(servicesPanel, gridBagConstraints);

        visualizerPanel.setLayout(new java.awt.BorderLayout());

        visualizerPanel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(visualizerPanel, gridBagConstraints);

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel servicesPanel;
    private javax.swing.JLabel emptyLabel;
    private javax.swing.JPanel visualizerPanel;
    private javax.swing.JLabel providerLabel;
    private javax.swing.JLabel visualizerLabel;
    // End of variables declaration//GEN-END:variables
    
    private void initMyComponents() {
        PropertyDescriptor pd;
        PropertyModel model;
        PropertyPanel panel;
        java.awt.GridBagConstraints gridBagConstraints;
        FileSystem dfs = TopManager.getDefault().getRepository().getDefaultFileSystem();
        FileObject services = dfs.findResource("Services");
        DataFolder df = DataFolder.findFolder(services);
        if (diffInfo.isChooseProviders()) {
            try {
                pd = new PropertyDescriptor (PROP_PROVIDER, getClass ());
            } catch (java.beans.IntrospectionException intrex) {
                return ;
            }
            pd.setDisplayName(org.openide.util.NbBundle.getMessage(DiffPresenter.class, "LBL_ProvidersPropEditorTitle"));
            pd.setPropertyEditorClass (PropertyEditorManager.findEditor (Object.class).getClass());
            // special attributes to the property editor
            pd.setValue ("superClass", DiffProvider.class);
            FileObject providersFO = services.getFileObject("DiffProviders");
            try {
                DataObject providersDO = DataObject.find(providersFO);
                Node providersNode = providersDO.getNodeDelegate();
                pd.setValue("node", providersNode);
            } catch (DataObjectNotFoundException donfex) {}
            pd.setValue(ExPropertyEditor.PROPERTY_HELP_ID, "org.netbeans.modules.diff.DiffPresenter.providers");
            model = new DefaultPropertyModel (this, pd);
            panel = new PropertyPanel (model, PropertyPanel.PREF_INPUT_STATE);
            panel.setChangeImmediate(false);

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            //gridBagConstraints.weightx = 1.0;
            gridBagConstraints.gridx = 1;
            servicesPanel.add(panel, gridBagConstraints);
            providerLabel.setLabelFor(panel);
            panel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DiffPresenter.class, "ACS_ProviderPropertyPanelA11yName"));  // NOI18N
            panel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DiffPresenter.class, "ACS_ProviderPropertyPanelA11yDesc"));  // NOI18N
        }
        if (diffInfo.isChooseVisualizers()) {
            try {
                pd = new PropertyDescriptor (PROP_VISUALIZER, getClass ());
            } catch (java.beans.IntrospectionException intrex) {
                return ;
            }
            pd.setDisplayName(org.openide.util.NbBundle.getMessage(DiffPresenter.class, "LBL_VisualizersPropEditorTitle"));
            pd.setPropertyEditorClass (PropertyEditorManager.findEditor (Object.class).getClass());
            // special attributes to the property editor
            pd.setValue ("superClass", DiffVisualizer.class);
            FileObject visualizersFO = services.getFileObject("DiffVisualizers");
            try {
                DataObject visualizersDO = DataObject.find(visualizersFO);
                Node visualizersNode = visualizersDO.getNodeDelegate();
                pd.setValue("node", visualizersNode);
            } catch (DataObjectNotFoundException donfex) {}
            pd.setValue(ExPropertyEditor.PROPERTY_HELP_ID, "org.netbeans.modules.diff.DiffPresenter.visualizers");
            model = new DefaultPropertyModel (this, pd);
            panel = new PropertyPanel (model, PropertyPanel.PREF_INPUT_STATE);
            panel.setChangeImmediate(false);
            panel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DiffPresenter.class, "ACS_VisualizerPropertyPanelA11yName"));  // NOI18N
            panel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DiffPresenter.class, "ACS_VisualizerPropertyPanelA11yDesc"));  // NOI18N

            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            //gridBagConstraints.weightx = 1.0;
            gridBagConstraints.gridx = 3;
            servicesPanel.add(panel, gridBagConstraints);
            visualizerLabel.setLabelFor(panel);
        }
        providerLabel.setVisible(diffInfo.isChooseProviders());
        visualizerLabel.setVisible(diffInfo.isChooseVisualizers());
        servicesPanel.setVisible(diffInfo.isChooseProviders() || diffInfo.isChooseVisualizers());
    }
    
    public DiffProvider getProvider() {
        return defaultProvider;
    }
    
    public void setProvider(DiffProvider p) {
        this.defaultProvider = (DiffProvider) p;
        //propSupport.firePropertyChange(PROP_PROVIDER, null, p);
        showDiff();
    }
    
    public DiffVisualizer getVisualizer() {
        return defaultVisualizer;
    }
    
    public void setVisualizer(DiffVisualizer v) {
        this.defaultVisualizer = (DiffVisualizer) v;
        //propSupport.firePropertyChange(PROP_VISUALIZER, null, v);
        showDiff();
    }

    private void showDiff() {
        try {
            showDiff(defaultProvider, defaultVisualizer);
        } catch (IOException ioex) {
            org.openide.TopManager.getDefault().notifyException(ioex);
        }
    }
    
    /*
    public void addProvidersChangeListener(PropertyChangeListener l) {
        propSupport.addPropertyChangeListener(PROP_PROVIDER, l);
    }
    
    public void removeProvidersChangeListener(PropertyChangeListener l) {
        propSupport.removePropertyChangeListener(PROP_PROVIDER, l);
    }
    
    public void addVisualizersChangeListener(PropertyChangeListener l) {
        propSupport.addPropertyChangeListener(PROP_VISUALIZER, l);
    }
    
    public void removeVisualizersChangeListener(PropertyChangeListener l) {
        propSupport.removePropertyChangeListener(PROP_VISUALIZER, l);
    }
     */
    
    private synchronized void showDiff(DiffProvider p, DiffVisualizer v) throws IOException {
        if (v == null) return ;
        Difference[] diffs;
        if (p != null) {
            diffs = diffInfo.getInitialDifferences();
            if (diffs == null) {
                diffs = p.computeDiff(diffInfo.createFirstReader(),
                                      diffInfo.createSecondReader());
            }
        } else {
            diffs = diffInfo.getDifferences();
        }
        if (diffs == null) return ;
        Component c = v.createView(diffs, diffInfo.getName1(), diffInfo.getTitle1(),
            diffInfo.createFirstReader(), diffInfo.getName2(), diffInfo.getTitle2(),
            diffInfo.createSecondReader(), diffInfo.getMimeType());
        setVisualizer(c);
        TopComponent tp = diffInfo.getPresentingComponent();
        if (tp != null) {
            tp.setName(c.getName());
            if (c instanceof TopComponent) {
                TopComponent vtp = (TopComponent) c;
                tp.setToolTipText(vtp.getToolTipText());
                tp.setIcon(vtp.getIcon());
            }
        }
        c.requestFocus();
    }

    private void setVisualizer(Component visualizer) {
        visualizerPanel.removeAll();
        if (visualizer != null) {
            visualizerPanel.add(visualizer, java.awt.BorderLayout.CENTER);
            // If the component needs to be initialized after it's visually
            // presented, it must implement the ShowingNotifier.
            if (visualizer instanceof ShowingNotifier) {
                ((ShowingNotifier) visualizer).componentShowingNotify();
            }
        }
        revalidate();
        repaint();
    }
    
    /**
     * This class contains informations about the differences.
     */
    public static abstract class Info extends Object {
        
        private String name1;
        private String name2;
        private String title1;
        private String title2;
        private String mimeType;
        private boolean chooseProviders;
        private boolean chooseVisualizers;
        private TopComponent tp;
        
        public Info(String name1, String name2, String title1, String title2,
                    String mimeType, boolean chooseProviders, boolean chooseVisualizers) {
            this.name1 = name1;
            this.name2 = name2;
            this.title1 = title1;
            this.title2 = title2;
            this.mimeType = mimeType;
            this.chooseProviders = chooseProviders;
            this.chooseVisualizers = chooseVisualizers;
        }
        
        public String getName1() {
            return name1;
        }
        
        public String getName2() {
            return name2;
        }
        
        public String getTitle1() {
            return title1;
        }
        
        public String getTitle2() {
            return title2;
        }
        
        public String getMimeType() {
            return mimeType;
        }
        
        public boolean isChooseProviders() {
            return chooseProviders;
        }
        
        public boolean isChooseVisualizers() {
            return chooseVisualizers;
        }
        
        public Difference[] getDifferences() {
            return null;
        }

        public Difference[] getInitialDifferences() {
            return null;
        }

        public abstract Reader createFirstReader();
        
        public abstract Reader createSecondReader();
        
        public void setPresentingComponent(TopComponent tp) {
            this.tp = tp;
        }
        
        public TopComponent getPresentingComponent() {
            return tp;
        }
        
    }
    
}
