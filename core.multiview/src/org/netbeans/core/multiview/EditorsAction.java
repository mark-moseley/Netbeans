/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.multiview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
                    final MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
                    if (handler != null) {
                        ButtonGroup group = new ButtonGroup();
                        MultiViewPerspective[] pers = handler.getPerspectives();
                        for (int i = 0; i < pers.length; i++) {
                            final MultiViewPerspective thisPers = pers[i];
                            JRadioButtonMenuItem item = new JRadioButtonMenuItem(thisPers.getDisplayName());
                            item.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent event) {
                                    handler.requestActive(thisPers);
                                }
                            });
                            if (thisPers.getDisplayName().equals(handler.getSelectedPerspective().getDisplayName())) {
                                item.setSelected(true);
                            }
                            group.add(item);
                            add(item);
                        }
                    } else { // handler == null
                        JRadioButtonMenuItem but = new JRadioButtonMenuItem(NbBundle.getMessage(EditorsAction.class, "EditorsAction.source"));
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
