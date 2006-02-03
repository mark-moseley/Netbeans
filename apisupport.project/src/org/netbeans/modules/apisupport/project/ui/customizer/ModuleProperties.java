/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Basic support for storing general module's properties.
 *
 * @author Martin Krauskopf
 */
public abstract class ModuleProperties {
    
    public static final String PROPERTIES_REFRESHED = "propertiesRefreshed"; // NOI18N
    
    static final RequestProcessor RP = new RequestProcessor(ModuleProperties.class.getName());
    
    // XXX better to inject own DialogDisplayer when running tests or similar trick
    static boolean runFromTests = false;
    
    // Helpers for storing and retrieving real values currently stored on the disk
    private AntProjectHelper helper;
    private PropertyEvaluator evaluator;
    
    /** Represent main module's properties (nbproject/project.properties). */
    private EditableProperties projectProperties;
    
    /** Represent module's private properties (nbproject/private/private.properties). */
    private EditableProperties privateProperties;
    
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    
    /** Creates a new instance of ModuleProperties */
    ModuleProperties(AntProjectHelper helper, PropertyEvaluator evaluator) {
        this.helper = helper;
        this.evaluator = evaluator;
        reloadProperties();
    }
    
    protected void reloadProperties() {
        this.projectProperties = helper.getProperties(
                AntProjectHelper.PROJECT_PROPERTIES_PATH);
        this.privateProperties = helper.getProperties(
                AntProjectHelper.PRIVATE_PROPERTIES_PATH);
    }
    
    /**
     * Returns map of keys form main module properties and their default values.
     * Must be overriden by a subclass.
     */
    abstract Map/*<String, String>*/ getDefaultValues();
    
    AntProjectHelper getHelper() {
        return helper;
    }
    
    PropertyEvaluator getEvaluator() {
        return evaluator;
    }
    
    EditableProperties getProjectProperties() {
        return projectProperties;
    }
    
    EditableProperties getPrivateProperties() {
        return privateProperties;
    }
    
    final String getProperty(String key) {
        String value = getProjectProperties().getProperty(key);
        return value != null ? value : (String) getDefaultValues().get(key);
    }
    
    final boolean getBooleanProperty(String key) {
        String bValue = getProperty(key);
        return bValue != null &&  (bValue.equalsIgnoreCase("true") || // NOI18N
                bValue.equalsIgnoreCase("yes")); // NOI18N
    }
    
    final String removeProperty(String key) {
        return (String) getProjectProperties().remove(key);
    }
    
    final String removePrivateProperty(String key) {
        return (String) getPrivateProperties().remove(key);
    }
    
    /**
     * The given property will be stored into the project's properties. If the
     * given value is equals to the default value it will be removed from the
     * properties.
     */
    final void setProperty(String key, String value) {
        String def = (String) getDefaultValues().get(key);
        if (def == null) {
            def = ""; // NOI18N
        }
        if (value == null || def.equals(value)) {
            getProjectProperties().remove(key);
        } else {
            getProjectProperties().setProperty(key, value);
        }
        firePropertyChange(key, null, value);
    }
    
    /**
     * The given property will be stored into the project's properties. If the
     * given value is equals to the default value it will be removed from the
     * properties.
     */
    final void setPrivateProperty(String key, String value) {
        String def = (String) getDefaultValues().get(key);
        if (def == null) {
            def = ""; // NOI18N
        }
        if (def.equals(value)) {
            getPrivateProperties().remove(key);
        } else {
            getPrivateProperties().setProperty(key, value);
        }
        firePropertyChange(key, null, value);
    }
    
    void setProperty(String key, String[] value) {
        getProjectProperties().setProperty(key, value);
        firePropertyChange(key, null, null);
    }
    
    final void setBooleanProperty(String key, boolean bProp) {
        setProperty(key, Boolean.toString(bProp));
    }
    
    String getProjectDisplayName() {
        return Util.getDisplayName(getHelper().getProjectDirectory());
    }
    
    final File getProjectDirectoryFile() {
        return FileUtil.toFile(getHelper().getProjectDirectory());
    }
    
    final String getProjectDirectory() {
        return getProjectDirectoryFile().getAbsolutePath();
    }
    
    /**
     * Stores cached properties. This is called when the user press <em>OK</em>
     * button in the properties customizer. If <em>Cancel</em> button is
     * pressed properties will not be saved. Be sure this method is called
     * whitin {@link ProjectManager#mutex}. However, you are well advised to
     * explicitly enclose a <em>complete</em> operation within write access to
     * prevent race conditions.
     */
    void storeProperties() throws IOException {
        // Store changes into in nbproject/project.properties
        getHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,
                getProjectProperties());
        // Store changes into in nbproject/private/private.properties
        getHelper().putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH,
                getPrivateProperties());
    }
    
    /**
     * Helper method for storing platform into the appropriate place.
     */
    static void storePlatform(AntProjectHelper helper, NbPlatform platform) {
        if (platform != null) {
            // store platform properties
            EditableProperties props = helper.getProperties(
                    NbModuleProjectGenerator.PLATFORM_PROPERTIES_PATH);
            props.put("nbplatform.active", platform.getID()); // NOI18N
            helper.putProperties(
                    NbModuleProjectGenerator.PLATFORM_PROPERTIES_PATH, props);
        }
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pchl) {
        changeSupport.addPropertyChangeListener(pchl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pchl) {
        changeSupport.removePropertyChangeListener(pchl);
    }
    
    protected void firePropertyChange(String propName, Object oldValue, Object newValue) {
        changeSupport.firePropertyChange(propName, oldValue, newValue);
    }
    
    protected void firePropertiesRefreshed() {
        firePropertyChange(PROPERTIES_REFRESHED, null, null);
    }
    
    protected static void reportLostPlatform(final NbPlatform lostPlatform) {
        String plafText = lostPlatform != null
                ? '"' + lostPlatform.getLabel() + '"'
                : NbBundle.getMessage(ModuleProperties.class, "MSG_PreviouslySet");
        String message = NbBundle.getMessage(ModuleProperties.class, "MSG_PlatformNotFound", plafText);
        if (!runFromTests) {
            DialogDisplayer.getDefault().notify(new DialogDescriptor.Message(message));
        } else {
            System.err.println(message);
        }
    }
    
    static String getPlatformID(JavaPlatform platform) {
        // XXX why isn't there a real API for this??
        return (String) platform.getProperties().get("platform.ant.name"); // NOI18N
    }
    
    static JavaPlatform findJavaPlatformByID(String id) {
        if (id == null || id.equals("default")) { // NOI18N
            return JavaPlatform.getDefault();
        }
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for (int i = 0; i < platforms.length; i++) {
            if (id.equals(getPlatformID(platforms[i]))) {
                return platforms[i];
            }
        }
        return null;
    }
    
    static void storeJavaPlatform(AntProjectHelper helper, PropertyEvaluator eval, JavaPlatform platform, boolean isNetBeansOrg) throws IOException {
        if (isNetBeansOrg) {
            final boolean isDefault = platform == null || platform == JavaPlatform.getDefault();
            final File home = isDefault ? null : getPlatformLocation(platform);
            if (home != null || isDefault) {
                final FileObject nbbuild = helper.resolveFileObject(eval.evaluate("${nb_all}/nbbuild")); // NOI18N
                if (nbbuild != null) {
                    try {
                        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                            public Object run() throws IOException {
                                FileObject userBuildProperties = nbbuild.getFileObject("user.build.properties"); // NOI18N
                                if (userBuildProperties == null) {
                                    userBuildProperties = nbbuild.createData("user.build.properties"); // NOI18N
                                }
                                EditableProperties ep = Util.loadProperties(userBuildProperties);
                                if (isDefault) {
                                    // Have to remove it; no default value.
                                    ep.remove("nbjdk.home");
                                } else {
                                    ep.setProperty("nbjdk.home", home.getAbsolutePath());
                                }
                                Util.storeProperties(userBuildProperties, ep);
                                return null;
                            }
                        });
                    } catch (MutexException e) {
                        throw (IOException) e.getException();
                    }
                }
            }
        } else {
            EditableProperties props = helper.getProperties(NbModuleProjectGenerator.PLATFORM_PROPERTIES_PATH);
            if (platform == null || platform == JavaPlatform.getDefault()) {
                if (props.containsKey("nbjdk.active")) { // NOI18N
                    // Could also just remove it, but probably nicer to set it explicitly to 'default'.
                    props.put("nbjdk.active", "default"); // NOI18N
                }
            } else {
                props.put("nbjdk.active", getPlatformID(platform)); // NOI18N
            }
            helper.putProperties(NbModuleProjectGenerator.PLATFORM_PROPERTIES_PATH, props);
        }
    }
    
    private static File getPlatformLocation(JavaPlatform platform) {
        Collection/*<FileObject>*/ installs = platform.getInstallFolders();
        if (installs.size() == 1) {
            return FileUtil.toFile((FileObject) installs.iterator().next());
        } else {
            return null;
        }
    }
    
    public static JavaPlatform findJavaPlatformByLocation(String home) {
        if (home == null) {
            return JavaPlatform.getDefault();
        }
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for (int i = 0; i < platforms.length; i++) {
            if (new File(home).equals(getPlatformLocation(platforms[i]))) {
                return platforms[i];
            }
        }
        return null;
    }
    
}
