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
import org.netbeans.modules.cnd.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class AsmDataLoader extends CCFSrcLoader{
    private static AsmDataLoader instance = null;

    /** Serial version number */
    static final long serialVersionUID = 6801389470714975683L;

    /** The suffix list for Asm primary files */
    private static final String[] asmExtensions =
				{ "s" };	//NOI18N

    protected AsmDataLoader() {
	super("org.netbeans.modules.cnd.loaders.AssemSrcObject");         //NOI18N
	instance = this;
        createExtentions(asmExtensions);
    }

    protected AsmDataLoader(String representationClassName) {
	super(representationClassName);
	instance = this;
        createExtentions(asmExtensions);
    }

    protected AsmDataLoader(Class representationClass) {
	super(representationClass);
	instance = this;
        createExtentions(asmExtensions);
    }

    public static AsmDataLoader getInstance(){
        return instance;
    }
    
    /** set the default display name */
    protected String defaultDisplayName() {
	return NbBundle.getMessage(CCFSrcLoader.class, "PROP_AsmDataLoader_Name"); // NOI18N
    }

    protected String getMimeType(){
        return MIMENames.ASM_MIME_TYPE;
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        return new AssemSrcObject(primaryFile, this);
    }
}
