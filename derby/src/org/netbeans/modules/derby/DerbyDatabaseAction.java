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

package org.netbeans.modules.derby;

import java.awt.Component;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 * A dummy action serving as the Derby Database menu item. Allows 
 * showing and hiding the menu item programmatically.
 *
 * @author Andrei Badea
 */
public class DerbyDatabaseAction extends AbstractAction implements Presenter.Menu {
    
    // XXX should perhaps get rid of DynamicMenuContent 
    // since now the Java DB Database menu is always visible
    
    private JMenuItem menuPresenter = null;
    
    public DerbyDatabaseAction() {
        super(NbBundle.getMessage(DerbyDatabaseAction.class, "LBL_DerbyDatabaseAction"));
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
    }

    public JMenuItem getMenuPresenter() {
        if (menuPresenter == null) {
            menuPresenter = new MenuPresenter();
        }
        return menuPresenter;
    }
    
    private final class MenuPresenter extends JMenu implements DynamicMenuContent, MenuListener {
        
        public MenuPresenter() {
            super();
            Mnemonics.setLocalizedText(this, (String)getValue(Action.NAME));
            addMenuListener(this);
        }
        
        public JComponent[] synchMenuPresenters(javax.swing.JComponent[] items) {
            return getMenuPresenters();
        }

        public JComponent[] getMenuPresenters() {
            return new JComponent[] { this };
        }

        public void menuSelected(MenuEvent e) {
            getPopupMenu().removeAll();
            JPopupMenu menu = Utilities.actionsToPopup(new Action[] {
                SystemAction.get(StartAction.class),
                SystemAction.get(StopAction.class),
                SystemAction.get(CreateDatabaseAction.class),
                SystemAction.get(DerbyPropertiesAction.class),
            }, Utilities.actionsGlobalContext());
            while (menu.getComponentCount() > 0) {
                Component c = menu.getComponent(0);
                menu.remove(0);
                getPopupMenu().add(c);
            }
        }
        
        public void menuCanceled(MenuEvent e) {
        }
        
        public void menuDeselected(MenuEvent e) {
        }
    }
}
