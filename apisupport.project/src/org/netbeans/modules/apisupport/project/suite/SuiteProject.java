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

package org.netbeans.modules.apisupport.project.suite;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.queries.FileEncodingQueryImpl;
import org.netbeans.modules.apisupport.project.queries.TemplateAttributesProvider;
import org.netbeans.modules.apisupport.project.ui.SuiteActions;
import org.netbeans.modules.apisupport.project.ui.SuiteLogicalView;
import org.netbeans.modules.apisupport.project.ui.SuiteOperations;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteCustomizer;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.LookupProviderSupport;
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
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;

/**
 * Represents one module suite project.
 * @author Jesse Glick
 */
public final class SuiteProject implements Project {
    
    public static final String SUITE_ICON_PATH =
            "org/netbeans/modules/apisupport/project/suite/resources/suite.png"; // NOI18N
    
    private final AntProjectHelper helper;
    private Lookup lookup;
    private final PropertyEvaluator eval;
    private final GeneratedFilesHelper genFilesHelper;
    
    public SuiteProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = createEvaluator();
        genFilesHelper = new GeneratedFilesHelper(helper);
        Util.err.log("Loading suite project in " + getProjectDirectory());
        lookup = Lookups.fixed(
            this, 
            new Info(),
            helper.createAuxiliaryConfiguration(),
            helper.createCacheDirectoryProvider(),
            new SavedHook(),
            UILookupMergerSupport.createProjectOpenHookMerger(new OpenedHook()),
            helper.createSharabilityQuery(eval, new String[0], new String[] {"build", "${dist.dir}"}), // NOI18N
            new SuiteSubprojectProviderImpl(helper, eval),
            new SuiteProviderImpl(),
            new SuiteActions(this),
            new SuiteLogicalView(this),
            new SuiteCustomizer(this, helper, eval),
            new PrivilegedTemplatesImpl(),
            new SuiteOperations(this),
            new TemplateAttributesProvider(helper, false),
            new FileEncodingQueryImpl());
        lookup = LookupProviderSupport.createCompositeLookup(lookup, "Projects/org-netbeans-modules-apisupport-project-suite/Lookup");
    }
    
    public @Override String toString() {
        return "SuiteProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }
    
    public File getProjectDirectoryFile() {
        return FileUtil.toFile(getProjectDirectory());
    }
    
    /** For unit tests purpose only. */
    public AntProjectHelper getHelper() {
        return helper;
    }
    
    /** For unit tests purpose only. */
    public PropertyEvaluator getEvaluator() {
        return eval;
    }

    public File getTestUserDirLockFile() {
        return getHelper().resolveFile(getEvaluator().evaluate("${test.user.dir}/lock"));
    }
    
    /**
     * Get the platform selected for use with this suite.
     * @param fallback if true, fall back to the default platform if necessary
     * @return the current platform; or null if fallback is false and there is no
     *         platform specified, or an invalid platform is specified, or even if
     *         fallback is true but even the default platform is not available
     */
    public NbPlatform getPlatform(boolean fallback) {
        NbPlatform p;
        // #65652: more reliable to use the dest dir, in case nbplatform.active is not set.
        String destdir = getEvaluator().getProperty("netbeans.dest.dir"); // NOI18N
        if (destdir != null) {
            p = NbPlatform.getPlatformByDestDir(getHelper().resolveFile(destdir));
        } else {
            p = null;
        }
        if (fallback && (p == null || !p.isValid())) {
            p = NbPlatform.getDefaultPlatform();
        }
        return p;
    }
    
    private PropertyEvaluator createEvaluator() {
        PropertyProvider predefs = helper.getStockPropertyPreprovider();
        File dir = getProjectDirectoryFile();
        List<PropertyProvider> providers = new ArrayList<PropertyProvider>();
        providers.add(helper.getPropertyProvider("nbproject/private/platform-private.properties")); // NOI18N
        providers.add(helper.getPropertyProvider("nbproject/platform.properties")); // NOI18N
        PropertyEvaluator baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, providers.toArray(new PropertyProvider[providers.size()]));
        providers.add(new Util.UserPropertiesFileProvider(baseEval, dir));
        baseEval = PropertyUtils.sequentialPropertyEvaluator(predefs, providers.toArray(new PropertyProvider[providers.size()]));
        class DestDirProvider extends Util.ComputedPropertyProvider {
            public DestDirProvider(PropertyEvaluator eval) {
                super(eval);
            }
            protected Map<String,String> getProperties(Map<String,String> inputPropertyValues) {
                String platformS = inputPropertyValues.get("nbplatform.active"); // NOI18N
                if (platformS != null) {
                    return Collections.singletonMap("netbeans.dest.dir", "${nbplatform." + platformS + ".netbeans.dest.dir}"); // NOI18N
                } else {
                    return Collections.emptyMap();
                }
            }
            protected Set<String> inputProperties() {
                return Collections.singleton("nbplatform.active"); // NOI18N
            }
        }
        providers.add(new DestDirProvider(baseEval));
        providers.add(helper.getPropertyProvider(AntProjectHelper.PRIVATE_PROPERTIES_PATH));
        providers.add(helper.getPropertyProvider(AntProjectHelper.PROJECT_PROPERTIES_PATH));
        Map<String,String> fixedProps = new HashMap<String,String>();
        // synchronize with suite.xml
        fixedProps.put(SuiteProperties.ENABLED_CLUSTERS_PROPERTY, "");
        fixedProps.put(SuiteProperties.DISABLED_CLUSTERS_PROPERTY, "");
        fixedProps.put(SuiteProperties.DISABLED_MODULES_PROPERTY, "");
        fixedProps.put(BrandingSupport.BRANDING_DIR_PROPERTY, "branding"); // NOI18N
        fixedProps.put("dist.dir", "dist"); // NOI18N
        fixedProps.put("test.user.dir", "build/testuserdir"); // NOI18N
        providers.add(PropertyUtils.fixedPropertyProvider(fixedProps));
        return PropertyUtils.sequentialPropertyEvaluator(predefs, providers.toArray(new PropertyProvider[providers.size()]));
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
            return new ImageIcon(ImageUtilities.loadImage(SUITE_ICON_PATH));
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
    
    /** For access from tests. */
    public void open() {
        // XXX skip this in case nbplatform.active is not defined
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
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
    private final class OpenedHook extends ProjectOpenedHook {
        OpenedHook() {}
        public void projectOpened() {
            open();
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
            return getProjectDirectoryFile();
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
