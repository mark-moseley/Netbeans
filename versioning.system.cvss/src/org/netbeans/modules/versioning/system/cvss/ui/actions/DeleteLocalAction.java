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

package org.netbeans.modules.versioning.system.cvss.ui.actions;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

/**
 * Delete action enabled only for new local files only.
 * It eliminates <tt>CVS/Entries</tt> scheduling if exists too.
 *
 * @author Petr Kuzel
 */
public final class DeleteLocalAction extends AbstractSystemAction {

    public static final int LOCALLY_DELETABLE_MASK = FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY;

    public void actionPerformed(ActionEvent ev) {
        File [] files = getContext().getFiles();
        int res = JOptionPane.showConfirmDialog(
                null,
                NbBundle.getMessage(DeleteLocalAction.class, "CTL_DeleteLocal_Prompt"),
                NbBundle.getMessage(DeleteLocalAction.class, "CTL_DeleteLocal_Title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (res != JOptionPane.YES_OPTION) return;
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            StandardAdminHandler entries = new StandardAdminHandler();
            FileObject fo = FileUtil.toFileObject(file);
            if (fo != null) {
                FileLock lock = null;
                try {
                    lock = fo.lock();
                    entries.removeEntry(file);
                    fo.delete(lock);
                } catch (IOException e) {
                    ErrorManager err = ErrorManager.getDefault();
                    err.annotate(e, "Can not lock " + file.toString());  // NOi18N
                    err.notify(e);
                } finally {
                    if (lock != null) {
                        lock.releaseLock();
                    }
                }
            }
        }
    }

    protected int getFileEnabledStatus() {
        return LOCALLY_DELETABLE_MASK;
    }

    protected String getBaseName() {
        return "Delete";  // NOI18M
    }
}
