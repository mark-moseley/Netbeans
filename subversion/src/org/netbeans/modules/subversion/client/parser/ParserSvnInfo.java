/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.subversion.client.parser;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Date;
import org.tigris.subversion.svnclientadapter.ISVNInfo;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNScheduleKind;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Ed Hillmann
 */
public class ParserSvnInfo implements ISVNInfo{
    
    private final File file;
    private final SVNUrl url;
    private final SVNUrl reposUrl;
    private final String reposUuid;
    private final SVNScheduleKind schedule;
    private final SVNRevision.Number revision;
    private final boolean isCopied;
    private final SVNUrl urlCopiedFrom;
    private final SVNRevision.Number revisionCopiedFrom;
    private final Date lastChangedDate;
    private final SVNRevision.Number lastChangedRevision;
    private final String lastCommitAuthor;
    private final Date lastDatePropsUpdate;
    private final Date lastDateTextUpdate;
    private final Date lockCreationDate;
    private final String lockOwner;
    private final String lockComment;
    private final SVNNodeKind nodeKind;
    private final File propertiesFile;
    private final File basePropertiesFile;
    
    /** Creates a new instance of LocalSvnInfoImpl */
    public ParserSvnInfo(File file, String url, String reposUrl, String reposUuid,
        String schedule, long revision, boolean isCopied, String urlCopiedFrom, 
        long revisionCopiedFrom, Date lastChangedDate, long lastChangedRevision,
        String lastCommitAuthor, Date lastDatePropsUpdate, Date lastDateTextUpdate,
        Date lockCreationDate, String lockOwner, String lockComment, String nodeKind, 
        File propertiesFile, File basePropertiesFile) {
        this.file = file;
        try {
            this.url = url != null ? new SVNUrl(url) : null;
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
        try {
            this.reposUrl = reposUrl != null ? new SVNUrl(reposUrl) : null;
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
        this.reposUuid = reposUuid;
        
        this.schedule = SVNScheduleKind.fromString(schedule);
        this.revision = new SVNRevision.Number(revision);
        
        this.isCopied = isCopied;
        try {
            this.urlCopiedFrom = isCopied && urlCopiedFrom != null ? new SVNUrl(urlCopiedFrom) : null;
        } catch (MalformedURLException ex) {
            throw new IllegalArgumentException(ex);
        }
        this.revisionCopiedFrom = isCopied ? new SVNRevision.Number(revisionCopiedFrom) : null;
        
        this.lastChangedDate = lastChangedDate;
        this.lastChangedRevision = new SVNRevision.Number(lastChangedRevision);
        this.lastCommitAuthor = lastCommitAuthor;
        
        this.lastDatePropsUpdate = lastDatePropsUpdate;
        this.lastDateTextUpdate = lastDateTextUpdate;
        
        this.lockCreationDate = lockCreationDate;
        this.lockOwner = lockOwner;
        this.lockComment = lockComment;
        
        this.nodeKind = SVNNodeKind.fromString(nodeKind);
        this.propertiesFile = propertiesFile;
        this.basePropertiesFile = basePropertiesFile;
    }

    public boolean isCopied() {
        return isCopied;
    }

    public String getUuid() {
        return reposUuid;
    }

    public SVNUrl getUrl() {
        return url;
    }

    public SVNScheduleKind getSchedule() {
        return schedule;
    }

    public SVNRevision.Number getRevision() {
        return revision;
    }

    public SVNRevision.Number getCopyRev() {
        return revisionCopiedFrom;
    }

    public SVNUrl getCopyUrl() {
        return urlCopiedFrom;
    }

    public File getFile() {
        return file;
    }

    public Date getLastChangedDate() {
        return lastChangedDate;
    }

    public SVNRevision.Number getLastChangedRevision() {
        return lastChangedRevision;
    }

    public String getLastCommitAuthor() {
        return lastCommitAuthor;
    }

    public Date getLastDatePropsUpdate() {
        return lastDatePropsUpdate;
    }

    public Date getLastDateTextUpdate() {
        return lastDateTextUpdate;
    }

    public String getLockComment() {
        return lockComment;
    }

    public Date getLockCreationDate() {
        return lockCreationDate;
    }

    public String getLockOwner() {
        return lockOwner;
    }

    public SVNNodeKind getNodeKind() {
        return nodeKind;
    }

    public SVNUrl getRepository() {
        return reposUrl;
    }

    public String getUrlString() {
        return url.toString();
    }

    public File getPropertyFile() {
        return propertiesFile;
    }
    
    public File getBasePropertyFile() {
        return basePropertiesFile;        
    }
}
