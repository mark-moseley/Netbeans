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
package org.netbeans.modules.collab.channel.filesharing.context;

import org.netbeans.modules.collab.channel.filesharing.filehandler.SharedFileGroup;
import org.netbeans.modules.collab.channel.filesharing.mdc.EventContext;


/**
 * Bean that holds sendfile context
 *
 * @author Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class SendFileContext extends EventContext {
    /**
         *
         * @param collabBean
         * @param messageOriginator
         * @param isUserSame
         */
    public SendFileContext(String eventID, Object sharedFileGroup) {
        super(eventID, sharedFileGroup);
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////	
    public SharedFileGroup getSharedFileGroup() {
        return (SharedFileGroup) this.getSource();
    }
}
