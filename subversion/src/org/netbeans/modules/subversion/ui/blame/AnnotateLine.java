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
package org.netbeans.modules.subversion.ui.blame;

import org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath;

import java.util.*;

/**
 * One line of annotation, this is copied from CVS so that other support classes stay the same.
 *
 * @author Maros Sandor
 */
public class AnnotateLine {

    private String  author;
    private String  revision;
    private Date    date;
    private String  content;
    private int     lineNum;

    private String commitMessage;
    
    /**
     * The default is true to enable rollback even if we were unable to determine the correct value.  
     */ 
    private boolean canBeRolledBack = true;

    private ISVNLogMessageChangePath [] changedPaths;

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public ISVNLogMessageChangePath[] getChangedPaths() {
        return changedPaths;
    }

    public void setChangedPaths(ISVNLogMessageChangePath[] changedPaths) {
        this.changedPaths = changedPaths;
    }

    /**
     * Returns the author of this line.
     */
    public String getAuthor() {
        return author != null ? author : "";
    }

    /**
     * Sets the author of this line.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Returns the revision of this line.
     */
    public String getRevision() {
        return revision;
    }

    /**
     * Sets the revision of this line.
     */
    public void setRevision(String revision) {
        this.revision = revision;
    }

    /**
     * Returns the date of this line.
     */
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
    /**
     * Return the line's content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the line's content.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns the line's number. It's 1 based.
     */
    public int getLineNum() {
        return lineNum;
    }

    /**
     * Returns the line's number.
     */
    public Integer getLineNumInteger() {
        return new Integer(lineNum);
    }

    /**
     * Sets the line's number.
     */
    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    /**
     * @return false if the file was added to repository (created) in this revision, true otherwise
     */ 
    public boolean canBeRolledBack() {
        return this.canBeRolledBack;
    }

    public void setCanBeRolledBack(boolean canBeRolledBack) {
        this.canBeRolledBack = canBeRolledBack;
    }
}