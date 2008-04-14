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

package org.netbeans.modules.server.test;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.server.ServerRegistry;
import org.netbeans.spi.server.ServerInstanceProvider;
import org.netbeans.spi.server.ServerWizardProvider;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.InstantiatingIterator;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.modules.ModuleInfo;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
public class MockWizardProvider implements ServerWizardProvider {

    private final String wizardName;

    public MockWizardProvider(String wizardName) {
        this.wizardName = wizardName;
    }

    public static void registerWizardProvider(String instanceName, ServerWizardProvider provider) throws IOException {
        if (provider == null) {
            return;
        }

        Lookup.getDefault().lookup(ModuleInfo.class);

        FileObject servers = Repository.getDefault().getDefaultFileSystem().getRoot()
                .getFileObject(ServerRegistry.SERVERS_PATH);
        FileObject testProvider = FileUtil.createData(servers, instanceName);

        testProvider.setAttribute("instanceOf", ServerWizardProvider.class.getName()); // NOI18N
        testProvider.setAttribute("instanceCreate", provider); // NOI18N
    }

    public String getDisplayName() {
        return wizardName;
    }

    public InstantiatingIterator getInstantiatingIterator() {
        return new MockWizardIterator(wizardName);
    }

    private static class MockWizardIterator implements InstantiatingIterator {

        private final String name;

        private Panel panel;

        public MockWizardIterator(String name) {
            this.name = name;
        }

        public Set instantiate() throws IOException {
            return Collections.EMPTY_SET;
        }

        public String name() {
            return name;
        }

        public synchronized Panel current() {
            if (panel == null) {
                panel = new MockWizardPanel(name);
            }
            return panel;
        }

        public boolean hasNext() {
            return false;
        }

        public boolean hasPrevious() {
            return false;
        }

        public void initialize(WizardDescriptor wizard) {
        }

        public void uninitialize(WizardDescriptor wizard) {
        }

        public void nextPanel() {
        }

        public void previousPanel() {
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }
    }

    private static class MockWizardPanel implements Panel {

        private final String name;

        private JPanel panel;

        public MockWizardPanel(String name) {
            this.name = name;
        }

        public synchronized Component getComponent() {
            if (panel == null) {
                panel = new JPanel();
                panel.add(new JLabel(name));
            }
            return panel;
        }

        public HelpCtx getHelp() {
            return HelpCtx.DEFAULT_HELP;
        }

        public boolean isValid() {
            return true;
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }

        public void readSettings(Object settings) {
        }

        public void storeSettings(Object settings) {
        }

    }
}
