/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.*;

import org.openide.ErrorManager;
import org.openide.util.HelpCtx;
import org.openide.nodes.*;
import org.openide.util.actions.*;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteUtils;

/**
 * Preview design action.
 *
 * @author Tomas Pavek, Jan Stola
 */
public class TestAction extends CallableSystemAction implements Runnable {

    private static String name;

    public TestAction() {
        setEnabled(false);
    }

    protected boolean asynchronous() {
        return false;
    }

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(TestAction.class)
                     .getString("ACT_TestMode"); // NOI18N
        return name;
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.testing"); // NOI18N
    }

    /** @return resource for the action icon */
    protected String iconResource() {
        return "org/netbeans/modules/form/resources/test_form.png"; // NOI18N
    }

    public void performAction() {
        if (formDesigner != null) {
            selectedLaf = null;
            if (java.awt.EventQueue.isDispatchThread())
                run();
            else
                java.awt.EventQueue.invokeLater(this);
        }
    }

    public void run() {
        RADVisualComponent topComp = formDesigner.getTopDesignComponent();
        if (topComp == null)
            return;

        RADVisualComponent parent = topComp.getParentContainer();
        while (parent != null) {
            topComp = parent;
            parent = topComp.getParentContainer();
        }

        FormModel formModel = formDesigner.getFormModel();
        RADVisualFormContainer formContainer =
            topComp instanceof RADVisualFormContainer ?
                (RADVisualFormContainer) topComp : null;

        try {
            if (selectedLaf == null) {
                selectedLaf = UIManager.getLookAndFeel().getClass();
            }

            // create a copy of form
            final Frame frame = (Frame) FormDesigner.createFormView(topComp, selectedLaf);

            // set title
            String title = frame.getTitle();
            if (title == null || "".equals(title)) { // NOI18N
                title = topComp == formModel.getTopRADComponent() ?
                        formModel.getName() : topComp.getName();
                frame.setTitle(java.text.MessageFormat.format(
                    org.openide.util.NbBundle.getBundle(TestAction.class)
                                               .getString("FMT_TestingForm"), // NOI18N
                    new Object[] { title }
                ));
            }

            // prepare close operation
            if (frame instanceof JFrame) {
                ((JFrame)frame).setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                HelpCtx.setHelpIDString(((JFrame)frame).getRootPane(),
                                        "gui.modes"); // NOI18N
            }
            else {
                frame.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent evt) {
                        frame.dispose();
                    }
                });
            }
 
            // set size
            boolean shouldPack = false;
            if (formContainer != null
                && formContainer.getFormSizePolicy()
                                     == RADVisualFormContainer.GEN_BOUNDS
                && formContainer.getGenerateSize())
            {
                Dimension size = formContainer.getFormSize();
                if (frame.isUndecorated()) { // will be shown as decorated anyway
                    Dimension diffSize = formContainer.getDecoratedWindowContentDimensionDiff();
                    size = new Dimension(size.width + diffSize.width, size.height + diffSize.height);
                }
                frame.setSize(size);
            }
            else {
                shouldPack = true;
            }
            frame.setUndecorated(false);
            frame.setFocusableWindowState(true);

            // Issue 66594 and 12084
            final boolean pack = shouldPack;
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    if (pack) {
                        frame.pack();
                    }
                    frame.setBounds(org.openide.util.Utilities.findCenterBounds(frame.getSize()));
                    frame.setVisible(true);
                }
            });
        }
        catch (Exception ex) {
            org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
        }
    }

    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    public JMenuItem getPopupPresenter() {
        JMenu layoutMenu = new LAFMenu(getName());
        layoutMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(layoutMenu, SelectLayoutAction.class.getName());
        return layoutMenu;
    }

    // -------

    private FormDesigner formDesigner;

    public void setFormDesigner(FormDesigner designer) {
        formDesigner = designer;
        setEnabled(formDesigner != null && formDesigner.getTopDesignComponent() != null);
    }
    
    // LAFMenu

    private Class selectedLaf;
    
    private class LAFMenu extends JMenu implements ActionListener {
        private boolean initialized = false;

        private LAFMenu(String name) {
            super(name);
        }

        public JPopupMenu getPopupMenu() {
            JPopupMenu popup = super.getPopupMenu();
            JMenuItem mi;
            if (!initialized) {
                popup.removeAll();
                
                // Swing L&Fs
                UIManager.LookAndFeelInfo[] lafs = UIManager.getInstalledLookAndFeels();
                for (int i=0; i<lafs.length; i++) {
                    mi = new JMenuItem(lafs[i].getName());
                    mi.putClientProperty("lafInfo", new LookAndFeelItem(lafs[i].getClassName())); // NOI18N
                    mi.addActionListener(this);
                    popup.add(mi);
                }

                // L&Fs from the Palette
                Node[] cats = PaletteUtils.getCategoryNodes(PaletteUtils.getPaletteNode(), false);
                for (int i=0; i<cats.length; i++) {
                    if ("LookAndFeels".equals(cats[i].getName())) { // NOI18N
                        final Node lafNode = cats[i];
                        Node[] items = PaletteUtils.getItemNodes(lafNode, true);
                        if (items.length != 0) {
                            popup.add(new JSeparator());
                        }
                        for (int j=0; j<items.length; j++) {
                            PaletteItem pitem = (PaletteItem)items[j].getLookup().lookup(PaletteItem.class);
                            boolean supported = false;
                            try {
                                Class clazz = pitem.getComponentClass();
                                if ((clazz != null) && (LookAndFeel.class.isAssignableFrom(clazz))) {
                                    LookAndFeel laf = (LookAndFeel)clazz.newInstance();
                                    supported = laf.isSupportedLookAndFeel();
                                }
                            } catch (Exception ex) {
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                            } catch (LinkageError ex) {
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                            }
                            if (supported) {
                                mi = new JMenuItem(items[j].getDisplayName());
                                mi.putClientProperty("lafInfo", new LookAndFeelItem(pitem)); // NOI18N
                                mi.addActionListener(this);
                                popup.add(mi);
                            }
                        }
                    }
                }

                initialized = true;
            }
            return popup;
        }

        public void actionPerformed(ActionEvent e) {
            Object o = e.getSource();
            if (o instanceof JComponent) {
                JComponent source = (JComponent)o;
                LookAndFeelItem item = (LookAndFeelItem)source.getClientProperty("lafInfo"); // NOI18N
                try {
                    selectedLaf = item.getLAFClass();
                    run();
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                }
            }
        }

    }

    /**
     * Information about one look and feel.
     */
    static class LookAndFeelItem {
        /** Name of the look and feel's class. */
        private String className;
        /** The corresponding PaletteItem, if exists. */
        private PaletteItem pitem;

        public LookAndFeelItem(String className) {
            this.className = className;
        }

        public LookAndFeelItem(PaletteItem pitem) {
            this.pitem = pitem;
            this.className = pitem.getComponentClassName();
        }

        public String getClassName() {
            return className;
        }

        public Class getLAFClass() throws ClassNotFoundException {
            Class clazz;
            if (pitem == null) {
                if (className == null) {
                    clazz = UIManager.getLookAndFeel().getClass();
                } else {
                    clazz = Class.forName(className);
                }
            } else {
                clazz = pitem.getComponentClass();
            }
            return clazz;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof LookAndFeelItem)) return false;
            LookAndFeelItem item = (LookAndFeelItem)obj;
            return (pitem == item.pitem) && ((pitem != null)
                || ((className == null) ? (item.className == null) : className.equals(item.className)));
        }

        public int hashCode() {
            return (className == null) ? pitem.hashCode() : className.hashCode();
        }
        
    }

}
