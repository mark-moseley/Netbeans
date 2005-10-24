/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.java.platform.JavaPlatformProvider;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Tomas Zezula
 */
public final class TestPlatformProvider implements JavaPlatformProvider {

    private JavaPlatform defaultPlatform;
    private JavaPlatform explicitPlatform;
    private PropertyChangeSupport support;
    private boolean hideExplicitPlatform;

    public TestPlatformProvider (ClassPath defaultPlatformBootClassPath, ClassPath explicitPlatformBootClassPath) {
        this.support = new PropertyChangeSupport (this);
        this.defaultPlatform = new TestPlatform("default_platform", defaultPlatformBootClassPath);
        this.explicitPlatform = new TestPlatform("ExplicitPlatform", explicitPlatformBootClassPath);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    public JavaPlatform[] getInstalledPlatforms()  {
        if (this.hideExplicitPlatform) {
            return new JavaPlatform[] {
                this.defaultPlatform,
            };
        }
        else {
            return new JavaPlatform[] {
                this.defaultPlatform,
                this.explicitPlatform,
            };
        }
    }

    public JavaPlatform getDefaultPlatform () {            
        return this.defaultPlatform;
    }

    public void setExplicitPlatformVisible (boolean value) {
        this.hideExplicitPlatform = !value;
        this.support.firePropertyChange(PROP_INSTALLED_PLATFORMS,null,null);
    }

    private static class TestPlatform extends JavaPlatform {

        private String systemName;
        private Map properties;
        private ClassPath bootClassPath;

        public TestPlatform (String systemName, ClassPath bootCP) {
            this.systemName = systemName;
            this.bootClassPath = bootCP;
            this.properties = Collections.singletonMap("platform.ant.name",this.systemName);
        }

        public FileObject findTool(String toolName) {
            return null;
        }

        public String getVendor() {
            return "me";    
        }

        public ClassPath getStandardLibraries() {
            return null;
        }

        public Specification getSpecification() {
            return new Specification ("j2se", new SpecificationVersion ("1.5"));
        }

        public ClassPath getSourceFolders() {
            return null;
        }

        public Map getProperties() {
            return this.properties;
        }

        public List getJavadocFolders() {
            return null;
        }

        public Collection getInstallFolders() {
            return null;
        }

        public String getDisplayName() {
            return this.systemName;
        }

        public ClassPath getBootstrapLibraries() {
            return this.bootClassPath;
        }
    }
}
