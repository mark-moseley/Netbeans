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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.websvc.rest.wizard;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.FinishablePanel;
import org.openide.WizardDescriptor.Panel;
import org.openide.util.NbBundle;

/**
 *
 * @author nam
 */
public abstract class AbstractPanel implements ChangeListener, FinishablePanel, Panel {
    private final java.util.Set listeners = new HashSet(1);
    protected java.lang.String panelName;
    protected org.openide.WizardDescriptor wizardDescriptor;

    public AbstractPanel (String name, WizardDescriptor wizardDescriptor) {
        this.panelName = name;
        this.wizardDescriptor = wizardDescriptor;
    }
    
    public abstract java.awt.Component getComponent();

    public abstract boolean isFinishPanel();

    public static interface Settings {
        public void read(WizardDescriptor wizard);
        public void store(WizardDescriptor wizard);
        public boolean valid(WizardDescriptor wizard);
        public void addChangeListener(ChangeListener l);
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        ((Settings)getComponent()).read(wizardDescriptor);
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        ((Settings)getComponent()).store(wizardDescriptor);
    }

    public boolean isValid() {
        if (getComponent() instanceof Settings) {
            return ((Settings)getComponent()).valid(wizardDescriptor);
        }
        return false;
    }
    
    public final void addChangeListener(javax.swing.event.ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    protected final void fireChangeEvent(javax.swing.event.ChangeEvent ev) {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }

    public final void removeChangeListener(javax.swing.event.ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    public static void clearErrorMessage(WizardDescriptor wizard) {
        setErrorMessage(wizard, (String) null);
    }
    
    public static void setErrorMessage(WizardDescriptor wizard, Throwable t) {
        String message = "";
        if (t != null) {
            message = (t.getLocalizedMessage());
        }
        wizard.putProperty("WizardPanel_errorMessage", message);
    }
    
    static void setErrorMessage(WizardDescriptor wizard, String key, String... params) {
        String message = "";
        if (key != null) {
            message = (NbBundle.getMessage(EntitySelectionPanel.class, key, params));
        }
        wizard.putProperty("WizardPanel_errorMessage", message);
    }
    
    public static void setErrorMessage(WizardDescriptor wizard, String key) {
        String message = "";
        if (key != null) {
            message = (NbBundle.getMessage(EntitySelectionPanel.class, key));
        }
        wizard.putProperty("WizardPanel_errorMessage", message);
    }

    protected void setErrorMessage(java.lang.String key) {
        setErrorMessage(wizardDescriptor, key);
    }

    public void stateChanged(javax.swing.event.ChangeEvent e) {
        Component c = getComponent();
        if (c instanceof Settings) {
            ((Settings)c).valid(wizardDescriptor);
        }
        fireChangeEvent(e);
    }
    
    public String getName() {
        return panelName;
    }
}
