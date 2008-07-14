// <editor-fold defaultstate="collapsed" desc=" License Header ">
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
//</editor-fold>

package org.netbeans.modules.glassfish.common.wizards;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import java.awt.Component;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AddDomainLocationPanel implements WizardDescriptor.Panel, ChangeListener {

    private final static String PROP_ERROR_MESSAGE = WizardDescriptor.PROP_ERROR_MESSAGE; // NOI18   
    private ServerWizardIterator wizardIterator;
    private AddDomainLocationVisualPanel component;
    private WizardDescriptor wizard;
    private transient Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    private String gfRoot;

    /**
     * 
     * @param instantiatingIterator 
     */
    public AddDomainLocationPanel(ServerWizardIterator wizardIterator) {
        this.wizardIterator = wizardIterator;
        wizard = null;
    }

    private AtomicBoolean isValidating = new AtomicBoolean();

    /**
     * 
     * @return 
     */
    public boolean isValid() {
        if (isValidating.compareAndSet(false, true)) {
            try {
                AddDomainLocationVisualPanel panel = (AddDomainLocationVisualPanel) getComponent();
                String domainField = panel.getDomainField();
                int dex = domainField.indexOf(File.separator);
                File domainDirCandidate = new File(gfRoot, "domains" + File.separator + domainField); // NOI18N
                if (AddServerLocationPanel.isRegisterableV3Domain(domainDirCandidate)) { // .exists() && logsDir.canWrite()) {
                    // the entry resolves to a domain name that we can register
                    wizardIterator.setDomainLocation(domainDirCandidate.getAbsolutePath());
                    wizard.putProperty(PROP_ERROR_MESSAGE, "Register existing embedded domain: " + domainField);
                    AddServerLocationPanel.readServerConfiguration(domainDirCandidate, wizardIterator);
                    return true;
                }
                File domainsDir = domainDirCandidate.getParentFile();
                if (domainsDir.canWrite() && dex < 0) {
                    wizardIterator.setDomainLocation(domainDirCandidate.getAbsolutePath());
                    wizard.putProperty(PROP_ERROR_MESSAGE, "Create embedded domain: " + domainField);
                    return true;
                }
                domainDirCandidate = new File(domainField);
                String domainLoc = domainDirCandidate.getAbsolutePath();
                if (AddServerLocationPanel.isRegisterableV3Domain(domainDirCandidate)) { // .exists() && logsDir.canWrite()) {
                    // the entry resolves to a domain name that we can register
                    //String domainLoc = domainDirCandidate.getAbsolutePath();
                    wizardIterator.setDomainLocation(domainLoc);
                    wizard.putProperty(PROP_ERROR_MESSAGE, "Register existing domain: " + domainLoc);
                    AddServerLocationPanel.readServerConfiguration(domainDirCandidate, wizardIterator);
                    return true;
                }
                if (AddServerLocationPanel.canCreate(domainDirCandidate)) {
                    wizardIterator.setDomainLocation(domainLoc);
                    wizard.putProperty(PROP_ERROR_MESSAGE, "Create domain: " + domainLoc);
                    return true;
                }
                wizard.putProperty(PROP_ERROR_MESSAGE, "Cannot create domain: " + domainLoc);
                return false;

            //}

            ///return true;
            } finally {
                isValidating.set(false);
            }
        }
        return true;
    }

    /**
     * 
     * @param ev 
     */
    public void stateChanged(ChangeEvent ev) {
        fireChangeEvent(ev);
    }

    /**
     * 
     * @param l 
     */
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    /**
     * 
     * @param l 
     */
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChangeEvent(ChangeEvent ev) {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }

    /**
     * 
     * @return 
     */
    public Component getComponent() {
        if (component == null) {
            component = new AddDomainLocationVisualPanel();
            component.addChangeListener(this);
        }
        return component;
    }

    /**
     * 
     * @return 
     */
    public HelpCtx getHelp() {
        // !PW FIXME correct help context
        return new HelpCtx("registering_app_server_hk2_domain"); //NOI18N
    }

    /**
     * 
     * @param settings 
     */
    public void readSettings(Object settings) {
        //Thread.dumpStack();
        if (wizard == null) {
            wizard = (WizardDescriptor) settings;
        }
        gfRoot = wizardIterator.getGlassfishRoot();
        ((AddDomainLocationVisualPanel) getComponent()).initModels(gfRoot);
    }

    /**
     * 
     * @param settings 
     */
    public void storeSettings(Object settings) {
        //Thread.dumpStack();
    }
}