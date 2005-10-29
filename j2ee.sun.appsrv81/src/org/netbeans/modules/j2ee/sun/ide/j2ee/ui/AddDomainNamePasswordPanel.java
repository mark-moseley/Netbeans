/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** Queries the user for the name and password when registering an instance.
 *
 */
class AddDomainNamePasswordPanel implements WizardDescriptor.Panel, ChangeListener {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private AddInstanceVisualNamePasswordPanel component;

    private WizardDescriptor wiz;
        
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new AddInstanceVisualNamePasswordPanel();
            component.addChangeListener(this);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        // TODO get help content
        return HelpCtx.DEFAULT_HELP; // new HelpCtx(SampleWizardPanel1.class);
    }
    
    /** Determine if the content is valid.
     *
     * The constraints on this is complex.  If there is a user name entry, there
     * must be a password entry at least 8 chanracters long (trimmed for spaces).
     *
     * If there is a password entry, it must be atleast 8 characters long.
     * 
     * If there is a password entry, there must be a user name entry.
     *
     * If the page appears in the wizard for creating a personal instance, there
     * be a passowrd entry.
     */
    public boolean isValid() {
        String password = component.getPWord().trim();
        String username = component.getUName().trim();
        
        wiz.putProperty(AddDomainWizardIterator.USER_NAME,username);
        wiz.putProperty(AddDomainWizardIterator.PASSWORD,password);
        wiz.putProperty(AddDomainWizardIterator.PROP_ERROR_MESSAGE, null);
        return true;
    }
    
    // Event handling
    //
    private final Set/*<ChangeListener>*/ listeners = new HashSet/*<ChangeListener>*/(1);
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
        Iterator/*<ChangeListener>*/ it;
        synchronized (listeners) {
            it = new HashSet/*<ChangeListener>*/(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings(Object settings) {
        wiz = (WizardDescriptor) settings;
    }
    public void storeSettings(Object settings) {
        // TODO implement?
    }

    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }
    
}

