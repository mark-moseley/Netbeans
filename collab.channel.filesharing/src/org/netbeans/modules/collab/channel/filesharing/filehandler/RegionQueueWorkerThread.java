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
package org.netbeans.modules.collab.channel.filesharing.filehandler;

import com.sun.collablet.CollabException;

import org.openide.*;

import java.util.*;

import org.netbeans.modules.collab.channel.filesharing.FilesharingConstants;
import org.netbeans.modules.collab.core.Debug;


/**
 *
 * @author  Ayub Khan ayub.khan@sun.com
 */
public class RegionQueueWorkerThread extends TimerTask implements FilesharingConstants {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////

    /* RegionQueue */
    RegionQueue queue = null;

    /* interval count, initially set to value resolved through getInterval() */
    protected long delayToProcess = getInterval();

    /* collabFileHandler */
    private CollabFileHandlerSupport collabFileHandler = null;

    /**
     *
     *
     */
    public RegionQueueWorkerThread(RegionQueue queue, CollabFileHandler collabFileHandler) {
        super();
        this.queue = queue;
        this.collabFileHandler = (CollabFileHandlerSupport) collabFileHandler;
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////                       

    /**
     * schedule
     *
     * @param delay
     */
    public void schedule(long delay) {
        collabFileHandler.getContext().schedule(this, delay);
    }

    /**
     * scheduleAtFixedRate
     *
     * @param delay
     * @param rate
     */
    public void scheduleAtFixedRate(long delay, long rate) {
        collabFileHandler.getContext().scheduleAtFixedRate(this, delay, rate);
    }

    /**
     * run
     *
     */
    public void run() {
        Debug.log("SendFileHandler", "RegionQueueWorkerThread, run"); //NoI18n

        if (!isReady()) {
            return;
        }

        try {
            RegionQueueItem lineitem = null;
            int beginLine = Integer.MAX_VALUE;
            int endLine = 0;
            int endOffsetCorrection = 0;
            int count = 0;

            while ((lineitem = queue.getItem()) != null) {
                int begin = lineitem.getBeginLine();
                int end = lineitem.getEndLine();
                endOffsetCorrection = lineitem.getEndOffsetCorrection();

                if (begin < beginLine) {
                    beginLine = begin;
                }

                if (end > endLine) {
                    endLine = end;
                }

                queue.removeItem(lineitem);
                count++;
            }

            Debug.log(
                "SendFileHandler",
                "RegionQueueWorkerThread, " + "createNewRegion beginLine: " + beginLine + " endLine: " + endLine
            ); //NoI18n

            if (count == 1) {
                collabFileHandler.createNewRegion(beginLine, endLine, endOffsetCorrection, true);
            } else {
                collabFileHandler.createNewRegion(beginLine, endLine, 0, true);
            }
        } catch (CollabException ce) {
            ErrorManager.getDefault().notify(ce);
        }
    }

    /**
     * cancel
     *
     */
    public boolean cancel() {
        boolean status = super.cancel();

        return status;
    }

    /**
     * test if region is ready to unlock (ready for removal)
     *
     * @return true if region is ready to unlock (ready for removal)
     */
    public boolean isReady() {
        delayToProcess -= (CREATELOCK_NEWITEM_INCREMENT_DELAY * 2);

        if (delayToProcess <= 0) {
            delayToProcess = getInterval();

            return true;
        }

        return false;
    }

    /**
     * incrementDelay
     *
     * param delayIncrement
     */
    public void incrementDelay(long delayIncrement) {
        delayToProcess += delayIncrement;
    }

    /**
     * getter for region unlock interval
     *
     * @return interval
     */
    public long getInterval() {
        return CREATELOCK_NEWITEM_INCREMENT_DELAY;
    }
}
