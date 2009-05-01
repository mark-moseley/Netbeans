/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.remote.ui.setup;

import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cnd.spi.remote.setup.HostSetupProvider;
import org.netbeans.modules.cnd.spi.remote.setup.HostSetupWorker;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/*package*/ final class CreateHostWizardPanel0 implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    private CreateHostVisualPanel0 component;
    private final List<HostSetupProvider> providers;
    private final ChangeListener changeListener;
    private HostSetupProvider lastSelectedProvider;

    private HostSetupWorker selectedWorker;

    public CreateHostWizardPanel0(ChangeListener changeListener, List<HostSetupProvider> providers) {
        this.providers = providers;
        this.changeListener = changeListener;
        this.lastSelectedProvider = providers.get(0);
        this.selectedWorker = lastSelectedProvider.createHostSetupWorker();
    }

    public HostSetupWorker getSelectedWorker() {
        return selectedWorker;
    }

    public CreateHostVisualPanel0 getComponent() {
        if (component == null) {
            component = new CreateHostVisualPanel0(this, providers);
        }
        return component;
    }

    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    public boolean isValid() {
        return getComponent().getSelectedProvider() != null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // change support
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public void stateChanged(ChangeEvent e) {
        changeSupport.fireChange();
    }

    ////////////////////////////////////////////////////////////////////////////
    // settings
    public void readSettings(WizardDescriptor settings) {
        getComponent().reset();
    }

    public void storeSettings(WizardDescriptor settings) {
        HostSetupProvider provider = getComponent().getSelectedProvider();
        assert provider != null;

        if (!provider.equals(lastSelectedProvider)) {
            lastSelectedProvider = provider;
            selectedWorker = provider.createHostSetupWorker();
            changeListener.stateChanged(new ChangeEvent(this));
        }
    }
}
