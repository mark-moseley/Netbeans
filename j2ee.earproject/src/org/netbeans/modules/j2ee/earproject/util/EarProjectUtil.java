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
package org.netbeans.modules.j2ee.earproject.util;

import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;

/**
 * Common utilities for Enterprise project.
 * This is a helper class; all methods are static.
 * @author Tomas Mysik
 */
public final class EarProjectUtil {
    
    private static final Logger UI_LOGGER = Logger.getLogger("org.netbeans.ui.j2ee.earproject"); // NOI18N
    private static final Logger USG_LOGGER = Logger.getLogger("org.netbeans.ui.metrics.j2ee.earproject"); // NOI18N    

    private EarProjectUtil() {}

    /**
     * Return <code>true</code> if deployment descriptor is compulsory for given EAR project.
     * @param earProject EAR project instance, shall include EarImplementation in it's lookup.
     * @return <code>true</code> if deployment descriptor is compulsory for given EAR project.
     * @see #isDDCompulsory(String)
     */
    public static boolean isDDCompulsory(Project earProject) {
        assert earProject != null;
        //#118047 avoid using the EarProject instance directly to allow for alternate implementations.
        EarImplementation impl = earProject.getLookup().lookup(EarImplementation.class);
        if (impl != null) {
            return isDDCompulsory(impl.getJ2eePlatformVersion());
        }
        return isDDCompulsory(J2eeModule.J2EE_14);
    }

    /**
     * Return <code>true</code> if deployment descriptor is compulsory for enterprise application
     * with given Java EE (or J2EE) version (typically applies for J2EE 1.3 or 1.4).
     * <p>
     * For possible JAVA EE versions see {@link J2eeModule J2eeModule constants}.
     * @param j2eeVersion Java EE (or J2EE) version.
     * @return <code>true</code> if deployment descriptor is compulsory.
     * @see J2eeModule
     */
    public static boolean isDDCompulsory(String j2eeVersion) {
        // #103298
        if (j2eeVersion == null) {
            // what should we return?
            return false;
        }
        if (J2eeModule.J2EE_13.equals(j2eeVersion)
                || J2eeModule.J2EE_14.equals(j2eeVersion)) {
            return true;
        } else if (J2eeModule.JAVA_EE_5.equals(j2eeVersion)) {
            return false;
        }
        assert false : "Unknown j2eeVersion: " + j2eeVersion;
        return true;
    }
    
    /**
     * Return <code>true</code> if deployment descriptor exists on the filesystem.
     * <p>
     * This method is useful if we want to write changes to the <i>application.xml</i> file.
     * @param earProject EAR project instance.
     * @return <code>true</code> if deployment descriptor exists on the filesystem for given EAR project.
     * @see org.netbeans.modules.j2ee.earproject.ProjectEar#getDeploymentDescriptor()
     */
    public static boolean isDDWritable(EarProject earProject) {
        return (earProject.getAppModule().getDeploymentDescriptor() != null);
    }
    
    /**
     * Check that the given String is neither <code>null</code> nor of length 0.
     * @param str input String.
     * @return <code>true</code> if input string contains any characters.
     */
    public static boolean hasLength(String str) {
        return str != null && str.length() > 0;
    }
    
    /**
     * Logs the UI gesture.
     *
     * @param bundle resource bundle to use for message
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUI(ResourceBundle bundle,String message, Object[] params) {
        Parameters.notNull("message", message);
        Parameters.notNull("bundle", bundle);

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(UI_LOGGER.getName());
        logRecord.setResourceBundle(bundle);
        if (params != null) {
            logRecord.setParameters(params);
        }
        UI_LOGGER.log(logRecord);
    }    
    
    /**
     * Logs feature usage.
     *
     * @param srcClass source class
     * @param message message key
     * @param params message parameters, may be <code>null</code>
     */
    public static void logUsage(Class srcClass, String message, Object[] params) {
        Parameters.notNull("message", message);

        LogRecord logRecord = new LogRecord(Level.INFO, message);
        logRecord.setLoggerName(USG_LOGGER.getName());
        logRecord.setResourceBundle(NbBundle.getBundle(srcClass));
        logRecord.setResourceBundleName(srcClass.getPackage().getName() + ".Bundle"); // NOI18N
        if (params != null) {
            logRecord.setParameters(params);
        }
        USG_LOGGER.log(logRecord);
    }        
}
