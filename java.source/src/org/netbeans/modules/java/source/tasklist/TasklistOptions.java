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
package org.netbeans.modules.java.source.tasklist;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Lahoda
 */
public class TasklistOptions extends AdvancedOption {

    public TasklistOptions() {
    }

    public String getDisplayName() {
        return NbBundle.getMessage(TasklistOptions.class, "DN_Tasklist");// "Java Tasklist - Temporary settings";
    }

    public String getTooltip() {
        return NbBundle.getMessage(TasklistOptions.class, "TP_Tasklist");// "Java Tasklist - Temporary settings";
    }

    public OptionsPanelController create() {
        return new TasklistOptionsPanelController();
    }

    private static class TasklistOptionsPanelController extends OptionsPanelController implements ChangeListener {

        private TasklistOptionsPanel panel;
        private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        public void update() {
            assert SwingUtilities.isEventDispatchThread();
            if (panel == null) {
                getComponent(null);//XXX: should not happen
            }
            
            panel.setDependenciesEnabled(TasklistSettings.isDependencyTrackingEnabled());
            panel.setBadgesEnabled(TasklistSettings.isBadgesEnabled());
            panel.setTasklistEnabled(TasklistSettings.isTasklistEnabled());
            
        }

        public void applyChanges() {
            if (panel == null)
                return ;
            
            TasklistSettings.setTasklistsEnabled(panel.getTasklistEnabled());
            TasklistSettings.setDependencyTrackingEnabled(panel.getDependenciesEnabled());
            TasklistSettings.setBadgesEnabled(panel.getBadgesEnabled());
            
        }

        public void cancel() {
            if (panel == null)
                return ;
            panel.setTasklistEnabled(TasklistSettings.isTasklistEnabled());
            panel.setDependenciesEnabled(TasklistSettings.isDependencyTrackingEnabled());
            panel.setBadgesEnabled(TasklistSettings.isBadgesEnabled());
        }

        public boolean isValid() {
            return true;
        }

        public boolean isChanged() {
            if (panel == null)
                return false;

            return    TasklistSettings.isTasklistEnabled() != panel.getTasklistEnabled()
                   || TasklistSettings.isDependencyTrackingEnabled() != panel.getDependenciesEnabled()
                   || TasklistSettings.isBadgesEnabled() != panel.getBadgesEnabled();
        }

        public JComponent getComponent(Lookup masterLookup) {
            if (panel == null) {
                panel = new TasklistOptionsPanel();
                panel.addChangeListener(this);
            }
            return panel;
        }

        public HelpCtx getHelpCtx() {
            return new HelpCtx("netbeans.optionsDialog.java.tasklist");
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }

        public void stateChanged(ChangeEvent e) {
            pcs.firePropertyChange(PROP_CHANGED, null, null);
        }
        
    }
}
