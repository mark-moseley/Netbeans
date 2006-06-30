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

package org.apache.tools.ant.module.loader;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 * Recognizes Ant project files according to XML signature.
 */
public class AntProjectDataLoader extends UniFileLoader {

    public static final String REQUIRED_MIME = "text/x-ant+xml"; // NOI18N
    private static final String KNOWN_ANT_FILENAME = "build.xml"; // NOI18N

    private static final long serialVersionUID = 3642056255958054115L;

    public AntProjectDataLoader () {
        super ("org.apache.tools.ant.module.loader.AntProjectDataObject"); // NOI18N
    }

    @Override
    protected String defaultDisplayName () {
        return NbBundle.getMessage (AntProjectDataLoader.class, "LBL_loader_name");
    }

    @Override
    protected void initialize () {
        super.initialize ();
        getExtensions().addMimeType(REQUIRED_MIME);
    }

    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
        FileObject prim = super.findPrimaryFile(fo);
        if (prim == null && fo.getNameExt().equals(KNOWN_ANT_FILENAME)) {
            // XXX hack for #43871.
            // Does not set the MIME type correctly, but at least should be
            // possible to run targets, etc.
            prim = fo;
        }
        return prim;
    }

    @Override
    protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new AntProjectDataObject(primaryFile, this);
    }

    @Override
    protected String actionsContext() {
        return "Loaders/" + REQUIRED_MIME + "/Actions"; // NOI18N
    }

}
