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
package org.netbeans.modules.bugtracking.util;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.spi.options.AdvancedOption;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;


/**
 * Bugtracking options panel combined from panels for various settings.
 *
 * @author Pavel Buzek
 * @author Tomas Stupka
 */
public class BugtrackingOptions extends AdvancedOption {
    public String getTooltip() {
        return getDisplayName();
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(BugtrackingOptions.class, "LBL_IssueTracking"); // NOI18N
    }
    
    public OptionsPanelController create() {
        return new Controller();
    }
    
    private static final class Controller extends OptionsPanelController {
        private BugtrackingOptionsPanel panel;
        private boolean initialized = false;
        private Map<String, OptionsPanelController> categoryToController = new HashMap<String, OptionsPanelController>();
        
        private void init(Lookup masterLookup) {
            if (initialized) return;
            initialized = true;
            panel = new BugtrackingOptionsPanel();
            
            Lookup lookup = Lookups.forPath("BugtrackingOptionsDialog"); // NOI18N
            Iterator<? extends AdvancedOption> it = lookup.lookup(new Lookup.Template<AdvancedOption> (AdvancedOption.class)).
                    allInstances().iterator();
            while (it.hasNext()) {
                AdvancedOption option = it.next();
                String category = option.getDisplayName();
                OptionsPanelController controller = option.create();
                categoryToController.put(category, controller);
                panel.addPanel(category, controller.getComponent(masterLookup));
            }
        }
        
        public JComponent getComponent(Lookup masterLookup) {
            init(masterLookup);
            return panel;
        }
        
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }
        
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }
        
        public void update() {
            Iterator it = categoryToController.values().iterator();
            while (it.hasNext())
                ((OptionsPanelController) it.next()).update();
        }
        
        public void applyChanges() {
            Iterator it = categoryToController.values().iterator();
            while (it.hasNext())
                ((OptionsPanelController) it.next()).applyChanges();
        }
        
        public void cancel() {
            Iterator it = categoryToController.values().iterator();
            while (it.hasNext())
                ((OptionsPanelController) it.next()).cancel();
        }
        
        public boolean isValid() {
            Iterator it = categoryToController.values().iterator();
            while (it.hasNext())
                if (!((OptionsPanelController) it.next()).isValid())
                    return false;
            return true;
        }
        
        public boolean isChanged() {
            Iterator it = categoryToController.values().iterator();
            while (it.hasNext())
                if (((OptionsPanelController) it.next()).isChanged())
                    return true;
            return false;
        }
        
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(BugtrackingOptions.class);
        }
    }
}
