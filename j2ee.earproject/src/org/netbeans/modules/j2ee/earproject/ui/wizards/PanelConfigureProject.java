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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject.ui.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import java.util.ResourceBundle;

/**
 * Panel just asking for basic info.
 * @author Jesse Glick
 */
final public class PanelConfigureProject implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {
    
    private WizardDescriptor wizardDescriptor;
    private PanelConfigureProjectVisual component;
    private String namePropIndex;
    private ResourceBundle customBundle;
    boolean importStyle = false;
    private HelpCtx helpId;
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    
    /** Create the wizard panel descriptor. */
    public PanelConfigureProject(String namePropIndex, ResourceBundle customBundle, 
        HelpCtx helpId, boolean importStyle) {
        this(namePropIndex, customBundle, helpId);
        this.importStyle = importStyle;
    }
    
     public PanelConfigureProject(String namePropIndex, ResourceBundle customBundle,
        HelpCtx helpId) {
        this.namePropIndex = namePropIndex;
        this.customBundle = customBundle;
        this.helpId = helpId;
    }
     
   public boolean isFinishPanel() {
        return true;
    }

    public Component getComponent() {
        if (component == null) {
            component = new PanelConfigureProjectVisual(this, namePropIndex, customBundle, importStyle);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return helpId;
    }
    
    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }
    
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read (wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) component).getClientProperty("NewProjectWizard_Title"); // NOI18N
        if (substitute != null)
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); // NOI18N
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
        ((WizardDescriptor) d).putProperty("NewProjectWizard_Title", null); // NOI18N
    }
    
    public String getProjectTypeFlag() {
        return "NWP1";
    }
    
}
