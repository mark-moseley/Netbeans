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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.autoupdate;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.autoupdate.services.OperationContainerImpl;
import org.netbeans.modules.autoupdate.services.OperationSupportImpl;

/**
 * @author Radek Matous, Jiri Rechtacek
 */
public final class OperationSupport {

    OperationSupport () {
    }

    public Restarter doOperation (ProgressHandle progress) throws OperationException {
        Boolean res =  getImpl (container.impl.getType ()).doOperation (progress, container);
        if (res == null /*was problem*/ || ! res) {
            return null;
        } else {
            return new Restarter ();
        }
    }

    public void doCancel () throws OperationException {
        getImpl (container.impl.getType ()).doCancel ();
    }

    public void doRestart (Restarter restarter, ProgressHandle progress) throws OperationException {
        getImpl (container.impl.getType ()).doRestart (restarter, progress);
    }

    public void doRestartLater (Restarter restarter) {
        getImpl (container.impl.getType ()).doRestartLater (restarter);
    }
    
    public static final class Restarter { Restarter () {} }
    
    //end of API - next just impl details
    private OperationContainer<OperationSupport> container;

    void setContainer (OperationContainer<OperationSupport> c) {
        container = c;
    }

    // private
    private static OperationSupportImpl getImpl (OperationContainerImpl.OperationType type) {
        assert type != null : "OperationContainerImpl.OperationType cannot be null.";
        OperationSupportImpl impl = null;
        switch (type) {
            case INSTALL:
                impl = OperationSupportImpl.forInstall ();
                break;
            case UNINSTALL:
                impl = OperationSupportImpl.forUninstall ();
                break;
            case DIRECT_UNINSTALL:
                impl = OperationSupportImpl.forDirectUninstall ();
                break;
            case UPDATE:
                impl = OperationSupportImpl.forUpdate ();
                break;
            case ENABLE:
                impl = OperationSupportImpl.forEnable ();
                break;
            case DISABLE:
                impl = OperationSupportImpl.forDisable ();
                break;
            case DIRECT_DISABLE:
                impl = OperationSupportImpl.forDirectDisable ();
                break;
            case CUSTOM_INSTALL:
                impl = OperationSupportImpl.forCustomInstall ();
                break;
            default:
                assert false : "Unknown OperationSupport for type " + type;
        }
        assert impl != null : "OperationSupportImpl cannot be null for operation " + type;
        return impl;
    }
}