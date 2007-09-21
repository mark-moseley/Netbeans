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

package org.netbeans.modules.vmd.api.inspector.common;

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import javax.swing.SwingUtilities;
import org.netbeans.modules.vmd.api.inspector.InspectorFolderPresenter;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.netbeans.modules.vmd.api.model.presenters.actions.ActionContext;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Karol Harezlak
 */

/**
 * This class provides GUI to change DesignComponent display name and DesignComponent's 
 * property which keeps information about DesignCompoent name. It needs to be attache to the DesignComponent
 * through ActionsPresenter.
 */ 
public final class RenameAction extends SystemAction implements ActionContext {
    
    public static final String DISPLAY_NAME = NbBundle.getMessage(RenameAction.class, "NAME_RenameAction"); //NOI18N
    
    private NotifyDescriptor.InputLine descriptor;
    private boolean canRename;
    private WeakReference<DesignComponent> component;
    
    
    public  void actionPerformed(ActionEvent e) {
        if (component == null)
            return;
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (component == null || component.get() == null)
                    throw new IllegalArgumentException("No DesignComponent attached to DeleteAction"); //NOI18N
                component.get().getDocument().getTransactionManager().writeAccess(new Runnable() {
                    public void run() {
                        InfoPresenter presenter = component.get().getPresenter(InfoPresenter.class);
                        if (presenter == null) {
                            Debug.warning("No necessary presenter for this operation - component: "+ component); //NOI18N
                            return;
                        }
                        getDialogDescriptor().setInputText( presenter.getEditableName() );
                        DialogDisplayer.getDefault().notify(getDialogDescriptor());
                        if (((Integer) descriptor.getValue()) == 0 && descriptor.getInputText().trim().length() > 0 ){
                            presenter.setEditableName(descriptor.getInputText().trim());
                        }
                    }
                });
            }
        });
    }
    
    private NotifyDescriptor.InputLine getDialogDescriptor(){
        if (descriptor != null)
            return descriptor;
        
        descriptor = new NotifyDescriptor.InputLine(NbBundle.getMessage(RenameAction.class,"TITLE_RenameQuestion"), NbBundle.getMessage(RenameAction.class,"TITLE_RenameDialog")); //NOI18N
        
        return descriptor;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public boolean isEnabled() {
        if (component == null)
            return false;
        
        component.get().getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                if (component.get().getDocument().getSelectedComponents().size() > 1) {
                    canRename = false;
                    return;
                }
                InspectorFolderPresenter presenter = component.get().getPresenter(InspectorFolderPresenter.class);
                if (presenter != null)
                    canRename = presenter.getFolder().canRename();
                else
                    canRename = false;
            }
        });
        
        return canRename;
    }
    
    public String getName() {
        return DISPLAY_NAME;
    }
    
    public void setComponent(DesignComponent component) {
        this.component = new WeakReference<DesignComponent>(component);
    }
    
}
