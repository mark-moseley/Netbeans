/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning.system.cvss.ui.history;

import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.system.cvss.VersionsCache;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.FileStatusCache;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import javax.swing.*;
import java.io.*;
import java.awt.event.ActionEvent;
import java.awt.Dialog;

/**
 * Allows to view a specific revision of the given file.
 * 
 * @author Maros Sandor
 */
public class ViewRevisionAction extends AbstractAction implements Runnable {
    
    private final VCSContext ctx;
    private ViewRevisionPanel settings; 

    public ViewRevisionAction(VCSContext ctx) {
        this(Utils.getActionName(ViewRevisionAction.class, "CTL_MenuItem_ViewRevision", ctx), ctx);
    }

    public ViewRevisionAction(String name, VCSContext ctx) {
        super(name);
        this.ctx = ctx;
    }

    public boolean isEnabled() {
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        for (File file : ctx.getRootFiles()) {
            if (file.isDirectory()) continue;
            if ((cache.getStatus(file).getStatus() & FileInformation.STATUS_IN_REPOSITORY) != 0) return true;
        }
        return false;
    }

    public void actionPerformed(ActionEvent e) {

        String title = NbBundle.getMessage(ViewRevisionAction.class, "CTL_ViewRevisionDialog_Title", Utils.getContextDisplayName(ctx)); // NOI18N
        
        settings = new ViewRevisionPanel(ctx); 
        
        JButton view = new JButton(NbBundle.getMessage(ViewRevisionAction.class, "CTL_ViewRevisionDialog_Action_View")); // NOI18N
        settings.putClientProperty("OKButton", view); // NOI18N
        settings.refreshComponents();
        view.setToolTipText(NbBundle.getMessage(ViewRevisionAction.class,  "TT_ViewRevisionDialog_Action_View")); // NOI18N
        DialogDescriptor descriptor = new DialogDescriptor(
                settings,
                title,
                true,
                new Object [] { view, DialogDescriptor.CANCEL_OPTION },
                view,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(ViewRevisionAction.class),
                null);
        descriptor.setClosingOptions(null);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ViewRevisionAction.class, "ACSD_ViewRevisionDialog")); // NOI18N
        dialog.setVisible(true);
        if (descriptor.getValue() != view) return;

        settings.saveSettings();
        Utils.createTask(this).schedule(0);
    }

    public void run() {
        final String revision = settings.getRevision();
        File tempFolder = Utils.getTempFolder();
        for (File file : ctx.getRootFiles()) {
            if (file.isDirectory()) continue;
            try {
                view(file, revision, tempFolder);
            } catch (Exception e) {
                // the file cannot be opened, ignore
            }
        }
    }

    /**
     * Open a file in the given revision in editor.
     * 
     * @param base base file
     * @param revision revision to open 
     * @param tempFolder temporary folder to use, it can be null bu this is not recommended if you will be calling this on multiple files in a row
     * @throws Exception if something goes wrong
     */
    public static void view(File base, final String revision, File tempFolder) throws Exception {
        if (tempFolder == null) tempFolder = Utils.getTempFolder();
        File original = VersionsCache.getInstance().getRemoteFile(base, revision, null);
        File daoFile = new File(tempFolder, base.getName());
        daoFile.deleteOnExit();
        Utils.copyStreamsCloseAll(new FileOutputStream(daoFile), new FileInputStream(original)); 
        final FileObject fo = FileUtil.toFileObject(daoFile);
        DataObject dao = DataObject.find(fo);
        EditorCookie ec = dao.getCookie(EditorCookie.class);
        if (ec != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    Utils.openFile(fo, revision);
                }
            });
        }
    }
}
