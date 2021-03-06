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

package org.netbeans.modules.visualweb.project.jsf.ui;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectConstants;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Po-Ting Wu
 */
final class PagebeanPackagePanel implements WizardDescriptor.Panel, ChangeListener {

    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    private PagebeanPackagePanelGUI gui;

    private Project project;
    private WizardDescriptor wizard;

    PagebeanPackagePanel(Project project) {
        this.project = project;
        this.gui = null;
    }

    public Component getComponent() {
        if (gui == null) {
            gui = new PagebeanPackagePanelGUI(project);
            gui.addChangeListener(this);
        }
        return gui;
    }

    public HelpCtx getHelp() {
        return null;
    }

    public boolean isValid() {
        if (gui == null) {
            return false;
        }

        // Check to make sure that the package name is valid
        String packageName = gui.getPackageName();
        if (!JsfProjectUtils.isValidJavaPackageName(packageName)) {
            wizard.putProperty("WizardPanel_errorMessage", NbBundle.getMessage(PagebeanPackagePanel.class, "MSG_InvalidPackageName", packageName)); // NOI18N
            return false;
        }

        return true;
    }

    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        List templist;
        synchronized (this) {
            templist = new ArrayList (listeners);
        }
        Iterator it = templist.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }

    public void readSettings(Object settings) {
        wizard = (WizardDescriptor) settings;
                
        if (gui == null) {
            getComponent();
        }
        
        gui.initValues(project);
    }
    
    public void storeSettings(Object settings) { 
        if (WizardDescriptor.PREVIOUS_OPTION.equals(((WizardDescriptor) settings).getValue())) {
            return;
        }

        if (isValid()) {
            ((WizardDescriptor) settings).putProperty(JsfProjectConstants.PROP_JSF_PAGEBEAN_PACKAGE, gui.getPackageName());
        }
    }

    public void stateChanged(ChangeEvent e) {        
        if (wizard != null && isValid()) {
            wizard.putProperty(JsfProjectConstants.PROP_JSF_PAGEBEAN_PACKAGE, gui.getPackageName());
        }

        fireChange();
    }
}
