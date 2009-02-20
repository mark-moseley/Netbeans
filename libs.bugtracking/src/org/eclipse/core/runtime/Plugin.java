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
package org.eclipse.core.runtime;

import java.io.File;
import java.util.logging.Level;
import org.netbeans.libs.bugtracking.BugtrackingRuntime;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * @author Maros Sandor
 */
public class Plugin implements ILog {
    private IPath stateLocation;

    public final Bundle getBundle() {
        return null;
    }
    
    public boolean isDebugging() {
        return false;
    }
    
    public void start(BundleContext context) throws Exception {
        
    }
    
    public void stop(BundleContext context) throws Exception {
        
    }
    
    public final ILog getLog() {
        return this;
    }
    
    public final IPath getStateLocation() throws IllegalStateException {
        if(stateLocation == null) {
            File f = new File(BugtrackingRuntime.getInstance().getCacheStore(), "statelocation");
            stateLocation = new StateLocation(f);
        }
        return stateLocation;
    }

    public void log(IStatus status) {
        Level l = null;
        switch (status.getSeverity()) {
            case IStatus.CANCEL:
                l = Level.OFF; break;
            case IStatus.ERROR:
                l = Level.SEVERE; break;
            case IStatus.INFO:
                l = Level.INFO; break;
            case IStatus.OK:
                l = Level.INFO; break;
            case IStatus.WARNING:
                l = Level.WARNING; break;
            default:
                l = Level.INFO;
        }
        BugtrackingRuntime.LOG.log(l, status.getMessage() + " code: " + status.getCode(), status.getException());
    }

    private class StateLocation implements IPath {
        private final File file;

        private StateLocation(File file) {
            this.file = file;
        }

        public IPath append(String path) {
            File f = new File(file, path);
            return new StateLocation(f);
        }

        public File toFile() {
            return file;
        }

    }

}
