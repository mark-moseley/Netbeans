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

package org.netbeans.modules.subversion.ui.diff;

import org.netbeans.api.diff.StreamSource;
import org.netbeans.api.diff.DiffController;
import org.netbeans.modules.subversion.*;
import org.netbeans.modules.subversion.client.PropertiesClient;
import org.openide.util.NbBundle;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.text.MessageFormat;

/**
 * Represents on DIFF setup.
 *
 * @author Maros Sandor
 */
public final class Setup {

    /**
     * What was locally changed? The right pane contains local file.
     *
     * <p>Local addition, removal or change is displayed in
     * the right pane as addition, removal or change respectively
     * (i.e. not reversed as removal, addition or change).
     *
     * <pre>
     * diff from-BASE to-LOCAL
     * </pre>
     */
    public static final int DIFFTYPE_LOCAL     = 0;

    /**
     * What was remotely changed? The right pane contains remote file.
     *
     * <p>Remote addition, removal or change is displayed in
     * the right pane as addition, removal or change respectively
     * (i.e. not reversed as removal, addition or change).
     *
     * <pre>
     * diff from-BASE to-HEAD
     * </pre>
     */
    public static final int DIFFTYPE_REMOTE    = 1;

    /**
     * What was locally changed comparing to recent head?
     * The Right pane contains local file.
     *
     * <p> Local addition, removal or change is displayed in
     * the right pane as addition, removal or change respectively
     * (i.e. not reversed as removal, addition or change).
     *
     * <pre>
     * diff from-HEAD to-LOCAL
     * </pre>
     */
    public static final int DIFFTYPE_ALL       = 2;
    
    public static final String REVISION_PRISTINE = "PRISTINE"; // NOI18N
    public static final String REVISION_BASE = "BASE"; // NOI18N
    public static final String REVISION_CURRENT = "LOCAL"; // NOI18N
    public static final String REVISION_HEAD    = "HEAD"; // NOI18N
    
    private final File      baseFile;
    
    /**
     * Name of the file's property if the setup represents a property diff setup, null otherwise. 
     */
    private final String    propertyName;
    
    private String    firstRevision;
    private final String    secondRevision;
    private FileInformation info;

    private DiffStreamSource    firstSource;
    private DiffStreamSource    secondSource;

    private DiffController      view;
    private DiffNode            node;

    private String    title;

    public Setup(File baseFile, String propertyName, int type) {
        this.baseFile = baseFile;
        this.propertyName = propertyName;
        info = Subversion.getInstance().getStatusCache().getStatus(baseFile);
        int status = info.getStatus();
        
        ResourceBundle loc = NbBundle.getBundle(Setup.class);
        String firstTitle;
        String secondTitle;

        // the first source

        switch (type) {
            case DIFFTYPE_LOCAL:           
            case DIFFTYPE_REMOTE:

                // from-BASE

                if (match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY
                | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
                    firstRevision = REVISION_BASE;
                    firstTitle = loc.getString("MSG_DiffPanel_LocalNew");
                } else if (match (status, FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
                    firstRevision = null;
                    firstTitle = NbBundle.getMessage(Setup.class, "LBL_Diff_NoLocalFile"); // NOI18N
                } else if (match(status, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY
                | FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
                    firstRevision = REVISION_BASE;
                    firstTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_BaseRevision"), new Object [] { firstRevision });
                } else {
                    firstRevision = REVISION_BASE;
                    firstTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_BaseRevision"), new Object [] { firstRevision });
                }

                break;

            case DIFFTYPE_ALL:

                // from-HEAD

                if (match (status, FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
                    firstRevision = REVISION_HEAD;
                    firstTitle = loc.getString("MSG_DiffPanel_RemoteNew");
                } else if (match (status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY
                                 | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
                    firstRevision = null;
                    firstTitle = loc.getString("MSG_DiffPanel_NoBaseRevision");
                } else if (match(status, FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
                    firstRevision = null;
                    firstTitle = loc.getString("MSG_DiffPanel_RemoteDeleted");
                } else {
                    firstRevision = REVISION_HEAD;
                    firstTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_RemoteModified"), new Object [] { firstRevision });
                }
                break;

            default:
                throw new IllegalArgumentException("Unknow diff type: " + type); // NOI18N
        }


        // the second source

        switch (type) {
            case DIFFTYPE_LOCAL:
            case DIFFTYPE_ALL:

                // to-LOCAL

                if (match(status, FileInformation.STATUS_VERSIONED_CONFLICT)) {
                    secondRevision = REVISION_CURRENT;
                    secondTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_LocalConflict"), new Object [] { secondRevision });
                } else if (match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY
                | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
                    secondRevision = REVISION_CURRENT;
                    secondTitle = loc.getString("MSG_DiffPanel_LocalNew");
                } else if (match (status, FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
                    secondRevision = null;
                    secondTitle = NbBundle.getMessage(Setup.class, "LBL_Diff_NoLocalFile"); // NOI18N
                } else if (match(status, FileInformation.STATUS_VERSIONED_DELETEDLOCALLY
                | FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY)) {
                    secondRevision = null;
                    secondTitle = loc.getString("MSG_DiffPanel_LocalDeleted");
                } else {
                    secondRevision = REVISION_CURRENT;
                    secondTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_LocalModified"), new Object [] { secondRevision });
                }
                break;

            case DIFFTYPE_REMOTE:

                // to-HEAD

                if (match(status, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY
                | FileInformation.STATUS_VERSIONED_ADDEDLOCALLY)) {
                    secondRevision = null;
                    secondTitle = loc.getString("MSG_DiffPanel_LocalNew");
                } else if (match(status, FileInformation.STATUS_VERSIONED_NEWINREPOSITORY)) {
                    secondRevision = REVISION_HEAD;
                    secondTitle = loc.getString("MSG_DiffPanel_RemoteNew");
                } else if (match(status, FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY)) {
                    secondRevision = null;
                    secondTitle = loc.getString("MSG_DiffPanel_RemoteDeleted");
                } else {
                    secondRevision = REVISION_HEAD;
                    secondTitle = MessageFormat.format(loc.getString("MSG_DiffPanel_RemoteModified"), new Object [] { secondRevision });
                }            
                break;

            default:
                throw new IllegalArgumentException("Unknow diff type: " + type); // NOI18N
        }
        
        if (propertyName != null){
            if (REVISION_HEAD.equals(firstRevision)) {
                firstRevision = REVISION_BASE;
            }
        }

        firstSource = new DiffStreamSource(baseFile, propertyName, firstRevision, firstTitle);
        secondSource = new DiffStreamSource(baseFile, propertyName, secondRevision, secondTitle);
        title = "<html>" + Subversion.getInstance().getAnnotator().annotateNameHtml(baseFile, info); // NOI18N
    }

    /**
     * Text file setup for arbitrary revisions.
     * @param firstRevision first revision or <code>null</code> for inital.
     * @param secondRevision second revision
     */
    public Setup(File baseFile, String firstRevision, String secondRevision) {
        this.baseFile = baseFile;
        this.propertyName = null;        
        this.firstRevision = firstRevision;
        this.secondRevision = secondRevision;
        firstSource = new DiffStreamSource(baseFile, propertyName, firstRevision, firstRevision);
        secondSource = new DiffStreamSource(baseFile, propertyName, secondRevision, secondRevision);
    }

    public String getPropertyName() {
        return propertyName;
    }

    public File getBaseFile() {
        return baseFile;
    }

    public FileInformation getInfo() {
        return info;
    }

    public void setView(DiffController view) {
        this.view = view;
    }

    public DiffController getView() {
        return view;
    }

    public StreamSource getFirstSource() {
        return firstSource;
    }

    public StreamSource getSecondSource() {
        return secondSource;
    }

    public void setNode(DiffNode node) {
        this.node = node;
    }

    public DiffNode getNode() {
        return node;
    }
    
    public String toString() {
        return title;
    }

    /**
     * Loads data over network
     */
    void initSources() throws IOException {
        if (firstSource != null) firstSource.init();
        if (secondSource != null) secondSource.init();
    }

    private static boolean match(int status, int mask) {
        return (status & mask) != 0;
    }
}
