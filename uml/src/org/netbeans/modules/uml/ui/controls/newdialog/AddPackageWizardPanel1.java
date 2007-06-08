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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.ui.controls.newdialog;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

public class AddPackageWizardPanel1 
      implements WizardDescriptor.FinishablePanel, INewUMLFileTemplates
{
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private AddPackageVisualPanel1 component;
    private WizardDescriptor wizardDescriptor;
    private final Set/*<ChangeListener>*/ listeners = new HashSet (1);
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent ()
    {
        if (component == null)
        {
            component = new AddPackageVisualPanel1 (this);
        }
        return component;
    }
    
    public HelpCtx getHelp ()
    {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }
    
    public boolean isValid ()
    {
        getComponent();
        return component.isValid (wizardDescriptor);
    }
    
    public final void addChangeListener (ChangeListener l)
    {
        synchronized (listeners)
        {
            listeners.add (l);
        }
    }
    
    public final void removeChangeListener (ChangeListener l)
    {
        synchronized (listeners)
        {
            listeners.remove (l);
        }
    }
    
    public final void fireChangeEvent ()
    {
        Iterator it;
        synchronized (listeners)
        {
            it = new HashSet (listeners).iterator ();
        }
        
        ChangeEvent ev = new ChangeEvent (this);
        
        while (it.hasNext ())
        {
            ((ChangeListener)it.next ()).stateChanged (ev);
        }
    }
    
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    public void readSettings (Object settings)
    {
        wizardDescriptor = (WizardDescriptor)settings;
        AddPackageVisualPanel1 comp = (AddPackageVisualPanel1) getComponent();
        comp.read(wizardDescriptor);
    }
    
    public void storeSettings (Object settings)
    {
        wizardDescriptor = (WizardDescriptor)settings;
        AddPackageVisualPanel1 comp = (AddPackageVisualPanel1) getComponent();
        comp.store(wizardDescriptor);
    }

    public boolean isFinishPanel() {
        return true;
    }
}

