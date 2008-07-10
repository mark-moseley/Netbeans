/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.junit.output;

import java.util.Collection;
import javax.swing.Action;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.junit.wizards.Utils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;

/**
 *
 * @author Marian Petras
 */
final class OutputUtils {

    static final Action[] NO_ACTIONS = new Action[0];
    
    private OutputUtils() {
    }
    
    /**
     */
    static void openCallstackFrame(Node node,
                                   String frameInfo) {
        Report report = getTestsuiteNode(node).getReport();
        Collection<FileObject> srcRoots = report.classpathSourceRoots;
        if ((srcRoots == null) || srcRoots.isEmpty()) {
            return;
        }

        FileObject[] srcRootsArr = new FileObject[srcRoots.size()];
        srcRoots.toArray(srcRootsArr);
        ClassPath srcClassPath = ClassPathSupport.createClassPath(srcRootsArr);

        final int[] lineNumStorage = new int[1];
        FileObject file = getFile(frameInfo, lineNumStorage, srcClassPath);
        Utils.openFile(file, lineNumStorage[0]);
    }

    /**
     * Returns a {@code Report} for the given node.
     * @param  node  node to find {@code Report} for
     * @return  found report
     */
    static Report getReport(Node node) {
        return getTestsuiteNode(node).getReport();
    }
        
    /**
     */
    private static TestsuiteNode getTestsuiteNode(Node node) {
        while (!(node instanceof TestsuiteNode)) {
            node = node.getParentNode();
        }
        return (TestsuiteNode) node;
    }
    
    /**
     * Returns FileObject corresponding to the given callstack line.
     *
     * @param  callstackLine  string representation of a callstack window
     *                        returned by the JUnit framework
     */
    private static FileObject getFile(final String callstackLine,
                                      final int[] lineNumStorage,
                                      final ClassPath classPath) {

        /* Get the part before brackets (if any brackets present): */
        int bracketIndex = callstackLine.indexOf('(');
        String beforeBrackets = (bracketIndex == -1)
                                ? callstackLine
                                : callstackLine.substring(0, bracketIndex)
                                  .trim();
        String inBrackets = (bracketIndex == -1)
                            ? (String) null
                            : callstackLine.substring(
                                    bracketIndex + 1,
                                    callstackLine.lastIndexOf(')'));

        /* Get the method name and the class name: */
        int lastDotIndex = beforeBrackets.lastIndexOf('.');
        String clsName = beforeBrackets.substring(0, lastDotIndex);
        String methodName = beforeBrackets.substring(lastDotIndex + 1);

        /* Get the file name and line number: */
        String fileName = null;
        int lineNum = -1;
        if (inBrackets != null) {
            // RegexpUtils.getInstance() retns instance from ResultPanelTree
            if (RegexpUtils.getInstance().getLocationInFilePattern()
                    .matcher(inBrackets).matches()) {
                int ddotIndex = inBrackets.lastIndexOf(':'); //srch from end
                if (ddotIndex == -1) {
                    fileName = inBrackets;
                } else {
                    fileName = inBrackets.substring(0, ddotIndex);
                    try {
                        lineNum = Integer.parseInt(
                                       inBrackets.substring(ddotIndex + 1));
                        if (lineNum <= 0) {
                            lineNum = 1;
                        }
                    } catch (NumberFormatException ex) {
                        /* should never happen as it passed the regexp */
                        assert false;
                    }
                }
            }
        }

        /* Find the file: */
        FileObject file;
        String thePath;

        //PENDING - Once 'thePath' is found for a given <clsName, fileName>
        //          pair, it could be cached for further uses
        //          (during a single AntSession).

        String clsNameSlash = clsName.replace('.', '/');
        String slashName, ending;
        int lastSlashIndex;

        if (fileName == null) {
            lastSlashIndex = clsNameSlash.length();
            slashName = clsNameSlash;
            ending = ".java";                                           //NOI18N
        } else {
            lastSlashIndex = clsNameSlash.lastIndexOf('/');
            slashName = (lastSlashIndex != -1)
                        ? clsNameSlash.substring(0, lastSlashIndex)
                        : clsNameSlash;
            ending = '/' + fileName;
        }
        file = classPath.findResource(thePath = (slashName + ending));
        while ((file == null) && (lastSlashIndex != -1)) {
            slashName = slashName.substring(0, lastSlashIndex);
            file = classPath.findResource(thePath = (slashName + ending));
            if (file == null) {
                lastSlashIndex = slashName.lastIndexOf(
                                                '/', lastSlashIndex - 1);
            }
        }
        if ((file == null) && (fileName != null)) {
            file = classPath.findResource(thePath = fileName);
        }

        /* Return the file (or null if no matching file was found): */
        if (file == null) {
            lineNum = -1;
        }
        lineNumStorage[0] = lineNum;
        return file;
    }

}
