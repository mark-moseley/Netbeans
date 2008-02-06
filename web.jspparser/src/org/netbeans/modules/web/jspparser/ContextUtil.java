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

package org.netbeans.modules.web.jspparser;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** Static utilities related to web context stuff - relative paths, relative objects etc.
 *
 * @author Petr Jiricka
 */
public class ContextUtil {

    /** Returns a message for a given throwable. Optionally includes the throwable
     * stack trace in the message.
     * @param throwable throwable for which to construct the message
     * @param includeStackTrace whether to include a stack trace of the throwable in the message
     * @return an appropriate message for the throwable
     */
    public static String getThrowableMessage(Throwable throwable,
            boolean includeStackTrace) {
        if (includeStackTrace) {
            StringWriter swriter = new StringWriter();
            PrintWriter pw = new PrintWriter(swriter);
            throwable.printStackTrace(pw);
            pw.close();
            return swriter.toString();
        }
        else {
            return throwable.getMessage();
        }
    }
    
    /**********************************
     * Copied over from WebModuleUtils.
     **********************************
     */
    
    /** Decides whether a given file is in the subtree defined by the given folder.
     * Similar to <code>org.openide.filesystems.FileUtil.isParentOf (FileObject folder, FileObject fo)</code>, 
     * but also accepts the case that <code>fo == folder</code>
     */
    public static boolean isInSubTree(FileObject folder, FileObject fo) {
        if (fo == folder) {
            return true;
        }
        else return FileUtil.isParentOf(folder, fo);
    }

    /** Finds a relative resource path between rootFolder and relativeObject. 
     * @return relative path between rootFolder and relativeObject. The returned path
     * never starts with a '/'. It never ends with a '/'.
     * @exception IllegalArgumentException if relativeObject is not in rootFolder's tree.
     */ 
    public static String findRelativePath(FileObject rootFolder, FileObject relativeObject) {
        String rfp = rootFolder.getPath();
        String rop = relativeObject.getPath();
        // check that they share the start of the path 
        if (!isInSubTree (rootFolder, relativeObject)) {
            throw new IllegalArgumentException("" + rootFolder + " / " + relativeObject); // NOI18N
        }
        // now really return the result
        String result = rop.substring(rfp.length());
        if (result.startsWith("/")) { // NOI18N
            result = result.substring(1);
        }
        return result;
    }
    
    /** Finds a relative context path between rootFolder and relativeObject. 
     * Similar to <code>findRelativePath(FileObject, FileObject)</code>, only 
     * different slash '/' conventions.
     * @return relative context path between rootFolder and relativeObject. The returned path
     * always starts with a '/'. It ends with a '/' if the relative object is a directory.
     * @exception IllegalArgumentException if relativeObject is not in rootFolder's tree.
     */ 
    public static String findRelativeContextPath(FileObject rootFolder, FileObject relativeObject) {
        String result = "/" + findRelativePath(rootFolder, relativeObject); // NOI18N
        return relativeObject.isFolder() ? (result + "/") : result; // NOI18N
    }
}
