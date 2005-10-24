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
package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.command.PipedFileInformation;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.event.FileInfoEvent;
import org.openide.filesystems.FileUtil;

import java.io.File;

/**
 * Fetches given revision.
 *
 * @author Petr Kuzel
 */
final class VersionsCacheExecutor extends ExecutorSupport {

    private File checkedOutVersion;

    public VersionsCacheExecutor(CheckoutCommand cmd, GlobalOptions options) {
        super(CvsVersioningSystem.getInstance(), cmd, options);
    }

    protected synchronized void commandFinished(ClientRuntime.Result result) {
        if (checkedOutVersion == null) {
            // typical for dead files
            // System.err.println("CVS: " + cmd.getCVSCommand() + " misses piped response!");
        }
        notifyAll();
    }

    public void fileInfoGenerated(FileInfoEvent e) {
        PipedFileInformation info = (PipedFileInformation) e.getInfoContainer();
        checkedOutVersion = FileUtil.normalizeFile(info.getTempFile());
    }

    public File getCheckedOutVersion() {
        return checkedOutVersion;
    }

    protected boolean logCommandOutput() {
        return ((CheckoutCommand)cmd).isPipeToOutput() == false;
    }


}
