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

package org.netbeans.modules.versioning.system.cvss.ui.actions.diff;

import org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.netbeans.modules.versioning.system.cvss.*;
import org.netbeans.modules.versioning.system.cvss.util.Context;

import javax.swing.*;
import java.awt.Component;
import java.awt.BorderLayout;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

/**
 * Performs diff action by fetching the appropriate file from repository
 * and shows the diff. It actually does not execute "cvs diff" but rather uses our
 * own diff algorithm. 
 * 
 * @author Maros Sandor
 */
public class DiffExecutor {

    private final Context   context;
    private final String    contextName;

    public DiffExecutor(Context context, String contextName) {
        this.context = context;
        this.contextName = contextName;
    }

    public DiffExecutor(String contextName) {
        this.contextName = contextName;
        this.context = null;
    }

    /**
     * Opens GUI component that shows differences between current base revisions
     * and HEAD revisions.
     */ 
    public void showRemoteDiff() {
        showDiff(Setup.DIFFTYPE_REMOTE);
    }
    
    /**
     * Opens GUI component that shows differences between current working files
     * and HEAD revisions.
     */ 
    public void showAllDiff() {
        showDiff(Setup.DIFFTYPE_ALL);
    }
    
    /**
     * Opens GUI component that shows differences between current working files
     * and repository versions they are based on.
     */ 
    public void showLocalDiff() {
        showDiff(Setup.DIFFTYPE_LOCAL);
    }

    public void showDiff(File file, String rev1, String rev2) {
        DiffMainPanel panel = new DiffMainPanel(file, rev1, rev2);
        openDiff(panel);
    }

    private void showDiff(int type) {
        VersionsCache.getInstance().purgeVolatileRevisions();
        DiffMainPanel panel = new DiffMainPanel(context, type, contextName);
        openDiff(panel);        
    }
    
    private void openDiff(final Component c) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DiffTopComponent tc = new DiffTopComponent(c);
                tc.setName(NbBundle.getMessage(DiffExecutor.class, "CTL_DiffPanel_Title", contextName));
                tc.open();
                tc.requestActive();
            }
        });
    }

    /**
     * Utility method that returns all non-excluded modified files that are
     * under given roots (folders) and have one of specified statuses.
     *
     * @param context context to search
     * @param includeStatus bit mask of file statuses to include in result
     * @return File [] array of Files having specified status
     */
    public static File [] getModifiedFiles(Context context, int includeStatus) {
        CvsFileTableModel model = CvsVersioningSystem.getInstance().getFileTableModel(context, includeStatus);
        CvsFileNode [] nodes = model.getNodes();
        List files = new ArrayList();
        for (int i = 0; i < nodes.length; i++) {
            File file = nodes[i].getFile();
            String path = file.getAbsolutePath();
            if (CvsModuleConfig.getDefault().isExcludedFromCommit(path) == false) {
                files.add(file);
            }
        }
        return (File[]) files.toArray(new File[files.size()]);
    }

    private static class DiffTopComponent extends TopComponent {

        public DiffTopComponent() {
        }
        
        public DiffTopComponent(Component c) {
            setLayout(new BorderLayout());
            add(c, BorderLayout.CENTER);            
            getAccessibleContext().setAccessibleName(NbBundle.getMessage(DiffTopComponent.class, "ACSN_Diff_Top_Component")); // NOI18N
            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DiffTopComponent.class, "ACSD_Diff_Top_Component")); // NOI18N
        }
        
        public int getPersistenceType(){
            return TopComponent.PERSISTENCE_NEVER;
        }

        protected void componentClosed() {
            ((DiffMainPanel) getComponent(0)).componentClosed();
            super.componentClosed();
        }

        protected String preferredID(){
            return "DiffExecutorTopComponent";    //NOI18N       
        }
        
        public HelpCtx getHelpCtx() {
            return new HelpCtx(getClass());
        }

    }
    
}
