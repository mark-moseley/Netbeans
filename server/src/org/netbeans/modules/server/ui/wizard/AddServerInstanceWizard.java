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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.server.ui.wizard;

import java.awt.Dialog;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.server.ServerInstance;
import org.netbeans.spi.server.ServerWizardProvider;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 * @author Petr Hejl
 */
public class AddServerInstanceWizard extends WizardDescriptor {

    public static final String PROP_DISPLAY_NAME = "ServInstWizard_displayName"; // NOI18N

    public static final String PROP_SERVER_INSTANCE_WIZARD = "ServInstWizard_server"; // NOI18N

    private static final String PROP_AUTO_WIZARD_STYLE = "WizardPanel_autoWizardStyle"; // NOI18N

    private static final String PROP_CONTENT_DISPLAYED = "WizardPanel_contentDisplayed"; // NOI18N

    private static final String PROP_CONTENT_NUMBERED = "WizardPanel_contentNumbered"; // NOI18N

    private static final String PROP_CONTENT_DATA = "WizardPanel_contentData"; // NOI18N

    private static final String PROP_CONTENT_SELECTED_INDEX = "WizardPanel_contentSelectedIndex"; // NOI18N

    private static final String PROP_ERROR_MESSAGE = "WizardPanel_errorMessage"; // NOI18N

    private AddServerInstanceWizardIterator iterator;

    private ServerWizardPanel chooser;

    private static final Logger LOGGER = Logger.getLogger(AddServerInstanceWizard.class.getName()); // NOI18N

    private AddServerInstanceWizard() {
        this(new AddServerInstanceWizardIterator());

        putProperty(PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
        putProperty(PROP_CONTENT_DISPLAYED, Boolean.TRUE);
        putProperty(PROP_CONTENT_NUMBERED, Boolean.TRUE);

        setTitle(NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ASIW_Title"));
        setTitleFormat(new MessageFormat(NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ASIW_TitleFormat")));

        initialize();
    }

    private AddServerInstanceWizard(AddServerInstanceWizardIterator iterator) {
        super(iterator);
        this.iterator = iterator;
    }


    public static ServerInstance showAddServerInstanceWizard() {
        AddServerInstanceWizard wizard = new AddServerInstanceWizard();

        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizard);
        try {
            dialog.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(AddServerInstanceWizard.class, "ACSD_Add_Server_Instance"));
            dialog.setVisible(true);
        } finally {
            dialog.dispose();
        }

        if (wizard.getValue() == WizardDescriptor.FINISH_OPTION) {
            Set instantiatedObjects = wizard.getInstantiatedObjects();
            if (instantiatedObjects != null && instantiatedObjects.size() > 0) {
                Object result = instantiatedObjects.iterator().next();
                if (result instanceof ServerInstance) {
                    return (ServerInstance) result;
                } else {
                    LOGGER.log(Level.WARNING, "Some localized warning"); // NOI18N
                    return null;
                }
            }
        }
        // the wizard was cancelled
        return null;
    }

    public void setErrorMessage(String message) {
        putProperty(PROP_ERROR_MESSAGE, message);
    }

    @Override
    protected void updateState() {
        super.updateState();

        String[] contentData = getContentData();
        if (contentData != null) {
            putProperty(PROP_CONTENT_DATA, contentData);
            putProperty(PROP_CONTENT_SELECTED_INDEX, Integer.valueOf(getContentSelectedIndex()));
        }
    }

    private ServerWizardPanel getChooser() {
        if (chooser == null) {
            chooser = new ServerWizardPanel();
        }
        return chooser;
    }

    private String[] getContentData() {
        JComponent first = (JComponent) getChooser().getComponent();
        String[] firstContentData = (String[]) first.getClientProperty(PROP_CONTENT_DATA);

        if (iterator.current().equals(getChooser())) {
            return firstContentData;
        } else {
            JComponent component = (JComponent) iterator.current().getComponent();
            String[] componentContentData = (String[]) component.getClientProperty(PROP_CONTENT_DATA);
            if (componentContentData == null) {
                return firstContentData;
            }

            String[] contentData = new String[componentContentData.length + 1];
            contentData[0] = firstContentData[0];
            System.arraycopy(componentContentData, 0, contentData, 1, componentContentData.length);
            return contentData;
        }
    }

    private int getContentSelectedIndex() {
        if (iterator.current().equals(getChooser())) {
            return 0;
        } else {
            JComponent component = (JComponent) iterator.current().getComponent();
            Integer componentIndex = (Integer) component.getClientProperty(PROP_CONTENT_SELECTED_INDEX);
            if (componentIndex != null) {
                return componentIndex.intValue() + 1;
            } else {
                return 1;
            }
        }
    }

    private static class AddServerInstanceWizardIterator implements WizardDescriptor.InstantiatingIterator {

        private final Map<ServerWizardProvider, InstantiatingIterator> iterators = new HashMap<ServerWizardProvider, InstantiatingIterator>();

        private WizardDescriptor.InstantiatingIterator iterator;

        private AddServerInstanceWizard wd;

        public boolean showingChooser = true;

        public AddServerInstanceWizardIterator() {
            super();
        }

        public String name() {
            return null;
        }

        public WizardDescriptor.Panel current() {
            if (showingChooser) {
                return wd.getChooser();
            } else {
                if (iterator != null) {
                    return iterator.current();
                } else {
                    return null;
                }
            }
        }

        public boolean hasNext() {
            if (showingChooser) {
                return true;
            } else {
                if (iterator != null) {
                    return iterator.hasNext();
                } else {
                    return false;
                }
            }
        }

        public boolean hasPrevious() {
            return !showingChooser;
        }

        public void nextPanel() {
            if (iterator == null) {
                iterator = getServerIterator();
            } else {
                if (!showingChooser) {
                    iterator.nextPanel();
                }
            }
            showingChooser = false;
        }

        public void previousPanel() {
            if (iterator.hasPrevious()) {
                iterator.previousPanel();
            } else {
                showingChooser = true;
                iterator = null;
            }
        }

        public void addChangeListener(ChangeListener l) {
        }

        public void removeChangeListener(ChangeListener l) {
        }

        public void uninitialize(WizardDescriptor wizard) {
        }

        public void initialize(WizardDescriptor wizard) {
            wd = (AddServerInstanceWizard) wizard;

            JComponent chooser = (JComponent) wd.getChooser().getComponent();
            chooser.putClientProperty(PROP_CONTENT_DATA, new String[] {
                NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ASIW_ChooseServer"),
                NbBundle.getMessage(AddServerInstanceWizard.class, "LBL_ASIW_Ellipsis")
            });
        }

        public Set instantiate() throws IOException {
            if (iterator != null) {
                return iterator.instantiate();
            } else {
                return null;
            }
        }

        private WizardDescriptor.InstantiatingIterator getServerIterator() {
            ServerWizardProvider server = getSelectedWizard();
            if (server == null) {
                return null;
            }

            WizardDescriptor.InstantiatingIterator iterator = (WizardDescriptor.InstantiatingIterator)iterators.get(server);
            if (iterator != null) {
                return iterator;
            }


            iterator = server.getInstantiatingIterator();
            iterator.initialize(wd);
            iterators.put(server, iterator);
            return iterator;
        }

        public ServerWizardProvider getSelectedWizard() {
            return (ServerWizardProvider) wd.getProperty(PROP_SERVER_INSTANCE_WIZARD);
        }
    }
}
