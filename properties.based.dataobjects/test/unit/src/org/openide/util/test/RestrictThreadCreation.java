/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.openide.util.test;

import java.security.Permission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Permits unit tests to limit creation of threads.
 * Unexpected thread creation can make tests fail randomly, which makes debugging difficult.
 * <p>Start off calling {@link #permitStandard} and {@link #forbidNewThreads}.
 * To determine which methods to permit, just try running the test;
 * if you see any {@link SecurityException}s which look like harmless thread creation
 * activities, just copy the appropriate part of the stack trace and pass to {@link #permit}.
 * <p>Use non-strict mode for {@link #forbidNewThreads} if you suspect some code
 * might be catching and not reporting {@link SecurityException}s; or you may prefer
 * to simply use non-strict mode while developing the test and then switch to strict
 * mode once it begins passing.
 * <p>Place calls to this class early in your test's initialization, e.g. in a static block.
 * (Not suitable for use from {@link junit.framework.TestCase#setUp}.)
 */
public class RestrictThreadCreation {

    private RestrictThreadCreation() {}

    private static Set<String> currentlyPermitted = new HashSet<String>();

    /**
     * Explicitly permits one or more thread creation idioms.
     * Each entry is of the form <samp>fully.qualified.Clazz.methodName</samp>
     * and if such a method can be found on the call stack the thread creation
     * is permitted.
     * @param permitted one or more fully qualified method names to accept
     */
    public static void permit(String... permitted) {
        currentlyPermitted.addAll(Arrays.asList(permitted));
    }

    /**
     * Permits a standard set of thread creation idioms which are normally harmless to unit tests.
     * Feel free to add to this list if it seems appropriate.
     */
    public static void permitStandard() {
        permit(// Found experimentally:
                "sun.java2d.Disposer.<clinit>",
                "java.awt.Toolkit.getDefaultToolkit",
                "java.util.logging.LogManager$Cleaner.<init>",
                "org.netbeans.Stamps$Worker.<init>",
                "org.netbeans.core.startup.Splash$SplashComponent.setText",
                "org.netbeans.core.startup.preferences.NbPreferences.<clinit>",
                "org.openide.loaders.FolderInstance.waitFinished",
                "org.openide.loaders.FolderInstance.postCreationTask",
                "org.netbeans.modules.java.source.usages.RepositoryUpdater.getDefault",
                "org.netbeans.modules.masterfs.filebasedfs.fileobjects.LockForFile.<clinit>",
                "org.netbeans.api.java.source.JavaSource.<clinit>",
                "org.netbeans.api.java.source.JavaSourceTaskFactory.fileObjectsChanged",
                "org.netbeans.progress.module.Controller.resetTimer",
                "org.netbeans.modules.timers.InstanceWatcher$FinalizingToken.finalize",
                "org.openide.util.RequestProcessor$EnqueueTask.run",
                "org.openide.util.Utilities$ActiveQueue.ping",
                "javax.swing.JComponent.revalidate",
                "javax.swing.ImageIcon.<init>");
    }

    /**
     * Install a security manager which prevents new threads from being created
     * unless they were explicitly permitted.
     * @param strict if true, throw a security exception; if false, just print stack traces
     */
    public static void forbidNewThreads(final boolean strict) {
        System.setSecurityManager(new SecurityManager() {
            public @Override void checkAccess(ThreadGroup g) {
                boolean inThreadInit = false;
                for (StackTraceElement line : Thread.currentThread().getStackTrace()) {
                    String id = line.getClassName() + "." + line.getMethodName();
                    if (currentlyPermitted.contains(id)) {
                        return;
                    } else if (id.equals("java.lang.Thread.init")) {
                        inThreadInit = true;
                    }
                }
                if (inThreadInit) {
                    SecurityException x = new SecurityException("Unauthorized thread creation");
                    if (strict) {
                        throw x;
                    } else {
                        x.printStackTrace();
                    }
                }
            }
            public @Override void checkPermission(Permission perm) {}
            public @Override void checkPermission(Permission perm, Object context) {}
        });
    }

}
