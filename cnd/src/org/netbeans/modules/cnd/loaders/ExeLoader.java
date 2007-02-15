/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.loaders;

import java.io.IOException;

import org.openide.filesystems.FileObject;
import org.openide.loaders.UniFileLoader;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.util.NbBundle;

import org.netbeans.modules.cnd.MIMENames;

/**
 *  Recognizes EXE files (Windows, Linux, and Solaris executables, shared objects and
 *  core files).
 */
public class ExeLoader extends UniFileLoader {

    /** Serial version number */
    static final long serialVersionUID = -602486606840357846L;

    /** Single depth cache of last MIME type */
    private static String lastMime;
    
    /** Single depth cache of FileObjects */
    private static FileObject lastFo;

    private static final String KNOWN_EXEFILE_TYPE =
	    "org.netbeans.modules.cnd.ExeLoader.KNOWN_EXEFILE_TYPE"; // NOI18N

    public ExeLoader() {
	super(ExeObject.class.getName());
    }

    public ExeLoader(Class recognizedClass) {
	super(recognizedClass);
    }

    public ExeLoader(String representationClassName) {
	super(representationClassName);
    }
    
    protected String actionsContext() {
        return "Loaders/application/x-executable+elf/Actions/"; // NOI18N
    }

    /** set the default display name */
    protected String defaultDisplayName() {
	return NbBundle.getMessage(MakefileDataLoader.class, "PROP_ExeLoader_Name"); // NOI18N
    }

    protected FileObject findPrimaryFile(FileObject fo) {
	String mime;

	if (fo.isFolder()) {
	    return null;
	}

	Object o = fo.getAttribute(KNOWN_EXEFILE_TYPE);
	if (o != null) {
	    mime = o.toString();
	} else {
	    mime = fo.getMIMEType();
	    if (MIMENames.ELF_GENERIC_MIME_TYPE.equals(mime)) {
		// Fallback matching. We shouldn't see this anymore.
		String name = fo.getNameExt();
		if (name.equals("core") || name.endsWith(".core")) { // NOI18N
		    mime = MIMENames.ELF_CORE_MIME_TYPE;
		} else if (name.endsWith(".o")) { // NOI18N
		    mime = MIMENames.ELF_OBJECT_MIME_TYPE;
		} else if (name.endsWith(".so") || name.indexOf(".so.") >= 0) { // NOI18N
		    mime = MIMENames.ELF_SHOBJ_MIME_TYPE;
		} else {
		    mime = MIMENames.ELF_EXE_MIME_TYPE;
		}
	    }
	}

	if (MIMENames.EXE_MIME_TYPE.equals(mime) ||
                    MIMENames.DLL_MIME_TYPE.equals(mime) ||
                    MIMENames.ELF_EXE_MIME_TYPE.equals(mime) ||
		    MIMENames.ELF_CORE_MIME_TYPE.equals(mime) ||
		    MIMENames.ELF_SHOBJ_MIME_TYPE.equals(mime) ||
		    MIMENames.ELF_OBJECT_MIME_TYPE.equals(mime)) {
	    lastMime = mime;
	    lastFo = fo;

	    try {
		fo.setAttribute(KNOWN_EXEFILE_TYPE, mime);
	    } catch (IOException ex) {
		// We've figured out the mime type, which is the main thing this
		// method needed to do. Its much less important that we couldn't
		// save it. So we just ignore the exception!
	    }

	    return fo;
	} else {
	    return null;
	}
    }
    
    protected MultiDataObject createMultiObject(FileObject primaryFile)
			throws DataObjectExistsException, IOException {
	String mime;

	if (lastFo.equals(primaryFile)) {
	    mime = lastMime;
	} else {
	    mime = primaryFile.getMIMEType();
	}

	if (mime.equals(MIMENames.EXE_MIME_TYPE)) {
	    return new ExeObject(primaryFile, this);
//	} else if (mime.equals(MIMENames.DLL_MIME_TYPE)) {
//	    return new ExeDllObject(primaryFile, this);
	} else if (mime.equals(MIMENames.ELF_EXE_MIME_TYPE)) {
	    return new ExeElfObject(primaryFile, this);
	} else if (mime.equals(MIMENames.ELF_CORE_MIME_TYPE)) {
	    return new CoreElfObject(primaryFile, this);
	} else if (mime.equals(MIMENames.ELF_SHOBJ_MIME_TYPE)) {
	    return new DllObject(primaryFile, this);
	} else {
	    return new OrphanedElfObject(primaryFile, this);
	}
    }
}
