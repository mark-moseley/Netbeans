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


package org.netbeans.modules.i18n;


import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.lang.ref.WeakReference;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.openide.cookies.SourceCookie;
import org.openide.nodes.Node;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.Workspace;


/**
 * Insert internationalized string at caret position (if it is not in guarded block).
 *
 * @author   Petr Jiricka
 */
public class InsertI18nStringAction extends CookieAction {

    /** Weak reference to top component to which the i18n string will be added. */
    private WeakReference topComponentWRef = new WeakReference(null);
    
    /** Generated serial version UID. */
    static final long serialVersionUID =-7002111874047983222L;

    
    /** 
     * Actually performs InsertI18nStringAction
     * @param activatedNodes currently activated nodes
     */
    public void performAction (final Node[] activatedNodes) {
        final SourceCookie.Editor sec = (SourceCookie.Editor)(activatedNodes[0]).getCookie(SourceCookie.Editor.class);
        if(sec == null)
            return;
        
        sec.open();

        // Set data object.
        DataObject dataObject = (DataObject)sec.getSource().getCookie(DataObject.class);
        if(dataObject == null)
            return; 

        JEditorPane[] panes = sec.getOpenedPanes();
        
        if(panes == null || panes.length == 0)
            return;
        
        // Set position. 
        int position = panes[0].getCaret().getDot();

        // Set document.
        StyledDocument document = sec.getDocument();
        if(document == null) {
            // Shouldn't happen.
            if(Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                TopManager.getDefault().notifyException(new InternalError("I18N: InsertI18nAction: Document not initialized.")); // NOI18N
            dataObject = null;
            return;
        }
        
        // If there is a i18n action in run on the same editor, cancel it.
        I18nManager.getDefault().cancel();

        addPanel(dataObject, document, position);

        // Ensure caret is visible.
        panes[0].getCaret().setVisible(true);;
    }

    /** Create panel used for specifying i18n string. */
    private JPanel createPanel(final DataObject dataObject, final StyledDocument document, final int position) {
        I18nSupport support = new JavaI18nSupport(dataObject, document);
        
        final I18nPanel i18nPanel = new I18nPanel(false, true);
        
        i18nPanel.setI18nString(support.getDefaultI18nString());
        
        JButton OKButton = new JButton(I18nUtil.getBundle().getString("CTL_OKButton"));
        JButton cancelButton = new JButton(I18nUtil.getBundle().getString("CTL_CancelButton"));

        JPanel buttonPanel = new JPanel(new GridBagLayout());

        // OK button.
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 0);
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.0;        
        buttonPanel.add(OKButton, gridBagConstraints); 

        // Cancel button.
        gridBagConstraints.gridx = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);        
        gridBagConstraints.weightx = 0.0;
        buttonPanel.add(cancelButton, gridBagConstraints);

        // Panel.
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.insets = new java.awt.Insets(17, 12, 11, 11);
        gridBagConstraints.weightx = 0.0;
        gridBagConstraints.weighty = 0.0;
        
        i18nPanel.add(buttonPanel, gridBagConstraints);

        // Set listeners for buttons.
        OKButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    // OK button.
                    try {
                        I18nString i18nString = i18nPanel.getI18nString();

                        if(i18nString.key == null) {
                            return;
                        }
                        
                        // Try to add key to bundle.                            
                        i18nString.addProperty(i18nString.getKey(), i18nString.getValue(), i18nString.getComment());

                        // Create field in necessary.
                        I18nUtil.createField((JavaI18nString)i18nString, dataObject);
                        // Replace string.
                        document.insertString(position, I18nUtil.getReplaceJavaCode((JavaI18nString)i18nString, dataObject), null);

                    } catch (IllegalStateException e) {
                        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                            I18nUtil.getBundle().getString("EXC_BadKey"),
                            NotifyDescriptor.ERROR_MESSAGE);
                        TopManager.getDefault().notify(msg);
                    } catch (BadLocationException e) {
                        TopManager.getDefault().notify(
                            new NotifyDescriptor.Message(
                                I18nUtil.getBundle().getString("MSG_CantInsertInGuarded"),
                                NotifyDescriptor.INFORMATION_MESSAGE
                            )
                        );
                    } finally { 
                        cancel();
                    }
                }
            }
        );

        cancelButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    cancel();
                }
            }
        );

        return i18nPanel;
    }
    
    /** Adds panel to top component in split pane. */
    private void addPanel(DataObject sourceDataObject, StyledDocument document, int position) {
        TopComponent topComponent = (TopComponent)topComponentWRef.get();

        if(topComponent == null) {
            JPanel panel = createPanel(sourceDataObject, document, position);
            
            // actually create the dialog as top component
            topComponent = new TopComponent() {
                public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
                }
                
                public void writeExternal(ObjectOutput out) throws IOException {
                }
                
                protected Object writeReplace() throws ObjectStreamException {
                    return null;
                }
            };            
            topComponent.setCloseOperation(TopComponent.CLOSE_EACH);
            topComponent.setLayout(new BorderLayout());
            topComponent.add(panel, BorderLayout.CENTER);
            topComponent.setName(sourceDataObject.getName());

            // dock into I18N mode if possible
            Workspace[] currentWs = TopManager.getDefault().getWindowManager().getWorkspaces();
            for (int i = currentWs.length; --i >= 0; ) {
                Mode i18nMode = currentWs[i].findMode(I18nManager.I18N_MODE);
                if (i18nMode == null) {
                    i18nMode = currentWs[i].createMode(
                        I18nManager.I18N_MODE,
                        I18nUtil.getBundle().getString("CTL_I18nDialogTitle"),
                        I18nManager.class.getResource("/org/netbeans/modules/i18n/i18nAction.gif") // NOI18N
                    );
                }
                i18nMode.dockInto(topComponent);
            }
            
            // Reset weak reference.
            topComponentWRef = new WeakReference(topComponent);
        }

        topComponent.open();
        topComponent.requestFocus();
    }
    
    /** Cancels the current insert i18n string action. */
    public void cancel() {
        TopComponent topComponent= (TopComponent)topComponentWRef.get();
        
        if(topComponent != null)
            topComponent.close();
    }
    
    /** Overrides superclass method.
     * @return true if action will be present 
     */
    protected boolean enable(Node[] activatedNodes) {
        if (!super.enable(activatedNodes))
            return false;
        
        // if has an open editor pane must not be in a guarded block
        // PENDING>>
        // It causes StackOverflowError
        // I18nSupport.isGuardedPosittion() checks teh way it causes change cookies (remove add SaveCookie), what
        // in turn calls back enable method, it calls isGueardedPosition again etc. etc.
        /*final SourceCookie.Editor sec = (SourceCookie.Editor)(activatedNodes[0]).getCookie(SourceCookie.Editor.class);        
        if (sec != null) {
            JEditorPane[] edits = sec.getOpenedPanes();
            if (edits != null && edits.length > 0) {
                int position = edits[0].getCaret().getDot();
                StyledDocument doc = sec.getDocument();
                DataObject obj = (DataObject)sec.getSource().getCookie(DataObject.class);
                if(I18nSupport.getI18nSupport(doc, obj).isGuardedPosition(position))
                    return false;
            }
        }*/
        // PENDING<<        
        
        return true;
    }

    /**
     * @return MODE_EXACTLY_ONE.
     */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /**
     * @return cookies needed to enable this action on appropriate node.
     */
    protected Class[] cookieClasses () {
        return new Class [] {
            SourceCookie.Editor.class
        };
    }

    /** 
     * @return the action's icon
     */
    public String getName() {
        return I18nUtil.getBundle().getString("CTL_InsertI18nString");
    }

    /** 
     * @return the action's help context 
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (InsertI18nStringAction.class);
    }

    /** Action's icon location.
     * @return the action's icon location
     */
    protected String iconResource () {
        return "/org/netbeans/modules/i18n/insertI18nStringAction.gif"; // NOI18N
    }
}