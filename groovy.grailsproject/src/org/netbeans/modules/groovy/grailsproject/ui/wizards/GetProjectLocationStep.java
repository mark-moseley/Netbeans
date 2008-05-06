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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grailsproject.ui.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author schmidtm
 */
public class GetProjectLocationStep implements  WizardDescriptor.Panel, 
                                                WizardDescriptor.ValidatingPanel,
                                                WizardDescriptor.FinishablePanel
                                                {

    private GetProjectLocationPanel component;
    private WizardDescriptor wizardDescriptor;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    boolean        serverRunning = false;
    boolean        serverConfigured = true;

    public GetProjectLocationStep(boolean serverRunning, boolean serverConfigured) {
        this.serverRunning = serverRunning;
        this.serverConfigured = serverConfigured;
    }

    public Component getComponent() {
        if (component == null) {
            component = new GetProjectLocationPanel(this);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return new HelpCtx( GetProjectLocationStep.class  );
    }

    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;        
        component.read (wizardDescriptor);

    }

    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor)settings;
        component.store(d);

    }

    public boolean isValid() {
        getComponent();
        
        if(!serverConfigured) {
            wizardDescriptor.putProperty("WizardPanel_errorMessage", 
                NbBundle.getMessage(NewGrailsProjectWizardIterator.class, 
                "NewGrailsProjectWizardIterator.NoGrailsServerConfigured"));
            }
        
        return  !serverRunning && serverConfigured && component.valid( wizardDescriptor );
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public void fireChangeEvent() {
        changeSupport.fireChange();
    }
    
    
    public void validate() throws WizardValidationException {
        getComponent ();
        component.validate (wizardDescriptor);
    }

    public boolean isFinishPanel() {
        return true;
    }

}
