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

package org.netbeans.modules.j2ee.deployment.plugins.spi;


import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Profile;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.openide.util.Lookup;

/**
 * Base SPI interface for J2eePlatform. The J2eePlatform describes the target
 * environment J2EE applications are build against and subsequently deployed to.
 * Each server instance defines its own J2EE platform.
 *
 * @author Stepan Herold
 * @since 1.5
 */
public abstract class J2eePlatformImpl {
    
    /** Display name property */
    public static final String PROP_DISPLAY_NAME = "displayName";       //NOI18N
    /** Libraries property */
    public static final String PROP_LIBRARIES = "libraries";            //NOI18N
    /** Platform roots property */
    public static final String PROP_PLATFORM_ROOTS = "platformRoots";   //NOI18N
    
    private PropertyChangeSupport supp;
    
    /**
     * Return platform's libraries.
     *
     * @return platform's libraries.
     */
    public abstract LibraryImplementation[] getLibraries();
    
    /**
     * Return platform's display name.
     *
     * @return platform's display name.
     */
    public abstract String getDisplayName();
    
    /**
     * Return an icon describing the platform. This will be mostly the icon
     * used for server instance nodes 
     * 
     * @return an icon describing the platform
     * @since 1.6
     */
    public abstract Image getIcon();
    
    /**
     * Return platform's root directories. This will be mostly server's installation
     * directory.
     *
     * @return platform's root directories.
     */
    public abstract File[] getPlatformRoots();
    
    /**
     * Return classpath for the specified tool. Use the tool constants declared 
     * in the {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform}.
     *
     * @param  toolName tool name, for example {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform#TOOL_APP_CLIENT_RUNTIME}.
     * @return classpath for the specified tool.
     */
    public abstract File[] getToolClasspathEntries(String toolName);
    
    /**
     * Specifies whether a tool of the given name is supported by this platform.
     * Use the tool constants declared in the {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform}.
     *
     * @param  toolName tool name, for example {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform#TOOL_APP_CLIENT_RUNTIME}
     * .
     * @return <code>true</code> if platform supports tool of the given name, 
     *         <code>false</code> otherwise.
     * @deprecated {@link #getLookup()} should be used to obtain tool specifics
     */
    public abstract boolean isToolSupported(String toolName);
    
    /**
     * Return a list of supported J2EE specification versions. Use J2EE specification 
     * versions defined in the {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule}
     * class.
     *
     * @return list of supported J2EE specification versions.
     * @deprecated override {@link #getSupportedProfiles()} and {@link #getSupportedProfiles(java.lang.Object)}
     */
    public Set<String> getSupportedSpecVersions() {
        return Collections.emptySet();
    }
    
    /**
     * Return a list of supported J2EE specification versions for
     * a given module type.
     *
     * Implement this method if the server supports different versions
     * of spec for different types of modules.
     * If this method is not implemented by the plugin the IDE
     * will use the non parametrized version of
     * getSupportedSpecVersions.
     *
     * @param moduleType one of the constants defined in 
     *   {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule}
     * @return list of supported J2EE specification versions.
     * @deprecated override {@link #getSupportedProfiles()} and {@link #getSupportedProfiles(java.lang.Object)}
     */
    public Set <String> getSupportedSpecVersions(Object moduleType) {
        return getSupportedSpecVersions();
    }

    /**
     * Returns a set of supported profiles. By default method converts
     * specification version returned by {@link #getSupportedSpecVersions()}
     * to profiles.
     *
     * @return set of supported profiles
     * @see Profile
     * @since 1.58
     */
    public Set<Profile> getSupportedProfiles() {
        Set<Profile> set = new HashSet<Profile>();
        for (String spec : getSupportedSpecVersions()) {
            Profile profile = Profile.fromPropertiesString(spec);
            if (profile != null) {
                set.add(profile);
            }
        }
        return set;
    }

    /**
     * Returns a set of supported profiles for the given module type
     * (one of {@link J2eeModule#EAR}, {@link J2eeModule#EJB},
     * {@link J2eeModule#WAR}, {@link J2eeModule#RAR} and {@link J2eeModule#CAR}).
     * By default method converts specification version returned by
     * {@link #getSupportedSpecVersions(java.lang.Object)} to profiles.
     *
     * @param moduleType type of the module
     * @return set of supported profiles
     * @see Profile
     * @since 1.58
     */
    public Set<Profile> getSupportedProfiles(Object moduleType) {
        Set<Profile> set = new HashSet<Profile>();
        for (String spec : getSupportedSpecVersions(moduleType)) {
            Profile profile = Profile.fromPropertiesString(spec);
            if (profile != null) {
                set.add(profile);
            }
        }
        return set;
    }

    /**
     * Return a list of supported J2EE module types. Use module types defined in the 
     * {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule}
     * class.
     *
     * @return list of supported J2EE module types.
     */
    public abstract Set/*<Object>*/ getSupportedModuleTypes();
    
    /**
     * Return a set of J2SE platform versions this J2EE platform can run with.
     * Versions should be specified as strings i.g. ("1.3", "1.4", etc.)
     *
     * @since 1.9
     */
    public abstract Set/*<String>*/ getSupportedJavaPlatformVersions();
    
    /**
     * Return server J2SE platform or null if the platform is unknown, not 
     * registered in the IDE.
     *
     * @return server J2SE platform or null if the platform is unknown, not 
     *         registered in the IDE.
     *
     * @since 1.9
     */
    public abstract JavaPlatform getJavaPlatform();
    
    /**
     * Register a listener which will be notified when some of the platform's properties
     * change.
     * 
     * @param l listener which should be added.
     */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        synchronized (this) {
            if (supp == null)
                supp = new PropertyChangeSupport(this);
        }
        supp.addPropertyChangeListener(l);
    }
    
    /**
     * Remove a listener registered previously.
     *
     * @param l listener which should be removed.
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        if (supp != null)
            supp.removePropertyChangeListener(l);
    }
    

    /** 
     * Fire PropertyChange to all registered PropertyChangeListeners.
     *
     * @param propName property name.
     * @param oldValue old value.
     * @param newValue new value.
     */
    public final void firePropertyChange(String propName, Object oldValue, Object newValue) {
        if (supp != null)
            supp.firePropertyChange(propName, oldValue, newValue);
    }

    /**
     * Returns the property value for the specified tool.
     * <p>
     * The property value uses Ant property format and therefore may contain 
     * references to another properties defined either by the client of this API 
     * or by the tool itself.
     * <p>
     * The properties the client may be requited to define are as follows
     * {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform#CLIENT_PROP_DIST_ARCHIVE}
     * 
     * @param toolName tool name, for example {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform#TOOL_APP_CLIENT_RUNTIME}.
     * @param propertyName tool property name, for example {@link org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform#TOOL_PROP_MAIN_CLASS}.
     *
     * @return property value or null, if the property is not defined for the 
     *         specified tool.
     *         
     * @since 1.16
     * @deprecated {@link #getLookup()} should be used to obtain tool specifics
     */
    public String getToolProperty(String toolName, String propertyName) {
        return null;
    }
    
    /**
     * Lookup providing a way to find non mandatory technologies supported
     * by the platform.
     * <p>
     * <div class="nonnormative">
     * The typical example of such support is a webservice stack.
     * </div>
     *
     * @return Lookup providing way to find other supported technologies
     * @since 1.44
     */
    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

}
