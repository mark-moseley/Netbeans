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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.xml.tools.generator;

import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.*;


import org.netbeans.modules.xml.core.lib.AbstractUtil;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Libor Kramolis
 * @version 0.2
 */
class Util extends AbstractUtil {

    public static final NameCheck JAVA_CHECK = new JavaIdentifierNameCheck();

    public static final NameCheck NONEMPTY_CHECK = new StringNameCheck();

    /** Default and only one instance of this class. */
    public static final Util THIS = new Util();

    /** Nobody can create instance of it, just me. */
    private Util () {
    }

    
    /** A name checker interface. */
    public static interface NameCheck {
        public boolean checkName (String name);        
    }

    /** Passes for java identifiers. */
    public static class JavaIdentifierNameCheck implements NameCheck {
        public boolean checkName (String name) {
            return name.length() > 0 && org.openide.util.Utilities.isJavaIdentifier(name);
        }
    }

    /** Passes for any non-empty string. */
    public static class StringNameCheck implements NameCheck {
        public boolean checkName (String name) {
            return name.length() > 0;
        }
    }

    /**
     * Calculate JTable cell height for textual rows.
     */
    public static int getTextCellHeight(JTable table) {
        JComboBox template = new JComboBox();
        return template.getPreferredSize().height;
    }
    
//    /**
//     * Finds Java package name for the given folder.
//     * @return package name separated by dots or null if package name
//     *     could not be find for the given file
//     */
//    public static String findJavaPackage(FileObject fo) {
//        assert fo.isFolder() : fo;
//        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
//        if (cp == null) {
//            return null;
//        }
//        return cp.getResourceName(fo, '.', false);
//    }
    
}
