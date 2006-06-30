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

package org.netbeans.modules.tomcat5.util;

import java.io.File;
import junit.textui.TestRunner;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.tomcat5.TomcatFactory55;
import org.netbeans.modules.tomcat5.TomcatFactory55Test;
import org.netbeans.modules.tomcat5.TomcatManager;
import org.openide.util.Utilities;

/**
 *
* @author sherold
 */
public class TestUtils {

    /** rm -rf file */
    public static void rm(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
        } else {
            File files[] = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                rm(files[i]);
            }
            file.delete();
        }
    }
}
