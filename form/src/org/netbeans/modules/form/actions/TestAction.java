/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.actions;

import javax.swing.*;
import java.awt.*;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.*;
import org.openide.util.actions.*;
import org.netbeans.modules.form.*;

/** TestAction action.
 *
 * @author   Ian Formanek
 */
public class TestAction extends CallableSystemAction {

    /** The help context of this action */
    private static HelpCtx help;

    static final long serialVersionUID =6405790716032972989L;

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return NbBundle.getBundle(TestAction.class).getString("ACT_TestMode"); // NOI18N
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(TestAction.class);
    }

    /** @return resource for the action icon */
    protected String iconResource() {
        return "/org/netbeans/modules/form/resources/testMode.gif"; // NOI18N
    }

    public void performAction() {
        if (formModel != null) {
            if (java.awt.EventQueue.isDispatchThread())
                testForm();
            else
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        testForm();
                    }
                });
        }
    }

    private void testForm() {
        RADComponent topComp = formModel.getTopRADComponent();
        if (!(topComp instanceof RADVisualFormContainer)) return;
        RADVisualFormContainer formContainer = (RADVisualFormContainer) topComp;

        // a JFrame or Frame will be used (depending on form is Swing or AWT)
        Object formInstance = formContainer.getBeanInstance();
        Class frameClass = formInstance instanceof JComponent
                               || formInstance instanceof JFrame
                               || formInstance instanceof JDialog
                               ||  formInstance instanceof JApplet ?
                           JFrame.class : Frame.class;
        try {
            // create a copy of form
            Frame frame = (Frame)
                FormDesigner.createContainerView(formContainer, frameClass);

            // add menu bar with menus
            String menuName = formContainer.getFormMenu();
            if (menuName != null) {
                RADMenuComponent menuBarComp = null;
                java.util.List menus = formContainer.getAvailableMenus();
                for (java.util.Iterator it=menus.iterator(); it.hasNext(); ) {
                    RADComponent metacomp = (RADComponent) it.next();
                    if (menuName.equals(metacomp.getName())) {
                        menuBarComp = (RADMenuComponent) metacomp;
                        break;
                    }
                }
                if (menuBarComp != null) { // there's a valid menu bar - clone it
                    Object menuBar = menuBarComp.cloneMenuInstance();
                    if (frame instanceof JFrame) {
                        if (menuBar instanceof JMenuBar)
                            ((JFrame)frame).setJMenuBar((JMenuBar)menuBar);
                    }
                    else if (menuBar instanceof MenuBar)
                        frame.setMenuBar((MenuBar)menuBar);
                }
            }

            // set title
            String title = frame.getTitle();
            if (title == null || "".equals(title))
                frame.setTitle(java.text.MessageFormat.format(
                    NbBundle.getBundle(TestAction.class).getString("FMT_TestingForm"), // NOI18N
                    new Object[] { formModel.getFormDataObject().getName() }
                ));

            // prepare close operation
            if (frame instanceof JFrame)
                ((JFrame)frame).setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            else {
                final Frame showingFrame = frame;
                frame.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent evt) {
                        showingFrame.dispose();
                    }
                });
            }
 
            // set size
            if (formContainer.getFormSizePolicy() == RADVisualFormContainer.GEN_BOUNDS
                    && formContainer.getGenerateSize())
                frame.setSize(formContainer.getFormSize());
            else
                frame.pack();

            // set location
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension frameSize = frame.getSize();
            frame.setLocation((screenSize.width - frameSize.width) / 2,
                              (screenSize.height - frameSize.height) / 2);
            // show it
            frame.show();
        }
        catch (Exception ex) {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
        }
    }

    // -------

    public void setFormModel(FormModel model) {
        formModel = model;
        setEnabled(formModel != null);
    }

    private FormModel formModel;
}
