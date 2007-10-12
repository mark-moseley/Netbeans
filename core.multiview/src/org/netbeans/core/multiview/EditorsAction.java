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

package org.netbeans.core.multiview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action to show in main menu to switch active view.
 * @author mkleint
 */
public class EditorsAction extends AbstractAction 
                                implements Presenter.Menu {
                                    
    public EditorsAction() {
        super(NbBundle.getMessage(EditorsAction.class, "CTL_EditorsAction"));
    }
    
    public void actionPerformed(ActionEvent ev) {
        assert false;// no operation
    }
    
    public JMenuItem getMenuPresenter() {
        JMenu menu = new UpdatingMenu();
        String label = NbBundle.getMessage(EditorsAction.class, "CTL_EditorsAction");
        Mnemonics.setLocalizedText(menu, label);
        return menu;
    }
    
    private static final class UpdatingMenu extends JMenu implements DynamicMenuContent {
        
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return getMenuPresenters();
        }

        public JComponent[] getMenuPresenters() {
            Mode mode = WindowManager.getDefault().findMode(CloneableEditorSupport.EDITOR_MODE);
            removeAll();
            if (mode != null) {
                TopComponent tc = mode.getSelectedTopComponent();
                if (tc != null) {
                    setEnabled(true);
                    MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
                    if (handler != null) {
                        final WeakReference<MultiViewHandler> handlerRef = new WeakReference<MultiViewHandler>(handler);
                        ButtonGroup group = new ButtonGroup();
                        MultiViewPerspective[] pers = handler.getPerspectives();
                        for (int i = 0; i < pers.length; i++) {
                            MultiViewPerspective thisPers = pers[i];
                            final WeakReference<MultiViewPerspective> persRef = new WeakReference<MultiViewPerspective>(thisPers);
                            
                            JRadioButtonMenuItem item = new JRadioButtonMenuItem();
                            Mnemonics.setLocalizedText(item, thisPers.getDisplayName());
                            item.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent event) {
                                    //#88626 prevent a memory leak
                                    MultiViewHandler handler = handlerRef.get();
                                    MultiViewPerspective thisPers = persRef.get();
                                    if (handler != null && thisPers != null) {
                                        handler.requestActive(thisPers);
                                    }
                                }
                            });
                            if (thisPers.getDisplayName().equals(handler.getSelectedPerspective().getDisplayName())) {
                                item.setSelected(true);
                            }
                            group.add(item);
                            add(item);
                        }
                    } else { // handler == null
                        JRadioButtonMenuItem but = new JRadioButtonMenuItem();
                        Mnemonics.setLocalizedText(but, NbBundle.getMessage(EditorsAction.class, "EditorsAction.source"));
                        but.setSelected(true);
                        add(but);
                    }
                } else { // tc == null
                    setEnabled(false);
                }
            } else { // mode == null
                setEnabled(false);
            }
            return new JComponent[] {this};
        }
        
    }
    
}
