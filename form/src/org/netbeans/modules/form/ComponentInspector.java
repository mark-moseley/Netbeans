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

package org.netbeans.modules.form;

import java.io.*;
import java.util.*;
import java.awt.*;
import java.beans.*;
import java.text.MessageFormat;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.explorer.*;
import org.openide.explorer.propertysheet.*;
import org.openide.awt.SplittedPanel;
import org.openide.explorer.view.BeanTreeView;
import org.openide.windows.*;
import org.openide.util.HelpCtx;
import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;

import org.netbeans.modules.form.actions.*;
import org.netbeans.modules.form.palette.*;

/**
 * The ComponentInspector explorer
 **/

public class ComponentInspector extends ExplorerPanel implements Serializable
{
    private static TestAction testAction = (TestAction)
                       SharedClassObject.findObject(TestAction.class, true);

    private static FormEditorAction inspectorAction =
        (FormEditorAction)
            SharedClassObject.findObject(FormEditorAction.class, true);

    /** The default width of the ComponentInspector */
    public static final int DEFAULT_INSPECTOR_WIDTH = 250;
    /** The default height of the ComponentInspector */
    public static final int DEFAULT_INSPECTOR_HEIGHT = 400;
    /** The default percents of the splitting of the ComponentInspector */
    public static final int DEFAULT_INSPECTOR_PERCENTS = 30;

    private static EmptyInspectorNode emptyInspectorNode;

    /** Default icon base for control panel. */
    private static final String EMPTY_INSPECTOR_ICON_BASE =
        "/org/netbeans/modules/form/resources/emptyInspector"; // NOI18N
    
    private static ResourceBundle formBundle = FormEditor.getFormBundle();
    
    /** Currently focused form or null if no form is opened/focused */
    private FormEditorSupport focusedForm;
//    private FormModel formModel;
    private boolean focusingOnForm = false;

    private SplittedPanel split;
    private PropertySheetView sheet;

    /** The icon for ComponentInspector */
    private static String iconURL = "/org/netbeans/modules/form/resources/inspector.gif"; // NOI18N

    static final long serialVersionUID =4248268998485315927L;

    private static ComponentInspector instance;

    public static ComponentInspector getInstance() {
        if (instance == null)
            instance = new ComponentInspector();
        return instance;
    }
    
    private ComponentInspector() {
        ExplorerManager manager = getExplorerManager();
        emptyInspectorNode = new EmptyInspectorNode();
        manager.setRootContext(emptyInspectorNode);
        
        setLayout(new BorderLayout());
        
        createSplit();

        setIcon(Utilities.loadImage(iconURL));
        setName(formBundle.getString("CTL_InspectorTitle"));

        manager.addPropertyChangeListener(new NodeSelectedListener());
    }

    private void createSplit() {
        split = new SplittedPanel();
        split.add(new BeanTreeView(), SplittedPanel.ADD_FIRST);
        split.add(sheet = new PropertySheetView(), SplittedPanel.ADD_SECOND);
        split.setSplitType(SplittedPanel.VERTICAL);
        split.setSplitPosition(DEFAULT_INSPECTOR_PERCENTS);

        sheet.setDisplayWritableOnly(
            FormEditor.getFormSettings().getDisplayWritableOnly());
        sheet.addPropertyChangeListener(new PropertiesDisplayListener());

        add(BorderLayout.CENTER, split);
    }

    class NodeSelectedListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (!ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()))
                return;
            if (focusedForm == null)
                return;
            
            FormDesigner designer = focusedForm.getFormDesigner();
            if (designer == null)
                return;
                        
            Node[] selectedNodes = getExplorerManager().getSelectedNodes();

            if (CPManager.getDefault().getMode() ==
                PaletteAction.MODE_CONNECTION) {
                if (selectedNodes.length < 1)
                    return;
                
                RADComponentCookie cookie =
                    (RADComponentCookie) selectedNodes[0]
                    .getCookie(RADComponentCookie.class);
                
                try {
                    getExplorerManager().setSelectedNodes(new Node[0]);
                }
                catch (PropertyVetoException ex) {}
                
                if (cookie != null)
                    designer.connectBean(cookie.getRADComponent(), true);
            }
            else if (!focusingOnForm) {
                designer.clearSelectionImpl();
                
                for (int i = 0; i < selectedNodes.length; i++) {
                    RADComponentCookie cookie =
                        (RADComponentCookie) selectedNodes[i].getCookie(RADComponentCookie.class);
                    if (cookie != null) {
                        designer.addComponentToSelectionImpl(
                            cookie.getRADComponent());
                    }
                }
            }
        }
    }

    class PropertiesDisplayListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (PropertySheet.PROPERTY_DISPLAY_WRITABLE_ONLY.equals(
                                              evt.getPropertyName()))
            {
                FormEditor.getFormSettings().setDisplayWritableOnly(
                                               sheet.getDisplayWritableOnly());
            }
        }
    }

    public void open(Workspace workspace) {
        // extension of this method is not needed now
        super.open(workspace);
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.component-inspector"); // NOI18N
    }

    /** This method focuses the ComponentInspector on given form.
     * @param form the form to focus on
     */
    public void focusForm(final FormEditorSupport form) {
        if (focusedForm != form)
            focusFormInAwtThread(form, 0);
    }

    /** This method focuses the ComponentInspector on given form.
     * @param form the form to focus on
     * @param visible true to open inspector, false to close
     */
    public void focusForm(final FormEditorSupport form, boolean visible) {
        if (focusedForm != form)
            focusFormInAwtThread(form, visible ? 1 : -1);
    }

    private void focusFormInAwtThread(final FormEditorSupport form,
                                      final int visibility) {
        if (java.awt.EventQueue.isDispatchThread()) {
            focusFormImpl(form, visibility);
        }
        else {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    focusFormImpl(form, visibility);
                }
            });
        }
    }

    private void focusFormImpl(FormEditorSupport form, int visibility) {
        focusedForm = form;

        if (form == null) {
            testAction.setFormModel(null);
            inspectorAction.setEnabled(false);

            // swing memory leak workaround
            remove(split);
            createSplit();

            getExplorerManager().setRootContext(emptyInspectorNode);
        }
        else {
            testAction.setFormModel(form.getFormModel());
            inspectorAction.setEnabled(true);

            Node formNode = form.getFormRootNode();
            // XXX how can it be null?
            if (formNode == null) {
                System.err.println("Warning: FormEditorSupport.getFormRootNode() returns null");
                getExplorerManager().setRootContext(emptyInspectorNode);
            }
            else {
                sheet.setDisplayWritableOnly(!form.getFormModel().isReadOnly()
                     && FormEditor.getFormSettings().getDisplayWritableOnly());

                focusingOnForm = true;
                getExplorerManager().setRootContext(formNode);
                focusingOnForm = false;
            }
        }
        updateTitle();

        if (visibility > 0)
            open();
        else if (visibility < 0) {
            setCloseOperation(TopComponent.CLOSE_EACH);
            close();
        }
    }

    protected void updateTitle() {
        setName(formBundle.getString("CTL_InspectorTitle"));
    }

    FormEditorSupport getFocusedForm() {
        return focusedForm;
    }

    void setSelectedNodes(Node[] nodes, FormEditorSupport form)
    throws PropertyVetoException
    {
        if (form == focusedForm) {
            getExplorerManager().setSelectedNodes(nodes);
        }
    }

    Node[] getSelectedNodes() {
        return getExplorerManager().getSelectedNodes();
    }

    /** Fixed preferred size, so as the inherited preferred size is too big */
    public Dimension getPreferredSize() {
        return new Dimension(DEFAULT_INSPECTOR_WIDTH, DEFAULT_INSPECTOR_HEIGHT);
    }

    /** replaces this in object stream */
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    static class EmptyInspectorNode extends AbstractNode {
        public EmptyInspectorNode() {
            super(Children.LEAF);
            setIconBase(EMPTY_INSPECTOR_ICON_BASE);
        }

        public boolean canRename() {
            return false;
        }
    }

    final public static class ResolvableHelper implements java.io.Serializable {
        static final long serialVersionUID = 7424646018839457544L;
        public Object readResolve() {
            return ComponentInspector.getInstance();
        }
    }
}
