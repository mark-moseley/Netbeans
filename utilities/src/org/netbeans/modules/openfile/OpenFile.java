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

package org.netbeans.modules.openfile;

import java.io.File;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Opens files when requested. Main functionality.
 * @author Jaroslav Tulach, Jesse Glick
 * @author Marian Petras
 */
public final class OpenFile {

    /** do not instantiate */
    private OpenFile() {}

    /**
     * Open a file (object) at the beginning.
     * @param fileObject the file to open
     * @param line 
     * @return error message or null on success
     * @usecase  API
     */
    public static String open(FileObject fileObject, int line) {
        for (OpenFileImpl impl : Lookup.getDefault().lookupAll(OpenFileImpl.class)) {
            if (impl.open(fileObject, line)) {
                return null;
            }
        }
        return NbBundle.getMessage(OpenFile.class, "MSG_FileIsNotPlainFile", fileObject);
    }
    
    /**
     * Opens a file.
     *
     * @param  file  file to open (must exist)
     * @param  line  line number to try to open to (starting at zero),
     *               or <code>-1</code> to ignore
     * @return null on success, otherwise the error message
     * @usecase CallbackImpl, OpenFileAction
     */
    static String openFile(File file, int line) {
        String msg = checkFileExists(file);
        if (msg != null) {
            return msg;
        }
                              
        FileObject fileObject;
        fileObject = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        if (fileObject != null) {
            return open(fileObject, line);
        }
        return NbBundle.getMessage(OpenFile.class, "MSG_FileDoesNotExist", file);
    }
    
    /**
     * Checks whether the specified file exists.
     * If the file doesn't exists, displays a message.
     * <p>
     * The code for displaying the message is running in a separate thread
     * so that it does not block the current thread.
     *
     * @param  file  file to check for existence
     * @return  null on success, otherwise the error message
     */
    private static String checkFileExists(File file) {
        final String errMsgKey;
        if (!file.exists()) {
            errMsgKey = "MSG_fileNotFound";                             //NOI18N
        } else if (isSpecifiedByUNCPath(file)) {
            errMsgKey = "MSG_UncNotSupported";                          //NOI18N
        } else if (!file.isFile() && !file.isDirectory()) {
            errMsgKey = "MSG_fileNotFound";                             //NOI18N
        } else {
            return null;
        }
        
        final String fileName = file.toString();
        final String msg = NbBundle.getMessage(OpenFile.class,
                                               errMsgKey,
                                               fileName);
        return msg;
    }

    /**
     * Checks whether a given file is specified by an UNC path.
     *
     * @param  file  existing file to check
     * @return  <code>true</code> if the file is specified by UNC path;
     *          <code>false</code> otherwise
     */
    static boolean isSpecifiedByUNCPath(File file) {
        assert file != null && file.exists();

        file = FileUtil.normalizeFile(file);
        return file.getPath().startsWith("\\\\");                       //NOI18N
    }
    
}
