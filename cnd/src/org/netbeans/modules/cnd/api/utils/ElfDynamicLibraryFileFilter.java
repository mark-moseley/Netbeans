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

package org.netbeans.modules.cnd.api.utils;

import java.io.File;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

public class ElfDynamicLibraryFileFilter extends javax.swing.filechooser.FileFilter {

    private static ElfDynamicLibraryFileFilter instance = null;

    public ElfDynamicLibraryFileFilter() {
	super();
    }

    public static ElfDynamicLibraryFileFilter getInstance() {
	if (instance == null)
	    instance = new ElfDynamicLibraryFileFilter();
	return instance;
    }

    public String getDescription() {
	return getString("ELF_DYNAMIC_LIB_FILTER"); // NOI18N
    }
    
    public boolean accept(File f) {
	if (f != null) {
	    if (f.isDirectory()) {
		return true;
	    }
	    return f.getName().endsWith(".so"); // NOI18N
	}
	return false;
    }

    /** Look up i18n strings here */
    private ResourceBundle bundle;
    private String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(ElfDynamicLibraryFileFilter.class);
	}
	return bundle.getString(s);
    }
}
