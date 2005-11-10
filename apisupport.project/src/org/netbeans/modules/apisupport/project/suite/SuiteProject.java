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

package org.netbeans.modules.apisupport.project.suite;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.ui.SuiteActions;
import org.netbeans.modules.apisupport.project.ui.SuiteLogicalView;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteCustomizer;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
/**
 * Represents one module suite project.
 * @author Jesse Glick
 */
public final class SuiteProject implements Project {
    
    public static final String SUITE_ICON_PATH =
            "org/netbeans/modules/apisupport/project/suite/resources/suite.gif"; // NOI18N
    
    private final AntProjectHelper helper;
    private final Lookup lookup;
    private final PropertyEvaluator eval;
    private final GeneratedFilesHelper genFilesHelper;
    
    public SuiteProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = createEvaluator();
        genFilesHelper = new GeneratedFilesHelper(helper);
        Util.err.log("Loading suite project in " + getProjectDirectory());
        lookup = Lookups.fixed(new Object[] {
            new Info(),
            helper.createAuxiliaryConfiguration(),
            helper.createCacheDirectoryProvider(),
            new SavedHook(),
            new OpenedHook(),
            helper.createSharabilityQuery(eval, new String[0], new String[] {"build", "dist"}), // NOI18N
            new SuiteSubprojectProviderImpl(this, helper, eval),
            new SuiteProviderImpl(),
            new SuiteActions(this),
            new SuiteLogicalView(this),
            new SuiteCustomizer(this, helper, eval),
            new PrivilegedTemplatesImpl()
        });
    }
    
    public String toString() {
        return "SuiteProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    /** For unit tests purpose only. */
    public AntProjectHelper getHelper() {
        return helper;
    }
    
    /** For unit tests purpose only. */
    public PropertyEvaluator getEvaluator() {
        return eval;
    }
    
    /**
     * Get the platform selected for use with this suite.
     * May return null in case the dest dir is unset.
     */
    public NbPlatform getPlatform() {
        // #65652: more reliable to use the dest dir, in case nbplatform.active is not set.
        String destdir = getEvaluator().getProperty("netbeans.dest.dir"); // NOI18N
        if (destdir == null) {
            return null;
        }
        return NbPlatform.getPlatformByDestDir(getHelper().resolveFile(destdir));
    }
    
    private PropertyEvaluator createEvaluator() {
        PropertyProvider predefs = helper.getStockPropertyPreprovider();
        File dir = FileUtil.toFile(getProjectDirectory());
        List/*<PropertyProvider>*/ providers = new ArrayList();
        // XXX listen to changes
        providers.add(helper.getPropertyProvider("nbproject/private/platform-private.properties")); // NOI18N
        providers.add(helper.getPropertyProvider("nbproject/platform.properties")); // NOI18N
        PropertyEvaluator baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
        String buildS = baseEval.getProperty("user.properties.file"); // NOI18N
        if (buildS != null) {
            providers.add(PropertyUtils.propertiesFilePropertyProvider(PropertyUtils.resolveFile(dir, buildS)));
        } else {
            providers.add(PropertyUtils.globalPropertyProvider());
        }
        baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
        class DestDirProvider implements PropertyProvider, PropertyChangeListener {
            private final PropertyEvaluator eval;
            private final List/*<ChangeListener>*/ listeners = new ArrayList();
            public DestDirProvider(PropertyEvaluator eval) {
                this.eval = eval;
                eval.addPropertyChangeListener(WeakListeners.propertyChange(this, eval));
            }
            public Map getProperties() {
                String platformS = eval.getProperty("nbplatform.active"); // NOI18N
                if (platformS != null) {
                    return Collections.singletonMap("netbeans.dest.dir", "${nbplatform." + platformS + ".netbeans.dest.dir}"); // NOI18N
                } else {
                    return Collections.EMPTY_MAP;
                }
            }
            public void addChangeListener(ChangeListener l) {
                synchronized (listeners) {
                    listeners.add(l);
                }
            }
            public void removeChangeListener(ChangeListener l) {
                synchronized (listeners) {
                    listeners.remove(l);
                }
            }
            public void propertyChange(PropertyChangeEvent evt) {
                ChangeEvent ev = new ChangeEvent(this);
                Iterator it;
                synchronized (listeners) {
                    it = new HashSet(listeners).iterator();
                }
                while (it.hasNext()) {
                    ((ChangeListener) it.next()).stateChanged(ev);
                }
            }
        }
        providers.add(new DestDirProvider(baseEval));
        providers.add(helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        providers.add(helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
        Map/*<String,String>*/ fixedProps = new HashMap();
        // synchronize with suite.xml
        fixedProps.put("disabled.clusters", ""); // NOI18N
        fixedProps.put("disabled.modules", ""); // NOI18N
        providers.add(PropertyUtils.fixedPropertyProvider(fixedProps));
        return PropertyUtils.sequentialPropertyEvaluator(predefs, (PropertyProvider[]) providers.toArray(new PropertyProvider[providers.size()]));
    }
    
    private final class Info implements ProjectInformation, AntProjectListener {
        
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        Info() {
            helper.addAntProjectListener(this);
        }
        
        private String getSimpleName() {
            Element nameEl = Util.findElement(helper.getPrimaryConfigurationData(true), "name", SuiteProjectType.NAMESPACE_SHARED); // NOI18N
            String text = (nameEl != null) ? Util.findText(nameEl) : null;
            return (text != null) ? text : "???"; // NOI18N
        }
        
        public String getName() {
            return PropertyUtils.getUsablePropertyName(getSimpleName());
        }

        public String getDisplayName() {
            String appTitle = getEvaluator().getProperty("app.title"); // NOI18N
            if (appTitle != null) {
                return appTitle;
            } else {
                return getSimpleName();
            }
        }

        public Icon getIcon() {
            return new ImageIcon(Utilities.loadImage(SUITE_ICON_PATH));
        }
        
        public Project getProject() {
            return SuiteProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        public void configurationXmlChanged(AntProjectEvent ev) {
            fireNameChange();
        }

        public void propertiesChanged(AntProjectEvent ev) {
            fireNameChange();
        }

        private void fireNameChange() {
            pcs.firePropertyChange(ProjectInformation.PROP_NAME, null, getName());
            pcs.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME, null, getDisplayName());
        }
        
    }
    
    final class OpenedHook extends ProjectOpenedHook {
        
        OpenedHook() {}
        
        protected void projectOpened() {
            // XXX skip this in case nbplatform.active is not defined
            ProjectManager.mutex().writeAccess(new Mutex.Action() {
                public Object run() {
                    String path = "nbproject/private/platform-private.properties"; // NOI18N
                    EditableProperties ep = helper.getProperties(path);
                    File buildProperties = new File(System.getProperty("netbeans.user"), "build.properties"); // NOI18N
                    ep.setProperty("user.properties.file", buildProperties.getAbsolutePath()); //NOI18N
                    helper.putProperties(path, ep);
                    try {
                        ProjectManager.getDefault().saveProject(SuiteProject.this);
                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                    return null;
                }
            });
            // refresh build.xml and build-impl.xml
            try {
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    SuiteProject.class.getResource("resources/build-impl.xsl"),
                    true);
                genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_XML_PATH,
                    SuiteProject.class.getResource("resources/build.xsl"),
                    true);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        
        protected void projectClosed() {
            try {
                ProjectManager.getDefault().saveProject(SuiteProject.this);
            } catch (IOException e) {
                Util.err.notify(e);
            }
        }
        
    }
    
    private final class SavedHook extends ProjectXmlSavedHook {
        
        SavedHook() {}
        
        protected void projectXmlSaved() throws IOException {
            // refresh build.xml and build-impl.xml
            genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                SuiteProject.class.getResource("resources/build-impl.xsl"),
                false);
            genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_XML_PATH,
                SuiteProject.class.getResource("resources/build.xsl"),
                false);
        }
        
    }
    
    private final class SuiteProviderImpl implements SuiteProvider {
        
        public File getSuiteDirectory() {
            return FileUtil.toFile(getProjectDirectory());
        }
        
    }

    private static final class PrivilegedTemplatesImpl implements PrivilegedTemplates, RecommendedTemplates {
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/Ant/Project.xml", // NOI18N
            "Templates/Other/properties.properties", // NOI18N
        };
        
        private static final String[] RECOMMENDED_TYPES = new String[] {         
            "oasis-XML-catalogs",   // NOI18N
            "XML",                  // NOI18N
            "ant-script",           // NOI18N
            "simple-files",         // NOI18N
        };
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }

        public String[] getRecommendedTypes() {
            return RECOMMENDED_TYPES;
        }
    }    
    
}
