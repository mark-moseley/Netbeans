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

package org.netbeans.modules.versioning.system.cvss.ui.actions.log;

import org.netbeans.modules.versioning.system.cvss.ui.actions.AbstractSystemAction;
import org.netbeans.modules.versioning.system.cvss.ui.history.SearchHistoryTopComponent;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.*;

/**
 * Search History action.
 *
 * @author Maros Sandor
 */
public class SearchHistoryAction extends AbstractSystemAction  {

    protected String getBaseName() {
        return "CTL_MenuItem_SearchHistory";
    }

    protected int getFileEnabledStatus() {
        return FileInformation.STATUS_IN_REPOSITORY;
    }

    public void actionPerformed(ActionEvent ev) {
        String title = NbBundle.getMessage(SearchHistoryAction.class, "CTL_SearchHistory_Title", getContextDisplayName());
        openHistory(getFilesToProcess(), title);
    }

    private void openHistory(final File [] roots, final String title) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SearchHistoryTopComponent tc = new SearchHistoryTopComponent(roots);
                tc.setName(title);
                tc.open();
                tc.requestActive();
            }
        });
    }

    /**
     * Opens the Seach History panel with given pre-filled values. The search is executed in default context
     * (all open projects). 
     * 
     * @param title title of the search
     * @param commitMessage commit message to search for
     * @param username user name to search for
     * @param date date of the change in question
     */ 
    public static void openSearch(String title, String commitMessage, String username, Date date) {
        openSearch(getDefaultContext(), title, commitMessage, username, date);
    }
    
    private static void openSearch(File [] context, String title, String commitMessage, String username, Date date) {
        Date from = date;
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, 1);
        Date to = c.getTime();
        
        SearchHistoryTopComponent tc = new SearchHistoryTopComponent(context, commitMessage, username, from, to);
        String tcTitle = NbBundle.getMessage(SearchHistoryAction.class, "CTL_SearchHistory_Title", title);
        tc.setName(tcTitle);
        tc.open();
        tc.requestActive();
        tc.search();
    }

    private static File[] getDefaultContext() {
        Project [] projects = OpenProjects.getDefault().getOpenProjects();
        List list = Utils.getProjectsSources(projects);
        return (File[]) list.toArray(new File[list.size()]);
    }
}
