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

package org.netbeans.modules.java.j2seproject.ui.wizards;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * Panel just asking for basic info.
 * @author Jesse Glick
 */
final class PanelConfigureProject implements WizardDescriptor.Panel, WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {
    
    private WizardDescriptor wizardDescriptor;
    private NewJ2SEProjectWizardIterator.WizardType type;
    private PanelConfigureProjectVisual component;
    
    public PanelConfigureProject(NewJ2SEProjectWizardIterator.WizardType type) {
        this.type = type;
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new PanelConfigureProjectVisual(this, type);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(PanelConfigureProject.class.getName() + '_' + type);
    }
    
    public boolean isValid() {
        getComponent();
        return component.valid( wizardDescriptor );
    }
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    protected final void fireChangeEvent() {
        changeSupport.fireChange();
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;        
        component.read (wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent)component).getClientProperty ("NewProjectWizard_Title"); // NOI18N
        if (substitute != null) {
            wizardDescriptor.putProperty ("NewProjectWizard_Title", substitute); // NOI18N
        }
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor)settings;
        component.store(d);
        d.putProperty ("NewProjectWizard_Title", null); // NOI18N
    }

    public boolean isFinishPanel() {
        return true;
    }
    
    public void validate () throws WizardValidationException {
        getComponent ();
        component.validate (wizardDescriptor);
    }

}
