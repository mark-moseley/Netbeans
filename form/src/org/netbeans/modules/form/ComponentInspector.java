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

import java.util.*;
import java.awt.event.*;
import java.beans.*;
import java.awt.datatransfer.*;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;

import org.openide.*;
import org.openide.actions.PasteAction;
import org.openide.nodes.*;
import org.openide.explorer.*;
import org.openide.awt.UndoRedo;
import org.openide.explorer.view.BeanTreeView;
import org.openide.windows.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.ActionPerformer;
import org.openide.util.datatransfer.*;

import org.netbeans.modules.form.actions.TestAction;
import org.netbeans.modules.form.actions.ReloadAction;

/**
 * The ComponentInspector - special explorer for form editor.
 *
 * @author Tomas Pavek
 */

public class ComponentInspector extends TopComponent
                                implements ExplorerManager.Provider
{
    private ExplorerManager explorerManager;
    private Lookup lookup;

    private TestAction testAction = (TestAction)
                SystemAction.findObject(TestAction.class, true);
    private ReloadAction reloadAction = (ReloadAction)
                SystemAction.findObject(ReloadAction.class, true);

//    private DeleteAction deleteAction = (DeleteAction)
//                SystemAction.findObject(DeleteAction.class, true);
//    private CopyAction copyAction = (CopyAction)
//                SystemAction.findObject(CopyAction.class, true);
//    private CutAction cutAction = (CutAction)
//                SystemAction.findObject(CutAction.class, true);
    private PasteAction pasteAction = (PasteAction)
                SystemAction.findObject(PasteAction.class, true);

    private CopyCutActionPerformer copyActionPerformer = new CopyCutActionPerformer(true);
    private CopyCutActionPerformer cutActionPerformer = new CopyCutActionPerformer(false);
    private DeleteActionPerformer deleteActionPerformer = new DeleteActionPerformer();

//    private boolean actionsAttached = false;

    private ClipboardListener clipboardListener;

    /** Currently focused form or null if no form is opened/focused */
    private FormEditorSupport focusedForm;

    private boolean dontSynchronizeSelectedNodes = false;

    private EmptyInspectorNode emptyInspectorNode;

    /** Default icon base for control panel. */
    private static final String EMPTY_INSPECTOR_ICON_BASE =
        "org/netbeans/modules/form/resources/emptyInspector"; // NOI18N

    /** The icon for ComponentInspector */
    private static final String iconURL =
        "org/netbeans/modules/form/resources/inspector.gif"; // NOI18N

    /** The name "Component Inspector" */
//    private static String INSPECTOR_TITLE;

    private static ComponentInspector instance;

    // ------------
    // construction (ComponentInspector is a singleton)

    /** Gets default instance. Don't use directly, it reserved for '.settings' file only,
     * i.e. deserialization routines, otherwise you can get non-deserialized instance. */
    public static synchronized ComponentInspector getDefault() {
        if(instance == null) {
            instance = new ComponentInspector();
        }
        return instance;
    }

    /** Finds default instance. Use in client code instead of {@link #getDefault()}. */
    public static synchronized ComponentInspector getInstance() {
        if(instance == null) {
            TopComponent tc = WindowManager.getDefault().findTopComponent("ComponentInspector"); // NOI18N
            if(instance == null) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException(
                    "Can not find ComponentInspector component for its ID. Returned " + tc)); // NOI18N
                instance = new ComponentInspector();
            }
        }
        
        return instance;
    }

    private ComponentInspector() {
        explorerManager = new ExplorerManager();

        ActionMap map = getActionMap ();
        map.put(DefaultEditorKit.copyAction, copyActionPerformer);
        map.put(DefaultEditorKit.cutAction, cutActionPerformer);
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(explorerManager));
        map.put("delete", deleteActionPerformer); // NOI18N

        lookup = ExplorerUtils.createLookup(explorerManager, map);

        emptyInspectorNode = new EmptyInspectorNode();
        explorerManager.setRootContext(emptyInspectorNode);

        explorerManager.addPropertyChangeListener(new NodeSelectionListener());

        setLayout(new java.awt.BorderLayout());

        createComponents();

        setIcon(Utilities.loadImage(iconURL));
//        if (INSPECTOR_TITLE == null)
//            INSPECTOR_TITLE = FormUtils.getBundleString("CTL_InspectorTitle"); // NOI18N
//        setName(INSPECTOR_TITLE);
        setName(FormUtils.getBundleString("CTL_InspectorTitle")); // NOI18N
        setToolTipText(FormUtils.getBundleString("HINT_ComponentInspector")); // NOI18N
    }

    private void createComponents() {
        BeanTreeView treeView = new BeanTreeView();
        treeView.getAccessibleContext().setAccessibleName(
            FormUtils.getBundleString("ACS_ComponentTree")); // NOI18N
        treeView.getAccessibleContext().setAccessibleDescription(
            FormUtils.getBundleString("ACSD_ComponentTree")); // NOI18N
        add(java.awt.BorderLayout.CENTER, treeView);
    }

    // --------------
    // overriding superclasses, implementing interfaces

    // ExplorerManager.Provider
    public ExplorerManager getExplorerManager() {
        return explorerManager;
    }

    // Lookup.Provider from TopComponent
    public Lookup getLookup() {
        return lookup;
    }

    public UndoRedo getUndoRedo() {
        UndoRedo ur = focusedForm != null ?
                          focusedForm.getFormUndoRedoManager() : null;
        return ur != null ? ur : super.getUndoRedo();
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.component-inspector"); // NOI18N
    }

    /** Fixed preferred size (as the inherited preferred size is too big). */
//    public Dimension getPreferredSize() {
//        return new Dimension(250, 400);
//    }

    /** Replaces this in object stream. */
    public Object writeReplace() {
        return new ResolvableHelper();
    }

    protected void componentActivated() {
        attachActions();
    }

    protected void componentDeactivated() {
        detachActions();
    }

    // ------------
    // activating and focusing

    synchronized void attachActions() {
        ExplorerUtils.activateActions(explorerManager, true);
//        actionsAttached = true;
        updateActions();

        Clipboard c = getClipboard();
        if (c instanceof ExClipboard) {
            ExClipboard clip = (ExClipboard) c;
            if (clipboardListener == null)
                clipboardListener = new ClipboardChangesListener();
            clip.addClipboardListener(clipboardListener);
        }
    }

    synchronized void detachActions() {
        ExplorerUtils.activateActions(explorerManager, false);
//        actionsAttached = false;

        Clipboard c = getClipboard();
        if (c instanceof ExClipboard) {
            ExClipboard clip = (ExClipboard) c;
            clip.removeClipboardListener(clipboardListener);
        }

//        if (deleteActionPerformer == deleteAction.getActionPerformer())
//            deleteAction.setActionPerformer(null);
//        if (copyActionPerformer == copyAction.getActionPerformer()) {
//            copyAction.setActionPerformer(null);
//            pasteAction.setPasteTypes(null);
//        }
//        if (cutActionPerformer == cutAction.getActionPerformer())
//            cutAction.setActionPerformer(null);
    }

//    boolean getActionsAttached() {
//        return actionsAttached;
//    }

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
            reloadAction.setForm(null);

            // swing memory leak workaround
            removeAll();
            createComponents();

            getExplorerManager().setRootContext(emptyInspectorNode);
        }
        else {
            testAction.setFormModel(form.getFormModel());
            reloadAction.setForm(form);

            Node formNode = form.getFormRootNode();
            // XXX how can it be null?
            if (formNode == null) {
                System.err.println("Warning: FormEditorSupport.getFormRootNode() returns null"); // NOI18N
                getExplorerManager().setRootContext(emptyInspectorNode);
            }
            else {
                dontSynchronizeSelectedNodes = true;
                getExplorerManager().setRootContext(formNode);
                dontSynchronizeSelectedNodes = false;
            }
        }
//        setName(INSPECTOR_TITLE);

        if (visibility > 0)
            open();
        else if (visibility < 0)
            close();
    }

    // from ExplorerPanel
//    protected void updateTitle() {
//        setName(INSPECTOR_TITLE);
//    }

    public FormEditorSupport getFocusedForm() {
        return focusedForm;
    }

    void setSelectedNodes(Node[] nodes, FormEditorSupport form)
        throws PropertyVetoException
    {
        if (form == focusedForm) {
            dontSynchronizeSelectedNodes = true;
            getExplorerManager().setSelectedNodes(nodes);
            dontSynchronizeSelectedNodes = false;
        }
    }

    Node[] getSelectedNodes() {
        return getExplorerManager().getSelectedNodes();
    }

    // ---------------
    // actions

    private void updateActions() {
/*        boolean noPerformers = false;
        Node[] selected = getExplorerManager().getSelectedNodes();
        int n = selected != null ? selected.length : 0;
        int i;

        if (n > 0) {
            if (n > 1) {
                HashMap allNodes = new HashMap(101);
                for (i=0; i < n; i++)
                    if (!checkNodeParents(selected[i], allNodes)) {
                        noPerformers = true;
                        break;
                    }
            }

            if (!noPerformers) {
                for (i=0; i < n; i++)
                    if (!selected[i].canCopy()) {
                        copyAction.setActionPerformer(null);
                        break;
                    }
                if (i == n)
                    copyAction.setActionPerformer(copyActionPerformer);

                for (i=0; i < n; i++)
                    if (!selected[i].canCut()) {
                        cutAction.setActionPerformer(null);
                        break;
                    }
                if (i == n)
                    cutAction.setActionPerformer(cutActionPerformer);

                for (i=0; i < n; i++)
                    if (!selected[i].canDestroy()) {
                        deleteAction.setActionPerformer(null);
                        break;
                    }
                if (i == n)
                    deleteAction.setActionPerformer(deleteActionPerformer);
            }
        }
        else noPerformers = true; // no selected nodes

        if (noPerformers) {
            deleteAction.setActionPerformer(null);
            copyAction.setActionPerformer(null);
            cutAction.setActionPerformer(null);
        }
*/
        updatePasteAction();
    }

    private void updatePasteAction() {
        Node[] selected = getExplorerManager().getSelectedNodes();
        if (selected != null && selected.length == 1) {
            // exactly one node must be selected
            Clipboard clipboard = getClipboard();
            Transferable trans = clipboard.getContents(this); // [this??]
            if (trans != null) {
                Node node = selected[0];
                PasteType[] pasteTypes = node.getPasteTypes(trans);
                if (pasteTypes.length != 0) {
                    // transfer accepted by the node, we are done
                    pasteAction.setPasteTypes(pasteTypes);
                    return;
                }

                boolean multiFlavor = false;
                try {
                    multiFlavor = trans.isDataFlavorSupported(
                                    ExTransferable.multiFlavor);
                }
                catch (Exception e) {} // ignore, should not happen

                if (multiFlavor) {
                    // The node did not accept whole multitransfer as is - try
                    // to break it into individual transfers and paste them in
                    // sequence instead.
                    try {
                        MultiTransferObject mto = (MultiTransferObject)
                            trans.getTransferData(ExTransferable.multiFlavor);

                        int n = mto.getCount(), i;
                        Transferable[] t = new Transferable[n];
                        PasteType[] p = new PasteType[n];

                        for (i=0; i < n; i++) {
                            t[i] = mto.getTransferableAt(i);
                            pasteTypes = node.getPasteTypes(t[i]);
                            if (pasteTypes.length == 0)
                                break;

                            p[i] = pasteTypes[0]; // ??
                        }

                        if (i == n) { // all individual transfers accepted
                            pasteAction.setPasteTypes(
                                new PasteType[] { new MultiPasteType(t, p) });
                            return;
                        }
                    }
                    catch (Exception ex) {
                        org.openide.ErrorManager.getDefault().notify(org.openide.ErrorManager.INFORMATIONAL, ex);
                    }
                }
            }
        }

        pasteAction.setPasteTypes(null);
    }

    private boolean checkNodeParents(Node node, Map set) {
        if (set.get(node) != null)
            return false; // the node is in the set (as parent of another node)
        
        // 'this' means the original node (not parent)
        set.put(node, this);
 
        node = node.getParentNode();
        while (node != null) {
            if (set.put(node, node) == this)
                return false; // the parent is also a node already in the set
            node = node.getParentNode();
        }

        return true;
    }

    // -------------

    private Clipboard getClipboard() {
        Clipboard c = (java.awt.datatransfer.Clipboard)
            Lookup.getDefault().lookup(java.awt.datatransfer.Clipboard.class);
        if (c == null)
            c = java.awt.Toolkit.getDefaultToolkit().getSystemClipboard();
        return c;
    }

    // ---------------
    // innerclasses

    // listener on nodes selection (ExplorerManager)
    private class NodeSelectionListener implements PropertyChangeListener,
                                                   ActionListener
    {
        private javax.swing.Timer timer;

        NodeSelectionListener() {
            timer = new javax.swing.Timer(150, this);
            timer.setCoalesce(true);
            timer.setRepeats(false);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (!ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()))
                return;

            setActivatedNodes(getExplorerManager().getSelectedNodes());
            timer.start();
        }

        public void actionPerformed(ActionEvent evt) {
//            if (actionsAttached)
                updateActions();

            if (focusedForm == null)
                return;

            FormDesigner designer = focusedForm.getFormDesigner();
            if (designer == null)
                return;

            Node[] selected = getExplorerManager().getSelectedNodes();

            if (designer.getDesignerMode() == FormDesigner.MODE_CONNECT) {
                // handle connection mode
                if (selected.length == 0)
                    return;

                RADComponentCookie cookie = (RADComponentCookie)
                    selected[0].getCookie(RADComponentCookie.class);
                if (cookie != null
                    && cookie.getRADComponent() == designer.getConnectionSource()
                    && selected.length > 1)
                {
                    cookie = (RADComponentCookie)
                        selected[selected.length-1].getCookie(RADComponentCookie.class);
                }
                if (cookie != null)
                    designer.connectBean(cookie.getRADComponent(), true);
            }
            else if (!dontSynchronizeSelectedNodes) {
                // synchronize FormDesigner with ComponentInspector
                designer.clearSelectionImpl();

                for (int i=0; i < selected.length; i++) {
                    FormCookie formCookie = (FormCookie)
                        selected[i].getCookie(FormCookie.class);
                    if (formCookie != null) {
                        Node node = formCookie.getOriginalNode();
                        if (node instanceof RADComponentNode)
                            designer.addComponentToSelectionImpl(
                                ((RADComponentNode)node).getRADComponent());
                    }
                }

                designer.repaintSelection();
            }

            timer.stop();
        }
    }

    // listener on clipboard changes
    private class ClipboardChangesListener implements ClipboardListener {
        public void clipboardChanged(ClipboardEvent ev) {
            if (!ev.isConsumed())
                updatePasteAction();
        }
    }

    // performer for DeleteAction
    private class DeleteActionPerformer extends AbstractAction
                                        implements ActionPerformer, Mutex.Action
    {
        private Node[] nodesToDestroy;

        public void actionPerformed(ActionEvent e) {
            performAction(null);
        }

        public void performAction(SystemAction action) {
            Node[] selected = getExplorerManager().getSelectedNodes();
            if (selected == null || selected.length == 0)
                return;
            
            if (!confirmDelete(selected)) // ExplorerPanel.isConfirmDelete() ??
                return;

            try { // clear nodes selection first
                getExplorerManager().setSelectedNodes(new Node[0]);
            }
            catch (PropertyVetoException e) {} // cannot be vetoed

            nodesToDestroy = selected;
            if (java.awt.EventQueue.isDispatchThread())
                doDelete();
            else // reinvoke synchronously in AWT thread
                Mutex.EVENT.readAccess(this);
        }

        public Object run() {
            doDelete();
            return null;
        }

        private void doDelete() {
            if (nodesToDestroy != null) {
                for (int i=0; i < nodesToDestroy.length; i++) {
                    try {
                        nodesToDestroy[i].destroy();
                    }
                    catch (java.io.IOException ex) { // should not happen
                        ex.printStackTrace();
                    }
                }
                nodesToDestroy = null;
            }
        }

        private boolean confirmDelete(Node[] selected) {
            String message;
            String title;
            if (selected.length == 1) {
                message = NbBundle.getMessage(ExplorerUtils.class,
                                              "MSG_ConfirmDeleteObject", // NOI18N
                                              selected[0].getDisplayName());
                title = NbBundle.getMessage(ExplorerUtils.class,
                                            "MSG_ConfirmDeleteObjectTitle"); // NOI18N
            }
            else {
                message = NbBundle.getMessage(ExplorerUtils.class,
                                              "MSG_ConfirmDeleteObjects", // NOI18N
                                               new Integer(selected.length));
                title = NbBundle.getMessage(ExplorerUtils.class,
                                            "MSG_ConfirmDeleteObjectsTitle"); // NOI18N
            }

            NotifyDescriptor desc = new NotifyDescriptor.Confirmation(
                        message, title, NotifyDescriptor.YES_NO_OPTION);

            return NotifyDescriptor.YES_OPTION.equals (
                   DialogDisplayer.getDefault().notify(desc));
        }
    }

    // performer for CopyAction and CutAction
    private class CopyCutActionPerformer extends AbstractAction
                                         implements ActionPerformer
    {
        private boolean copy;

        public CopyCutActionPerformer(boolean copy) {
            this.copy = copy;
        }

        public void actionPerformed(ActionEvent e) {
            performAction(null);
        }

        public void performAction(SystemAction action) {
            Transferable trans;
            Node[] selected = getExplorerManager().getSelectedNodes();

            if (selected == null || selected.length == 0)
                trans = null;
            else if (selected.length == 1)
                trans = getTransferableOwner(selected[0]);
            else {
                Transferable[] transArray = new Transferable[selected.length];
                for (int i=0; i < selected.length; i++)
                    if ((transArray[i] = getTransferableOwner(selected[i]))
                                                                     == null)
                        return;

                trans = new ExTransferable.Multi(transArray);
            }

            if (trans != null) {
                Clipboard clipboard = getClipboard();
                clipboard.setContents(trans, new StringSelection("")); // NOI18N
            }
        }

        private Transferable getTransferableOwner(Node node) {
            try {
                return copy ? node.clipboardCopy() : node.clipboardCut();
            }
            catch (java.io.IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                return null;
            }
        }
    }

    // paste type used for ExTransferable.Multi
    private class MultiPasteType extends PasteType
                                 implements Mutex.ExceptionAction
    {
        private Transferable[] transIn;
        private PasteType[] pasteTypes;

        MultiPasteType(Transferable[] t, PasteType[] p) {
            transIn = t;
            pasteTypes = p;
        }

        // performs the paste action
        public Transferable paste() throws java.io.IOException {
            if (java.awt.EventQueue.isDispatchThread())
                return doPaste();
            else { // reinvoke synchronously in AWT thread
                try {
                    return (Transferable) Mutex.EVENT.readAccess(this);
                }
                catch (MutexException ex) {
                    Exception e = ex.getException();
                    if (e instanceof java.io.IOException)
                        throw (java.io.IOException) e;
                    else { // should not happen, ignore
                        e.printStackTrace();
                        return ExTransferable.EMPTY;
                    }
                }
            }
        }

        public Object run() throws Exception {
            return doPaste();
        }

        private Transferable doPaste() throws java.io.IOException {
            Transferable[] transOut = new Transferable[transIn.length];
            for (int i=0; i < pasteTypes.length; i++) {
                Transferable newTrans = pasteTypes[i].paste();
                transOut[i] = newTrans != null ? newTrans : transIn[i];
            }
            return new ExTransferable.Multi(transOut);
        }
    }

    // -----------

    // node for empty ComponentInspector
    private static class EmptyInspectorNode extends AbstractNode {
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
            return ComponentInspector.getDefault();
        }
    }
}
