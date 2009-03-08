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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.grails.api;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.settings.GrailsSettings;


/**
 * Represents IDE configuration of the Grails project.
 *
 * @author schmidtm, Petr Hejl
 */
// FIXME move this to project support
public final class GrailsProjectConfig {

    public static final String GRAILS_PORT_PROPERTY = "grails.port"; // NOI18N

    public static final String GRAILS_ENVIRONMENT_PROPERTY = "grails.environment"; // NOI18N

    public static final String GRAILS_DEPLOY_DIR_PROPERTY = "grails.deploy.dir"; // NOI18N

    public static final String GRAILS_AUTODEPLOY_PROPERTY = "grails.deploy.auto"; // NOI18N

    public static final String GRAILS_JAVA_PLATFORM_PROPERTY = "grails.java.platform"; // NOI18N

    public static final String GRAILS_DEBUG_BROWSER_PROPERTY = "grails.debug.browser"; // NOI18N

    public static final String GRAILS_DISPLAY_BROWSER_PROPERTY = "grails.display.browser"; // NOI18N

    private static final String DEFAULT_PORT = "8080"; // NOI18N

    private final Project prj;

    private final GrailsSettings settings = GrailsSettings.getInstance();

    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    //private static final Map<Project, GrailsProjectConfig> CONFIG_CACHE = new WeakHashMap<Project, GrailsProjectConfig>();

    private static final JavaPlatformManager PLATFORM_MANAGER  = JavaPlatformManager.getDefault();

    public GrailsProjectConfig(Project prj) {
        this.prj = prj;
    }

    /**
     * Returns the configuration of the given project.
     *
     * @param project project for which the returned configuration will serve
     * @return the configuration of the given project
     */
    public static synchronized GrailsProjectConfig forProject(Project project) {
        GrailsProjectConfig config = project.getLookup().lookup(GrailsProjectConfig.class);

        return config;

//        GrailsProjectConfig config = CONFIG_CACHE.get(project);
//        if (config == null) {
//            config = new GrailsProjectConfig(project);
//            CONFIG_CACHE.put(project, config);
//        }
//        return config;
    }

    /**
     * Returns the project for wich the configuration is used.
     *
     * @return the project for wich the configuration is used
     */
    public Project getProject() {
        return prj;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Returns the port configured for the project.
     *
     * @return the port configured for the project
     */
    public String getPort() {
        synchronized (settings) {
            String port = settings.getPortForProject(prj);
            if (port == null) {
                port = DEFAULT_PORT;
            }
            return port;
        }
    }

    /**
     * Sets the port for the project.
     *
     * @param port the port to set
     */
    public void setPort(String port) {
        assert port != null;
        String oldValue;
        synchronized (settings) {
            oldValue = getPort();
            settings.setPortForProject(prj, port);
        }
        propertyChangeSupport.firePropertyChange(GRAILS_PORT_PROPERTY, oldValue, port);
    }

    /**
     * Returns the environment configured for the project.
     *
     * @return the environment configured for the project or <code>null</code>
     *             if no environment has been configured yet
     */
    public GrailsEnvironment getEnvironment() {
        synchronized (settings) {
            return settings.getEnvForProject(prj);
        }
    }

    /**
     * Sets the environment for the project.
     *
     * @param env the environment to set
     */
    public void setEnvironment(GrailsEnvironment env) {
        assert env != null;
        GrailsEnvironment oldValue;
        synchronized (settings) {
            oldValue = getEnvironment();
            settings.setEnvForProject(prj, env);
        }
        propertyChangeSupport.firePropertyChange(GRAILS_ENVIRONMENT_PROPERTY, oldValue, env);
    }

    /**
     * Returns the deployment dir configured for the project.
     *
     * @return the deployment dir configured for the project or <code>null</code>
     *             if no deployment dir has been configured yet
     */
    public String getDeployDir() {
        synchronized (settings) {
            return settings.getDeployDirForProject(prj);
        }
    }

    /**
     * Sets the deployment dir for the project.
     *
     * @param dir deployemnt dir to set
     */
    public void setDeployDir(String dir) {
        assert dir != null;
        String oldValue;
        synchronized (settings) {
            oldValue = getDeployDir();
            settings.setDeployDirForProject(prj, dir);
        }
        propertyChangeSupport.firePropertyChange(GRAILS_DEPLOY_DIR_PROPERTY, oldValue, dir);
    }

    /**
     * Returns the autodeploy flag of the project.
     *
     * @return the autodeploy flag of the project
     */
    public boolean getAutoDeployFlag() {
        synchronized (settings) {
            return settings.getAutoDeployFlagForProject(prj);
        }
    }

    /**
     * Sets the autodeploy flag of the project.
     *
     * @param flag the autodeploy flag to set
     */
    public void setAutoDeployFlag(boolean flag) {
        boolean oldValue;
        synchronized (this) {
            oldValue = getAutoDeployFlag();
            settings.setAutoDeployFlagForProject(prj, flag);
        }
        propertyChangeSupport.firePropertyChange(GRAILS_AUTODEPLOY_PROPERTY, oldValue, flag);
    }

    /**
     * Returns the browser configured for the project.
     *
     * @return the browser configured for the project or <code>null</code>
     *             if no browser has been configured yet
     */
    public String getDebugBrowser() {
        synchronized (settings) {
            return settings.getDebugBrowserForProject(prj);
        }
    }

    /**
     * Sets the browser for the project.
     *
     * @param browser browser to set
     */
    public void setDebugBrowser(String browser) {
        assert browser != null;
        String oldValue;
        synchronized (settings) {
            oldValue = getDebugBrowser();
            settings.setDebugBrowserProject(prj, browser);
        }
        propertyChangeSupport.firePropertyChange(GRAILS_DEBUG_BROWSER_PROPERTY, oldValue, browser);
    }

    public JavaPlatform getJavaPlatform() {
        String platformId;
        synchronized (settings) {
            platformId = settings.getJavaPlatformForProject(prj);
        }

        if (platformId == null) {
            return JavaPlatform.getDefault();
        }

        JavaPlatform[] platforms = PLATFORM_MANAGER.getPlatforms(null, new Specification("j2se", null)); //NOI18N
        for (JavaPlatform platform : platforms) {
            if (platform.getInstallFolders().size() > 0) {
                String antName = platform.getProperties().get("platform.ant.name"); //NOI18N
                if (platformId.equals(antName)) {
                    return platform;
                }
            }
        }
        return JavaPlatform.getDefault();
    }

    public void setJavaPlatform(JavaPlatform platform) {
        assert platform != null;
        JavaPlatform oldValue;
        synchronized (settings) {
            oldValue = getJavaPlatform();
            settings.setJavaPlatformForProject(prj, platform.getProperties().get("platform.ant.name"));
        }
        propertyChangeSupport.firePropertyChange(GRAILS_JAVA_PLATFORM_PROPERTY, oldValue, platform);
    }

    public GrailsPlatform getGrailsPlatform() {
        GrailsPlatform runtime = GrailsPlatform.getDefault();
        if (runtime.isConfigured()) {
            return runtime;
        }
        return null;
    }

    /**
     * Returns the display browser flag of the project.
     *
     * @return the display browser flag of the project
     */
    public boolean getDisplayBrowser() {
        synchronized (settings) {
            return settings.getDisplayBrowserForProject(prj);
        }
    }

    /**
     * Sets the display browser flag of the project.
     *
     * @param displayBrowser display browser flag to set
     */
    public void setDisplayBrowser(boolean displayBrowser) {
        boolean oldValue;
        synchronized (this) {
            oldValue = getDisplayBrowser();
            settings.setDisplayBrowserForProject(prj, displayBrowser);
        }
        propertyChangeSupport.firePropertyChange(GRAILS_DISPLAY_BROWSER_PROPERTY, oldValue, displayBrowser);
    }

}
