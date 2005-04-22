/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * MultiFileLoaderHid.java
 *
 * Created on September 12, 2001, 7:35 PM
 */

package org.openide.loaders;

import org.openide.filesystems.*;
import java.io.*;

/**
 *
 * @author  Vitezslav Stejskal
 */
public class MultiFileLoaderHid extends MultiFileLoader {
    
    private static final String PRIMARY_EXT = "primary";
    private static final String SECONDARY_EXT = "secondary";
    
    /** Creates new MultiFileLoaderHid */
    public MultiFileLoaderHid () {
        super ("org.openide.loaders.DataObject");
    }

    /** For a given file finds the primary file.
     * @param fo the (secondary) file
     *
     * @return the primary file for the file or <code>null</code> if the file is not
     *  recognized by this loader
     */
    protected FileObject findPrimaryFile (FileObject fo) {
        if (PRIMARY_EXT.equals (fo.getExt ())) {
            return fo;
        }
        if (SECONDARY_EXT.equals (fo.getExt ())) {
            return FileUtil.findBrother (fo, PRIMARY_EXT);
        }
        return null;
    }

    /** Creates a new secondary entry for a given file.
     * Note that separate entries must be created for every secondary
     * file within a given multi-file data object.
     *
     * @param obj requesting object
     * @param secondaryFile a secondary file
     * @return the entry
     */
    protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile) {
        return new FileEntry (obj, secondaryFile);
    }

    /** Creates the right primary entry for a given primary file.
     *
     * @param obj requesting object
     * @param primaryFile primary file recognized by this loader
     * @return primary entry for that file
     */
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
        return new FileEntry (obj, primaryFile);
    }

    /** Creates the right data object for a given primary file.
     * It is guaranteed that the provided file will actually be the primary file
     * returned by {@link #findPrimaryFile}.
     *
     * @param primaryFile the primary file
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has a data object
     */
    protected MultiDataObject createMultiObject (FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new MDO (primaryFile, this);
    }
    
    private static class MDO extends MultiDataObject {
        public MDO (FileObject primaryFile, MultiFileLoader loader) throws DataObjectExistsException {
            super (primaryFile, loader);
        }
    }
}
