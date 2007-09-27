/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.api.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.ResourceBundle;
import org.netbeans.modules.cnd.loaders.ShellDataLoader;
import org.openide.loaders.ExtensionList;
import org.openide.util.NbBundle;

public class ShellFileFilter extends javax.swing.filechooser.FileFilter {

    private static ShellFileFilter instance = null;

    public ShellFileFilter() {
	super();
    }

    public static ShellFileFilter getInstance() {
	if (instance == null)
	    instance = new ShellFileFilter();
	return instance;
    }
    
    public String getDescription() {
	return(getString("FILECHOOSER_SHELL_FILEFILTER")); // NOI18N
    }
    
    public boolean accept(File f) {
	if (f != null) {
	    if (f.isDirectory()) {
		return true;
	    }
	    if (checkExtension(f))
		return true;
	    if (checkFirstFewBytes(f))
		return true;
	}
	return false;
    }

    private boolean checkExtension(File f) {
	// recognize shell scripts by extension
	String fname = f.getName();
	String ext = null;
	int i = fname.lastIndexOf('.');
	if (i > 0)
	    ext = fname.substring(i+1);

	ExtensionList extensions = ShellDataLoader.getInstance().getExtensions();
	for (Enumeration e = extensions.extensions(); e != null &&  e.hasMoreElements();) {
	    String ex = (String) e.nextElement();
	    if (ex != null && ex.equals(ext))
		return true;
	}

	return false;
    }

    /** Check if this file's header represents an elf executable */
    private boolean checkFirstFewBytes(File f) {
        byte b[] = new byte[2];
	InputStream is = null;
	try {
	    is = new FileInputStream(f);
	    int n = is.read(b, 0, 2);
	    if (n < 2) {
	        // File isn't big enough ...
		return false;
	    }
	} catch (Exception e) {
	    return false;
	} finally {
	    if (is != null) {
		try {
		    is.close();
		} catch (IOException e) {
		}
	    }
	}
	if (b[0] == '#' && b[1] == '!')
	    return true;

	return false;
    }

    /** Look up i18n strings here */
    private ResourceBundle bundle;
    private String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(ShellFileFilter.class);
	}
	return bundle.getString(s);
    }
}
