/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.etl.ui.view.wizards;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Accepts input of unique name for new ETL Collaboration instance.
 * 
 * @author Sanjeeth Duvuru
 * @version $Revision$
 */
public class ETLCollaborationWizardNamePanel extends JPanel implements WizardDescriptor.Panel {

    class NameFieldKeyAdapter extends KeyAdapter {
        /**
         * Overrides default implementation to notify listeners of new collab name value
         * in associated textfield.
         * 
         * @param e KeyEvent to be handled
         */
        public void keyReleased(KeyEvent e) {
            String collaborationName = ETLCollaborationWizardNamePanel.this.textField.getText();

            if (collaborationName != null && collaborationName.trim().length() != 0) {
                ETLCollaborationWizardNamePanel.this.collabName = collaborationName.trim();
            } else {
                ETLCollaborationWizardNamePanel.this.collabName = null;
            }

            ETLCollaborationWizardNamePanel.this.fireChangeEvent();
        }
    }
    protected String collabName;

    /* Set <ChangeListeners> */
    protected final Set listeners = new HashSet(1);
    protected ETLCollaborationWizard owner;
    protected JTextField textField;

    protected String title;

    /**
     * No-arg constructor for this wizard descriptor.
     */
    public ETLCollaborationWizardNamePanel() {
        setLayout(new BorderLayout());

        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new GridBagLayout());

        // Top filler panel to absorb 20% of any expansion up and down the page.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.2;
        outerPanel.add(new JPanel(), gbc);

        // Text field label.
        JLabel header = new JLabel(NbBundle.getMessage(ETLCollaborationWizardNamePanel.class, "LBL_tblwizard_namefield"));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets.right = 10;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        outerPanel.add(header, gbc);

        // Text field.
        textField = new JTextField();
        textField.addKeyListener(new NameFieldKeyAdapter());

        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        outerPanel.add(textField, gbc);

        // Bottom filler panel to absorb 80% of any expansion up and down the page.
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.PAGE_START;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.8;
        outerPanel.add(new JPanel(), gbc);

        add(outerPanel, BorderLayout.CENTER);
    }

    /**
     * Create the wizard panel descriptor, using the given panel title, content panel
     * 
     * @param myOwner ETLWizard that owns this panel
     * @param panelTitle text to display as panel title
     */
    public ETLCollaborationWizardNamePanel(ETLCollaborationWizard myOwner, String panelTitle) {
        this();

        title = panelTitle;
        this.setName(title);
        owner = myOwner;
    }

    /**
     * @see ETLWizardPanel#addChangeListener
     */
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    /**
     * @see ETLWizardPanel#fireChangeEvent
     */
    public void fireChangeEvent() {
        Iterator it;

        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }

        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    /**
     * @see ETLWizardPanel#getComponent
     */
    public Component getComponent() {
        return this;
    }

    /**
     * Gets current value of collaboration name as entered by user.
     * 
     * @return current user-specified name
     */
    public String getCollabName() {
        return collabName;
    }
    
    /**
     * @see ETLWizardPanel#getHelp
     */
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(ETLCollaborationWizardNamePanel.class);
    }

    /**
     * Indicates whether current contents of collaboration name textfield correspond to
     * the name of an existing collaboration in the current project.
     * 
     * @return true if textfield contains the name of an existing collab; false otherwise
     */
    public boolean isDuplicateCollabName() {
        String collaborationName = textField.getText();
        collaborationName = (collaborationName != null) ? collaborationName.trim() : null;
        
        boolean duplicated = false;

        //TODO - verify implementation. Where do collaboration files live?
        FileObject fo = Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject(collaborationName);

        if (fo != null) { // file exists
            duplicated = true;
            NotifyDescriptor.Message d1 = new NotifyDescriptor.Message(NbBundle.getMessage(ETLCollaborationWizardNamePanel.class,
                "ERROR_tblwizard_duplicatename", collaborationName), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d1);
            
            textField.requestFocus();
        }

        return duplicated;
    }

    /**
     * @see ETLWizardPanel#isValid
     */
    public boolean isValid() {
        boolean returnVal = false;
        if (collabName != null) {
            returnVal = true;
        }
        return returnVal;
    }

    /**
     * @see ETLWizardPanel#readSettings
     */
    public void readSettings(Object settings) {
        WizardDescriptor wd = null;
        if(settings instanceof ETLWizardContext) {
            ETLWizardContext wizardContext = (ETLWizardContext) settings;
            wd = (WizardDescriptor) wizardContext.getProperty(ETLWizardContext.WIZARD_DESCRIPTOR);
        } else if(settings instanceof WizardDescriptor) {
            wd = (WizardDescriptor) settings;
        }
        
        if(wd != null) {
            String myCollabName = (String) wd.getProperty(ETLCollaborationWizard.COLLABORATION_NAME);
            textField.setText(myCollabName);
        }
    }

    /**
     * @see ETLWizardPanel#removeChangeListener
     */
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    /**
     * @see ETLWizardPanel#storeSettings
     */
    public void storeSettings(Object settings) {
        WizardDescriptor wd = null;
        if(settings instanceof  ETLWizardContext) {
            ETLWizardContext wizardContext = (ETLWizardContext) settings;
            wd = (WizardDescriptor) wizardContext.getProperty(ETLWizardContext.WIZARD_DESCRIPTOR);
        } else if(settings instanceof WizardDescriptor) {
            wd = (WizardDescriptor) settings;
            this.owner.setDescriptor(wd);
        }
        
        if(wd != null) {
            final Object selectedOption = wd.getValue();
            if (NotifyDescriptor.CANCEL_OPTION == selectedOption || NotifyDescriptor.CLOSED_OPTION == selectedOption) {
                return;
            }
            wd.putProperty(ETLCollaborationWizard.COLLABORATION_NAME, collabName);
        }
    }
}

