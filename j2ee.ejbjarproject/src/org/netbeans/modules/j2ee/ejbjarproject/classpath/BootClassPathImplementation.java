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
package org.netbeans.modules.j2ee.ejbjarproject.classpath;

import java.beans.PropertyChangeEvent;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.classpath.ClassPath;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.WeakListeners;

final class BootClassPathImplementation implements ClassPathImplementation, PropertyChangeListener {

    private static final String PLATFORM_ACTIVE = "platform.active";        //NOI18N
    private static final String ANT_NAME = "platform.ant.name";             //NOI18N
    private static final String J2SE = "j2se";                              //NOI18N

    private final PropertyEvaluator evaluator;
    private JavaPlatformManager platformManager;
    //name of project active platform
    private String activePlatformName;
    //active platform is valid (not broken reference)
    private boolean isActivePlatformValid;
    private List resourcesCache;
    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    public BootClassPathImplementation(PropertyEvaluator evaluator) {
        assert evaluator != null;
        this.evaluator = evaluator;
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
    }

    public synchronized List /*<PathResourceImplementation>*/ getResources() {
        if (this.resourcesCache == null) {
            JavaPlatform jp = findActivePlatform ();
            if (jp != null) {
                //TODO: May also listen on CP, but from Platform it should be fixed.
                ClassPath cp = jp.getBootstrapLibraries();
                List entries = cp.entries();
                ArrayList result = new ArrayList (entries.size());
                for (Iterator it = entries.iterator(); it.hasNext();) {
                    ClassPath.Entry entry = (ClassPath.Entry) it.next();
                    result.add (ClassPathSupport.createResource(entry.getURL()));
                }
                resourcesCache = Collections.unmodifiableList (result);
            }
            else {
                resourcesCache = Collections.EMPTY_LIST;
            }
        }
        return this.resourcesCache;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }

    private JavaPlatform findActivePlatform () {
        if (this.platformManager == null) {
            this.platformManager = JavaPlatformManager.getDefault();
            this.platformManager.addPropertyChangeListener(WeakListeners.propertyChange(this, this.platformManager));
        }
        this.activePlatformName = evaluator.getProperty(PLATFORM_ACTIVE);
        if (activePlatformName!=null) {
            JavaPlatform[] installedPlatforms = this.platformManager.getInstalledPlatforms();
            for (int i = 0; i< installedPlatforms.length; i++) {
                Specification spec = installedPlatforms[i].getSpecification();
                String antName = (String) installedPlatforms[i].getProperties().get (ANT_NAME);
                if (J2SE.equalsIgnoreCase(spec.getName())
                    && activePlatformName.equals(antName)) {
                        this.isActivePlatformValid = true;
                        return installedPlatforms[i];
                }
            }
            //Platform not found, return the default platform and listen
            //on broken reference resolution
            this.isActivePlatformValid = false;
            return null; //this.platformManager.getDefaultPlatform ();
        } else {
            //Platform not set => default platform
            return this.platformManager.getDefaultPlatform();
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getSource() == this.evaluator && evt.getPropertyName().equals(PLATFORM_ACTIVE)) {
            //Active platform was changed
            resetCache ();
        }
        else if (evt.getSource() == this.platformManager && JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(evt.getPropertyName()) && activePlatformName != null) {
            //Platform definitions were changed, check if the platform was not resolved or deleted
            if (this.isActivePlatformValid) {
                JavaPlatform[] j2sePlatforms = this.platformManager.getPlatforms(null,new Specification("j2se",null)); //NOI18N
                boolean found = false;
                for (int i=0; i< j2sePlatforms.length; i++) {
                    String antName = (String) j2sePlatforms[i].getProperties().get("platform.ant.name");        //NOI18N
                    if (antName != null && antName.equals(this.activePlatformName)) {
                        found = true;
                    }
                }
                if (!found) {
                    //the platform was not removed
                    this.resetCache();
                }
            }
            else {
                JavaPlatform[] j2sePlatforms = this.platformManager.getPlatforms(null,new Specification("j2se",null)); //NOI18N
                for (int i=0; i< j2sePlatforms.length; i++) {
                    String antName = (String) j2sePlatforms[i].getProperties().get("platform.ant.name");        //NOI18N
                    if (antName != null && antName.equals(this.activePlatformName)) {
                        this.resetCache();
                        break;
                    }
                }
            }

        }
    }
    
    /**
     * Resets the cache and firesPropertyChange
     */
    private void resetCache () {
        synchronized (this) {
            resourcesCache = null;
        }
        support.firePropertyChange(PROP_RESOURCES, null, null);
    }
    
}
