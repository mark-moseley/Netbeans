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

package org.netbeans.swing.tabcontrol;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.swing.popupswitcher.SwitcherTableItem;
import org.openide.windows.TopComponent;

/**
 * An action which, when invoked, displays a popup with an alphabetized table
 * of all the tabs in a TabDisplayer.
 *
 * @author  Tim Boudreau
 */
public class TabListPopupAction extends AbstractAction {
    
    private TabDisplayer displayer;
    
    public TabListPopupAction(TabDisplayer displayer) {
        this.displayer = displayer;
    }
    
    public void actionPerformed(ActionEvent ae) {
        if ("pressed".equals(ae.getActionCommand())) {
            JComponent jc = (JComponent) ae.getSource();
            Point p = new Point(jc.getWidth(), jc.getHeight());
            SwingUtilities.convertPointToScreen(p, jc);
            if (!ButtonPopupSwitcher.isShown()) {
                SwitcherTableItem[] items = createSwitcherItems(displayer);
                Arrays.sort(items);
                ButtonPopupSwitcher.selectItem(jc, items, p.x, p.y);
            }
            //Other portion of issue 37487, looks funny if the
            //button becomes pressed
            if (jc instanceof AbstractButton) {
                AbstractButton jb = (AbstractButton) jc;
                jb.getModel().setPressed(false);
                jb.getModel().setRollover(false);
                jb.getModel().setArmed(false);
                jb.repaint();
            }
        }
    }
    
    private SwitcherTableItem[] createSwitcherItems(TabDisplayer displayer) {
        List tabs = displayer.getModel().getTabs();
        SwitcherTableItem[] items = new SwitcherTableItem[tabs.size()];
        int i = 0;
        int selIdx = displayer.getSelectionModel().getSelectedIndex();
        TabData selectedTab = selIdx >= 0 ? displayer.getModel().getTab(selIdx) : null;
        for (Iterator it = tabs.iterator(); it.hasNext(); ) {
            TabData tab = (TabData) it.next();
            String name;
            String htmlName;
            if (tab.getComponent() instanceof TopComponent) {
                TopComponent tabTC = (TopComponent) tab.getComponent();
                name = tabTC.getDisplayName();
                // #68291 fix - some hostile top components have null display name 
                if (name == null) {
                    name = tabTC.getName();
                }
                htmlName = tabTC.getHtmlDisplayName();
                if (htmlName == null) {
                    htmlName = name;
                }
            } else {
                name = htmlName = tab.getText();
            }
            items[i++] = new SwitcherTableItem(
                    new ActivatableTab(tab),
                    name,
                    htmlName,
                    tab.getIcon(),
                    tab == selectedTab);
        }
        return items;
    }
    
    private class ActivatableTab implements SwitcherTableItem.Activatable {
        private TabData tab;
        
        private ActivatableTab(TabData tab) {
            this.tab = tab;
        }
        
        public void activate() {
            if (tab != null) {
                selectTab(tab);
            }
        }
        
        /**
         * Maps tab selected in quicklist to tab index in displayer to select
         * correct tab
         */
        private void selectTab(TabData tab) {
            //Find corresponding index in displayer
            List tabs = displayer.getModel().getTabs();
            int ind = -1;
            for (int i = 0; i < tabs.size(); i++) {
                if (tab.equals(tabs.get(i))) {
                    ind = i;
                    break;
                }
            }
            if (ind != -1) {
                int old = displayer.getSelectionModel().getSelectedIndex();
                displayer.getSelectionModel().setSelectedIndex(ind);
                //#40665 fix start
                if (displayer.getType() == TabbedContainer.TYPE_EDITOR
                        && ind >= 0 && ind == old) {
                    displayer.getUI().makeTabVisible(ind);
                }
                //#40665 fix end
            }
        }
    }
}
