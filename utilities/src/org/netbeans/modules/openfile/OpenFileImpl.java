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
 * Software is Sun Microsystems, Inc. Portions Copyright 2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.openfile;

import java.io.File;

import org.openide.filesystems.FileObject;

/**
 * Interface for Open File implementations.
 *
 * @author  Marian Petras
 */
public interface OpenFileImpl {

    /**
     * Opens the specified <code>FileObject</code>.
     *
     * @param  fileObject  file to open
     * @param  line    line number to try to open to (starting at zero),
     *                 or <code>-1</code> to ignore
     * @return true on success, false on failure
     */
    boolean open(FileObject fileObject, int line);

}
