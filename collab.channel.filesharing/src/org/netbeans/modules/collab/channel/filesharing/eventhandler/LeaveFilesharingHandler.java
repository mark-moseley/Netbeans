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
package org.netbeans.modules.collab.channel.filesharing.eventhandler;

import com.sun.collablet.CollabException;

import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import java.io.IOException;

import org.netbeans.modules.collab.channel.filesharing.filehandler.CollabFileHandler;
import org.netbeans.modules.collab.channel.filesharing.filehandler.SharedFileGroup;
import org.netbeans.modules.collab.channel.filesharing.mdc.CollabContext;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;
import org.netbeans.modules.collab.channel.filesharing.msgbean.CCollab;
import org.netbeans.modules.collab.channel.filesharing.msgbean.LeaveFilesharing;
import org.netbeans.modules.collab.channel.filesharing.msgbean.User;
import org.netbeans.modules.collab.core.Debug;


/**
 * LeaveFilesharing EventHandler
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class LeaveFilesharingHandler extends FilesharingEventHandler {
    /**
     * constructor
     *
     */
    public LeaveFilesharingHandler(CollabContext context) {
        super(context);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Event Handler methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * constructMsg
     *
     * @param        evContext                                        Event Context
     */
    public CCollab constructMsg(EventContext evContext) {
        CCollab collab = new CCollab();
        collab.setVersion(getVersion());

        LeaveFilesharing leaveFilesharing = new LeaveFilesharing();
        collab.setChLeaveFilesharing(leaveFilesharing);

        //add this element to message when begin to join a conversation.
        User userObj = new User();
        userObj.setId(getLoginUser());
        leaveFilesharing.setUser(userObj);

        return collab;
    }

    /**
     * handleMsg
     *
     * @param        collabBean
     * @param        messageOriginator
     * @param        isUserSame
     */
    public void handleMsg(CCollab collabBean, String messageOriginator, boolean isUserSame)
    throws CollabException {
        if (isUserSame) {
            getContext().setReceivedMessageState(false);

            return; //skip if fileowner==loginUser
        }

        LeaveFilesharing leaveFilesharing = collabBean.getChLeaveFilesharing();
        String newFileOwner = getContext().getNewFileOwner(leaveFilesharing.getNewFileOwner().getUsers());

        //delete all shared files that belong to this user
        String[] fileGroupNames = getContext().getUserSharedFileGroupNames(messageOriginator);

        if (fileGroupNames != null) {
            for (int i = 0; i < fileGroupNames.length; i++) {
                SharedFileGroup sharedFileGroup = getContext().getSharedFileGroupManager().getSharedFileGroup(
                        fileGroupNames[i]
                    );

                if (sharedFileGroup != null) {
                    //set valid to false before delete
                    CollabFileHandler[] fhs = sharedFileGroup.getFileHandlers();

                    for (int j = 0; j < fhs.length; j++) {
                        if (fhs[j] != null) {
                            fhs[j].setValid(false);

                            String fileGroupName = fhs[j].getFileGroupName();
                            Debug.out.println("LeaveFS, fileGroup: " + fileGroupName);
                            try {
                                deleteSharedFiles(fileGroupName, messageOriginator, false);
                            } catch(Exception e) {
                                Debug.logDebugException("LeaveFS, Exception " + 
                                        "removing files and filehandlers",e,true);
                            } 
                        }
                    }

                    //remove all previous file to owner map for messageOriginator
                    getContext().removeFileOwnerMap(sharedFileGroup);
                }
            }
        }

        if (
            (getContext().getFilesystemExplorer() != null) &&
                (getContext().getFilesystemExplorer().getRootNode() != null)
        ) {
            Node userNode = getContext().getFilesystemExplorer().getRootNode().getChildren().findChild(
                    messageOriginator
                );

            try {
                if (userNode != null) {
                    DataObject dd = (DataObject) userNode.getCookie(DataObject.class);

                    //delete node
                    if (userNode != null) {
                        userNode.destroy();
                    }
                }
            } catch (IOException iox) {
                Debug.out.println("LeaveFS delete userNode ex: " + iox);
                iox.printStackTrace(Debug.out);
            }
        }

        //remove the current file owner from the fileowner list
        getContext().removeFileOwner(messageOriginator);

        //remove the user that leaves filesharing, from the user list
        getContext().removeUser(messageOriginator);

        String newModerator = getContext().getNewModerator(leaveFilesharing.getNewModerator().getUsers());

        //We dont use this data, everyone is a moderator
        if ((newModerator != null) && !newModerator.equals("") && newModerator.equals(getLoginUser())) {
            //isModerator = true;
        }
    }
}
