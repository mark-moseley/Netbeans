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

package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Component;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.netbeans.modules.websvc.rest.support.JavaSourceHelper;
import org.netbeans.modules.websvc.rest.support.PersistenceHelper;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Pavel Buzek
 */
public final class EntitySelectionPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel, ChangeListener {
    
    private WizardDescriptor wizardDescriptor;
    private String panelName;
    private HelpCtx helpCtx;
    private EntitySelectionPanelVisual component;
    
    
    /** Create the wizard panel descriptor. */
    public EntitySelectionPanel(String panelName, HelpCtx helpCtx, WizardDescriptor wizardDescriptor) {
        this.panelName = panelName;
        this.helpCtx = helpCtx;
        this.wizardDescriptor = wizardDescriptor;
    }
    
    public boolean isFinishPanel() {
        return false;
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new EntitySelectionPanelVisual(panelName, wizardDescriptor);
            component.addChangeListener(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return helpCtx;
    }
    
    public boolean isValid() {
        Project project = Templates.getProject(wizardDescriptor);
        RestSupport support = project.getLookup().lookup(RestSupport.class);
        if(support == null) {
            setErrorMessage("MSG_EntitySelectionPanel_NotWebProject");
            return false;
        } else {
            if(!support.hasSwdpLibrary()) {
                setErrorMessage("MSG_EntitySelectionPanel_NoSWDP");
                return false;
            }
            List availableEntities = JavaSourceHelper.getEntityClasses(project);
            if (availableEntities == null || availableEntities.size() == 0) {
                setErrorMessage("MSG_EntitySelectionPanel_NoEntities");
                return false;
            }
            if (getPersistenceUnitName(project) == null) {
                setErrorMessage("MSG_EntitySelectionPanel_NoPersistenceUnit");
                return false;
            }
        }
        return true;
    }
    
    String getPersistenceUnitName(Project project) {
        String puName = (String) wizardDescriptor.getProperty(WizardProperties.PERSISTENCE_UNIT_NAME);
        if (puName == null || puName.trim().length() == 0) {
            puName = PersistenceHelper.getPersistenceUnitName(project);
            wizardDescriptor.putProperty(WizardProperties.PERSISTENCE_UNIT_NAME, puName);
        }
        return puName;
    }
    
    private void setErrorMessage(String key) {
        if ( key == null ) {
            setLocalizedErrorMessage(""); // NOI18N
        } else {
            setLocalizedErrorMessage(
                    NbBundle.getMessage(EntitySelectionPanel.class, key)); // NOI18N
        }
    }
    
    private void setLocalizedErrorMessage(String message) {
        wizardDescriptor.putProperty("WizardPanel_errorMessage", message); // NOI18N
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
    protected final void fireChangeEvent(ChangeEvent ev) {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read(wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) component).getClientProperty("NewProjectWizard_Title"); // NOI18N
        if (substitute != null){
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); // NOI18N
        }
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
        //d.putProperty(WizardProperties.PERSISTENCE_UNIT, component.getPersistenceUnit());
        ((WizardDescriptor) d).putProperty("NewProjectWizard_Title", null); // NOI18N
    }
    
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent(e);
    }
    
}
