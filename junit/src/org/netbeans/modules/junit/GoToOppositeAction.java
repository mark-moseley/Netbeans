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

package org.netbeans.modules.junit;

import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.awt.EventQueue;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.gototest.TestLocator;
import org.netbeans.modules.junit.plugin.JUnitPlugin;
import org.netbeans.modules.junit.plugin.JUnitPlugin.Location;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Jumps to the opposite class or method.
 * If the cursor is currently in a source method, this action will jump to the
 * corresponding test method and vice versa. If the cursor is currently in a
 * source class but not in any method, this action will switch to the beginning
 * of the corresponding class.
 *
 * @see  OpenTestAction
 * @author  Marian Petras
 */
@SuppressWarnings("serial")
public final class GoToOppositeAction implements TestLocator {
    
    public GoToOppositeAction() {
    }
    

    public boolean asynchronous() {
        return true;
    }

    public LocationResult findOpposite(FileObject fileObj) {
        throw new UnsupportedOperationException("JUnit's GoToOppositeAction is asynchronous");
    }
        
    public void findOpposite(FileObject fileObj, LocationListener callback) {
        boolean isJavaFile = false;
        ClassPath srcCP;
        FileObject fileObjRoot;
        Project project;
        boolean sourceToTest = true;
        
        if ((fileObj == null)
          || !fileObj.isFolder() && !(isJavaFile = TestUtil.isJavaFile(fileObj))
          || ((srcCP = ClassPath.getClassPath(fileObj, ClassPath.SOURCE)) == null)
          || ((fileObjRoot = srcCP.findOwnerRoot(fileObj)) == null)
          || ((project = FileOwnerQuery.getOwner(fileObjRoot)) == null)
          || (UnitTestForSourceQuery.findUnitTests(fileObjRoot).length == 0)
              && !(sourceToTest = false)         //side effect - assignment
              && (!isJavaFile || (UnitTestForSourceQuery.findSources(fileObjRoot).length == 0))) {
            callback.foundLocation(fileObj, new LocationResult(null));
            return;
        }
        
        JUnitPlugin plugin = TestUtil.getPluginForProject(project);
        assert plugin != null;
        
        SourceGroup[] srcGroups;
        FileObject[] srcRoots;
        srcGroups = ProjectUtils.getSources(project)
                    .getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        srcRoots = new FileObject[srcGroups.length];
        for (int i = 0; i < srcGroups.length; i++) {
            srcRoots[i] = srcGroups[i].getRootFolder();
        }
        ClassPath srcClassPath = ClassPathSupport.createClassPath(srcRoots);

        /*
        ClasspathInfo cpInfo = ClasspathInfo.create(
                        ClassPath.getClassPath(fileObj, ClassPath.BOOT),
                        ClassPath.getClassPath(fileObj, ClassPath.COMPILE),
                        srcClassPath);
        int caretPos = editorPane.getCaretPosition();
        boolean fromSourceToTest = sourceToTest;
        
        JavaSource javaSource = JavaSource.create(
                cpInfo,
                Collections.<FileObject>singleton(fileObj));
        
        ElementFinder elementFinder = new ElementFinder(caretPos);
        try {
            javaSource.runUserActionTask(elementFinder, true);
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);     //NOI18N
        }
        Element element = elementFinder.getElement();
        */
        RequestProcessor.getDefault().post(
                new ActionImpl(plugin,
                               callback,
                               new Location(fileObj/*, element*/),
                               sourceToTest,
                               srcClassPath));
    }
    
    /**
     * Determines an element at the current cursor position.
     */
    private class ElementFinder implements CancellableTask<CompilationController> {
        
        /** */
        private final int caretPosition;
        /** */
        private volatile boolean cancelled;
        /** */
        private Element element = null;
        
        /**
         */
        private ElementFinder(int caretPosition) {
            this.caretPosition = caretPosition;
        }
    
        /**
         */
        public void run(CompilationController controller) throws IOException {
            controller.toPhase(Phase.RESOLVED);     //cursor position needed
            if (cancelled) {
                return;
            }

            TreePath treePath = controller.getTreeUtilities()
                                          .pathFor(caretPosition);
            if (treePath != null) {
                if (cancelled) {
                    return;
                }
                
                TreePath parent = treePath.getParentPath();
                while (parent != null) {
                    Tree.Kind parentKind = parent.getLeaf().getKind();
                    if ((parentKind == Tree.Kind.CLASS)
                            || (parentKind == Tree.Kind.COMPILATION_UNIT)) {
                        break;
                    }
                    treePath = parent;
                    parent = treePath.getParentPath();
                }

            }

            if (treePath != null) {
                if (cancelled) {
                    return;
                }

                try {
                    element = controller.getTrees().getElement(treePath);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger("global").log(Level.WARNING, null, ex);
                }
            }
        }
        
        /**
         */
        public void cancel() {
            cancelled = true;
        }
        
        /**
         */
        Element getElement() {
            return element;
        }

    }
    
    /**
     * 
     */
    private class ActionImpl implements Runnable {
        
        private final JUnitPlugin plugin;
        private final Location currLocation;
        private final boolean sourceToTest;
        private final ClassPath srcClassPath;
        private final LocationListener callback;
        
        private Location oppoLocation;
        
        ActionImpl(JUnitPlugin plugin,
                   LocationListener callback,
                   Location currLocation,
                   boolean sourceToTest,
                   ClassPath srcClassPath) {
            this.plugin = plugin;
            this.currLocation = currLocation;
            this.sourceToTest = sourceToTest;
            this.srcClassPath = srcClassPath;
            this.callback = callback;
        }
        
        public void run() {
            if (!EventQueue.isDispatchThread()) {
                findOppositeLocation();
                if ((oppoLocation != null) || sourceToTest) {
                    EventQueue.invokeLater(this);
                }
            } else {
                if (oppoLocation != null) {
                    goToOppositeLocation();
                } else if (sourceToTest) {
                    displayNoOppositeLocationFound();
                }
            }
        }
        
        /**
         */
        private void findOppositeLocation() {
            oppoLocation = sourceToTest
                  ? JUnitPluginTrampoline.DEFAULT.getTestLocation(plugin,
                                                                  currLocation)
                  : JUnitPluginTrampoline.DEFAULT.getTestedLocation(plugin,
                                                                  currLocation);
        }
        
        /**
         */
        private void goToOppositeLocation() {
            assert oppoLocation != null;
            assert oppoLocation.getFileObject() != null;

            final FileObject oppoFile = oppoLocation.getFileObject();
//            final ElementHandle<Element> elementHandle
//                                         = oppoLocation.getElementHandle();
//            if (elementHandle != null) {
//                OpenTestAction.openFileAtElement(oppoFile, elementHandle);
//            } else {
//                OpenTestAction.openFile(oppoFile);
                  callback.foundLocation(currLocation.getFileObject(), new LocationResult(oppoFile, -1));
//            }
        }
        
        /**
         */
        private void displayNoOppositeLocationFound() {
            String sourceClsName;
            FileObject fileObj = currLocation.getFileObject();
            sourceClsName = srcClassPath.getResourceName(fileObj, '.', false);
            String msgKey = !fileObj.isFolder()
                            ? "MSG_test_class_not_found"                //NOI18N
                            : (sourceClsName.length() != 0)
                              ? "MSG_testsuite_class_not_found"         //NOI18N
                              : "MSG_testsuite_class_not_found_def_pkg";//NOI18N
            callback.foundLocation(currLocation.getFileObject(), 
                    new LocationResult(NbBundle.getMessage(getClass(), msgKey, sourceClsName)));
        }
    }
    
    /**
     * Checks whether this action should be enabled for &quot;Go To Test&quot;
     * or for &quot;Go To Tested Class&quot or whether it should be disabled.
     * 
     * @return  {@code Boolean.TRUE} if this action should be enabled for
     *          &quot;Go To Test&quot;,<br />
     *          {@code Boolean.FALSE} if this action should be enabled for
     *          &quot;Go To Tested Class&quot;,<br />
     *          {@code null} if this action should be disabled
     */
    private Boolean checkDirection(FileObject fileObj) {
        ClassPath srcCP;
        FileObject fileObjRoot;
        
        boolean isJavaFile = false;
        boolean sourceToTest = true;
        boolean enabled = (fileObj != null)
          && (fileObj.isFolder() || (isJavaFile = TestUtil.isJavaFile(fileObj)))
          && ((srcCP = ClassPath.getClassPath(fileObj, ClassPath.SOURCE)) != null)
          && ((fileObjRoot = srcCP.findOwnerRoot(fileObj)) != null)
          && ((UnitTestForSourceQuery.findUnitTests(fileObjRoot).length != 0)
              || (sourceToTest = false)         //side effect - assignment
              || isJavaFile && (UnitTestForSourceQuery.findSources(fileObjRoot).length != 0));
        
        return enabled ? Boolean.valueOf(sourceToTest)
                       : null;
    }
    
    public boolean appliesTo(FileObject fo) {
        return TestUtil.isJavaFile(fo);
    }

    public FileType getFileType(FileObject fo) {
        Boolean b = checkDirection(fo);
        
        if (b == null) {
            return FileType.NEITHER;
        } else if (b.booleanValue()) {
            return FileType.TESTED;
        } else {
            return FileType.TEST;
        }
    }
}
