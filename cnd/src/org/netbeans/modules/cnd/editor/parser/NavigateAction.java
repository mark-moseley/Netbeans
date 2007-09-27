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

package  org.netbeans.modules.cnd.editor.parser;


import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openide.nodes.Node;

import org.openide.explorer.ExplorerManager;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 * loosly based on java/src/org/netbeans/modules/java/ui/NavigationAction.java
 */
public class NavigateAction extends CookieAction {

    /**
     * Cookies required for this action to be activated.
     */
    private static final Class[] REQUIRED_COOKIES = new Class[] {
//        SourceCookie.Editor.class
    };
    
    static String getString(String key) {
        return NbBundle.getMessage(NavigateAction.class, key);
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    /**
     * Return the display name for the action.
     */
    public String getName() {
        return getString("LAB_NavigateAction"); // NOI18N
    }

    /**
     * The action accepts only one node with a java source on it.
     */
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    protected Class[] cookieClasses() {
        return REQUIRED_COOKIES;
    }
    
    /**
     * performAction will activate the associated choice navigation component.
     */
    protected void performAction(Node[] activatedNodes) {
        // PENDING: activate the presenter -- what presenter ??
    }
    
    public java.awt.Component getToolbarPresenter() {
        java.awt.Component c = new ToolbarPresenter();
        return c;
    }
    
    private static class ToolbarPresenter extends JPanel implements ExplorerManager.Provider {
        
        private static final int FIXED_WIDTH = 220;

        NavigationView      explorerView;
        JLabel              comboLabel;
        ExplorerManager     manager;
        
        ToolbarPresenter() {
            initComponents();
        }
        
        public void addNotify() {
            if (manager == null)
                manager = ExplorerManager.find(this);
            super.addNotify();
        }
        
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        
        private void initComponents() {
            setLayout(new BorderLayout(12, 0));

            //if (System.getProperty("os.name", "").toLowerCase().indexOf("windows") == -1)
            //{
                explorerView = new NavigationView();
                explorerView.setMaximumRowCount(15);

                char mnemChar = NavigateAction.getString("LAB_NavigationList_Mnem").charAt(0);
                //explorerView.setToolTipText(NavigateAction.getString("HINT_NavigationList") + " (Alt+" + mnemChar + ')');

                comboLabel = new JLabel();
    //            comboLabel.setText(NavigateAction.getString("LAB_NavigationList"));
                comboLabel.setDisplayedMnemonic(mnemChar);
    //            comboLabel.setToolTipText(NavigateAction.getString("HINT_NavigationList"));
                comboLabel.setLabelFor(explorerView);

                add(explorerView, BorderLayout.CENTER);
                add(comboLabel, BorderLayout.WEST);
            //}
        }
        
        public java.awt.Dimension getMinimumSize() {
            // minimum width to prevent excessive horizontal shrinking
            return new java.awt.Dimension(FIXED_WIDTH, getPreferredSize().height);
        }
        
        public java.awt.Dimension getMaximumSize() {
            // minimum width to prevent moving of the remaining contents of the toolbar
            return new java.awt.Dimension(FIXED_WIDTH, getPreferredSize().height);
        }
    }
}
