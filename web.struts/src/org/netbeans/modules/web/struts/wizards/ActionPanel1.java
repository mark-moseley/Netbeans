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

package org.netbeans.modules.web.struts.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import org.netbeans.api.project.Project;

/**
 *
 * @author radko
 */
public class ActionPanel1 implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {

    private WizardDescriptor wizardDescriptor;
    private ActionPanel1Visual component;
    private Project project;

    /** Creates a new instance of ActionPanel */
    public ActionPanel1(Project project) {
        this.project=project;
    }
    
    Project getProject() {
        return project;
    }
    
    public Component getComponent() {
        if (component == null)
            component = new ActionPanel1Visual(this);

        return component;
    }
    
    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read(wizardDescriptor);
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((javax.swing.JComponent) component).getClientProperty("NewFileWizard_Title"); // NOI18N
        if (substitute != null)
            wizardDescriptor.putProperty("NewFileWizard_Title", substitute); // NOI18N
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor desc = (WizardDescriptor) settings;
        component.store(desc);
    }

    public HelpCtx getHelp() {
        return new HelpCtx(ActionPanel1.class);
    }

    private final Set/*<ChangeListener>*/ listeners = new HashSet(1);
    
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
    
    public boolean isFinishPanel() {
        return isValid();
    }

}
