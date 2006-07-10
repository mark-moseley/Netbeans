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
package org.netbeans.modules.collab.channel.filesharing.config;

import com.sun.collablet.CollabException;

import org.openide.*;
import org.netbeans.modules.collab.channel.filesharing.mdc.util.CollabProcessorConfig;


/**
 * Default EventProcessor
 *
 * @author  Ayub Khan, ayub.khan@sun.com
 * @version 1.0
 */
public class FilesharingProcessorConfig extends CollabProcessorConfig {
    ////////////////////////////////////////////////////////////////////////////
    // Constants
    ////////////////////////////////////////////////////////////////////////////

    /* configURL */
    private static final String configURL = "nbresloc:/org/netbeans/modules/collab/channel/filesharing/resources/filesharing_event_processor_config.xml";

    /**
     * constructor
     *
     */
    public FilesharingProcessorConfig() {
        super();

        try {
            init();
        } catch (CollabException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    /**
     * constructor
     *
     */
    public FilesharingProcessorConfig(String currentVersion) {
        super(currentVersion);

        try {
            init();
        } catch (CollabException e) {
            ErrorManager.getDefault().notify(e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * init
     *
     */
    public void init() throws CollabException {
        init(configURL);
    }
}
