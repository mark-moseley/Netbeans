/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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
package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class SimpleConfigurationWizard implements WizardDescriptor.Panel, ChangeListener {
    
    private DiscoveryDescriptor wizardDescriptor;
    private SimpleConfigurationPanel component;
    private String name;
    private boolean inited = false;
    
    public SimpleConfigurationWizard(){
        name = NbBundle.getMessage(SimpleConfigurationWizard.class, "SimpleConfigurationName"); // NOI18N
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new SimpleConfigurationPanel(this);
            component.setName(name);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(DiscoveryWizardAction.HELP_CONTEXT);
    }
    
    public boolean isValid() {
        boolean valid = ((SimpleConfigurationPanel)getComponent()).valid();
        if (valid) {
            wizardDescriptor.setMessage(""); // NOI18N
        }
        return valid;
    }
    
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
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
    
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }
    
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        if (!inited) {
            wizardDescriptor = DiscoveryWizardDescriptor.adaptee(settings);
            component.read(wizardDescriptor);
            inited = true;
        }
    }
    
    public void storeSettings(Object settings) {
        DiscoveryDescriptor wizardDescriptor = DiscoveryWizardDescriptor.adaptee(settings);
        component.store(DiscoveryWizardDescriptor.adaptee(settings));
    }
}

