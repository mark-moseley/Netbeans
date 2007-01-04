/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifactProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibrariesConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.MakeCustomizerProvider;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Represents one plain Make project.
 */
final class MakeProject implements Project, AntProjectListener {
    
    private static final Icon MAKE_PROJECT_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/makeProject.gif")); // NOI18N
        
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private ConfigurationDescriptorProvider projectDescriptorProvider;
    private int projectType = -1;
    
    MakeProject(AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = createEvaluator();
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, eval);
	projectDescriptorProvider = new ConfigurationDescriptorProvider(helper.getProjectDirectory());
        genFilesHelper = new GeneratedFilesHelper(helper);
        lookup = createLookup(aux);
        helper.addAntProjectListener(this);

	// Find the project type from project.xml
        Element data = helper.getPrimaryConfigurationData(true);
        NodeList nl = data.getElementsByTagName("make-project-type"); // NOI18N
        if (nl.getLength() == 1) {
	    nl = nl.item(0).getChildNodes();
	    String typeTxt = (String)nl.item(0).getNodeValue();
	    projectType = new Integer(typeTxt).intValue();
        }
    }

    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    public String toString() {
        return "MakeProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    private PropertyEvaluator createEvaluator() {
        // XXX might need to use a custom evaluator to handle active platform substitutions... TBD
        return helper.getStandardPropertyEvaluator();
    }
    
    PropertyEvaluator evaluator() {
        return eval;
    }

    ReferenceHelper getReferenceHelper () {
        return this.refHelper;
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }

    public Lookup getLookup() {
        return lookup;
    }

    private Lookup createLookup(AuxiliaryConfiguration aux) {
        SubprojectProvider spp = new MakeSubprojectProvider(); //refHelper.createSubprojectProvider();
        return Lookups.fixed(new Object[] {
            new Info(),
            aux,
            helper.createCacheDirectoryProvider(),
            spp,
            new MakeActionProvider( this, helper ),
            new MakeLogicalViewProvider(this, helper, evaluator(), spp, refHelper),
            new MakeCustomizerProvider(this, helper, evaluator(), refHelper, projectDescriptorProvider),
            new MakeArtifactProviderImpl(),
	    //new CustomActionsHookImpl(),
            new ProjectXmlSavedHookImpl(),
            new ProjectOpenedHookImpl(),
	    new MakeSharabilityQuery(FileUtil.toFile(getProjectDirectory())),
	    new MakeSources(this, helper),
            new AntProjectHelperProvider (),
	    projectDescriptorProvider,
            MobilityBridge.createBridge(this),
            new NativeProjectProvider(this, projectDescriptorProvider),
	    new RecommendedTemplatesImpl(),
            new MakeProjectOperations(this),
        });
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
        if (ev.getPath().equals(AntProjectHelper.PROJECT_XML_PATH)) {
            // Could be various kinds of changes, but name & displayName might have changed.
            Info info = (Info)getLookup().lookup(ProjectInformation.class);
            info.firePropertyChange(ProjectInformation.PROP_NAME);
            info.firePropertyChange(ProjectInformation.PROP_DISPLAY_NAME);
        }
    }

    public void propertiesChanged(AntProjectEvent ev) {
        // currently ignored (probably better to listen to evaluator() if you need to)
    }
    
    // Package private methods -------------------------------------------------
    
    final class AntProjectHelperProvider {
        AntProjectHelper getAntProjectHelper () {
            return helper;
        }
    }

    private static final class RecommendedTemplatesImpl
		    implements RecommendedTemplates, PrivilegedTemplates {

        private static final String[] RECOMMENDED_TYPES = new String[] { 
	    "c-types",         // NOI18N
            "cpp-types",       // NOI18N
            "shell-types",     // NOI18N
            "makefile-types",  // NOI18N
            "c-types",         // NOI18N
            "simple-files",    // NOI18N
	};
        
        private static final String[] RECOMMENDED_TYPES_FORTRAN = new String[] { 
	    "c-types",         // NOI18N
            "cpp-types",       // NOI18N
            "shell-types",     // NOI18N
            "makefile-types",  // NOI18N
            "c-types",         // NOI18N
            "simple-files",    // NOI18N
            "fortran-types",   // NOI18N
	};

        private static final String[] PRIVILEGED_NAMES = new String[] { 
            "Templates/cFiles/main.c",                                      // NOI18N
            "Templates/cFiles/file.c",                                      // NOI18N
            "Templates/cFiles/file.h",                                      // NOI18N
            "Templates/cppFiles/main.cc",                                   // NOI18N
            "Templates/cppFiles/file.cc",                                   // NOI18N
            "Templates/cppFiles/file.h",                                    // NOI18N
            "Templates/MakeTemplates/ComplexMakefile",			    // NOI18N
            "Templates/MakeTemplates/SimpleMakefile/ExecutableMakefile",    // NOI18N
            "Templates/MakeTemplates/SimpleMakefile/SharedLibMakefile",     // NOI18N
            "Templates/MakeTemplates/SimpleMakefile/StaticLibMakefile",     // NOI18N
	};

	public String[] getRecommendedTypes() {
            if (MakeOptions.getInstance().getFortran())
                return RECOMMENDED_TYPES_FORTRAN;
            else
                return RECOMMENDED_TYPES;
	}
        
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
    }
    
    /** Return configured project name. */
    public String getName() {
        return (String) ProjectManager.mutex().readAccess(new Mutex.Action() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                if (nl.getLength() == 1) {
                    nl = nl.item(0).getChildNodes();
                    if (nl.getLength() == 1 && nl.item(0).getNodeType() == Node.TEXT_NODE) {
                        return ((Text) nl.item(0)).getNodeValue();
                    }
                }
                return "???"; // NOI18N
            }
        });
    }
    
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action() {
            public Object run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name");
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }
    
    // Private innerclasses ----------------------------------------------------

    /*
    private class CustomActionsHookImpl implements CustomActionsHook {
	private Vector customActions = null;

	public CustomActionsHookImpl() {
	    customActions = new Vector();
	}

	public void addCustomAction(Action action) {
	    synchronized (customActions) {
		customActions.add(action);
	    }
	}

	public void removeCustomAction(Action action) {
	    synchronized (customActions) {
		customActions.add(action);
	    }
	}

	public Vector getCustomActions() {
	    return customActions;
	}
    }
    */

    private class MakeSubprojectProvider implements SubprojectProvider {
        // Add a listener to changes in the set of subprojects.
	public void addChangeListener(ChangeListener listener) {
	}

	// Get a set of projects which this project can be considered to depend upon somehow.
	public Set getSubprojects() {
	    Set subProjects = new HashSet();

	    ConfigurationDescriptor projectDescriptor = projectDescriptorProvider.getConfigurationDescriptor();
            if (projectDescriptor == null) {
                // Something serious wrong. Return nothing...
                return subProjects;
            }
	    Configuration[] confs = projectDescriptor.getConfs().getConfs();
	    for (int i = 0; i < confs.length; i++) {
		MakeConfiguration makeConfiguration = (MakeConfiguration)confs[i];
		LibrariesConfiguration librariesConfiguration = makeConfiguration.getLinkerConfiguration().getLibrariesConfiguration();
		LibraryItem[] libraryItems = librariesConfiguration.getLibraryItemsAsArray();
		for (int j = 0; j < libraryItems.length; j++) {
		    if (libraryItems[j] instanceof LibraryItem.ProjectItem) {
			LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem)libraryItems[j];
			Project project = projectItem.getProject(makeConfiguration.getBaseDir());
			if (project != null) {
			    subProjects.add(project);
			}
			else {
			    ; // FIXUP ERROR
			}
		    }
		}
	    }
	    return subProjects;
	}

	//Remove a listener to changes in the set of subprojects.
	public void removeChangeListener(ChangeListener listener)  {
	}
    }
    
    private final class Info implements ProjectInformation {
        
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        
        Info() {}
        
        void firePropertyChange(String prop) {
            pcs.firePropertyChange(prop, null, null);
        }
        
        public String getName() {
            String name = PropertyUtils.getUsablePropertyName(MakeProject.this.getName());
            return name;
        }
        
        public String getDisplayName() {
            String name = MakeProject.this.getName();
            return name;
        }
        
        public Icon getIcon() {
	    Icon icon = null;
	    icon = MakeConfigurationDescriptor.MAKEFILE_ICON;
	    // First 'projectType' (from project.xml)
	    /*
	    switch (projectType) {
	    case ProjectDescriptor.TYPE_APPLICATION :
		icon = NeoProjectDescriptor.MAKE_NEW_APP_ICON;
		break;
	    case ProjectDescriptor.TYPE_DYNAMIC_LIB :
		icon = NeoProjectDescriptor.MAKE_NEW_LIB_ICON;
		break;
	    case ProjectDescriptor.TYPE_MAKEFILE :
		icon = MakeProjectDescriptor.MAKE_EXT_APP_ICON;
		break;
	    case ProjectDescriptor.TYPE_STATIC_LIB :
		icon = MakeProjectDescriptor.MAKE_EXT_LIB_ICON;
		break;
	    };

	    // Then lookup the projectDescriptor and get it from there
	    if (icon == null) {
		ProjectDescriptorProvider pdp = (ProjectDescriptorProvider)getLookup().lookup(ProjectDescriptorProvider.class);
		if (pdp != null) {
		    icon = pdp.getProjectDescriptor().getIcon();
		    projectType = pdp.getProjectDescriptor().getProjectType();
		}
	    }

	    // Then ...
	    if (icon == null) {
		icon = MAKE_PROJECT_ICON;
		System.err.println("Cannot recognize make project type!"); // NOI18N
	    }
	    */
            return icon;
        }
        
        public Project getProject() {
            return MakeProject.this;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
        
    }
    
    private final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {
        
        ProjectXmlSavedHookImpl() {}
        
        protected void projectXmlSaved() throws IOException {
	    /*
            genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                MakeProject.class.getResource("resources/build-impl.xsl"),
                false);
            genFilesHelper.refreshBuildScript(
                GeneratedFilesHelper.BUILD_XML_PATH,
                MakeProject.class.getResource("resources/build.xsl"),
                false);
	    */
        }
        
    }
    
    private final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
        }
        
        protected void projectClosed() {
            if (projectDescriptorProvider.getConfigurationDescriptor() != null)
                projectDescriptorProvider.getConfigurationDescriptor().save(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/Bundle").getString("ProjectNotSaved"));
        }
    }
    
    private final class MakeArtifactProviderImpl implements MakeArtifactProvider {

        public MakeArtifact[] getBuildArtifacts() {
	    Vector artifacts = new Vector();
	    
	    MakeConfigurationDescriptor projectDescriptor = (MakeConfigurationDescriptor)projectDescriptorProvider.getConfigurationDescriptor();
	    Configuration[] confs = projectDescriptor.getConfs().getConfs();

	    String projectLocation = null;
	    int configurationType = 0;
	    String configurationName = null;
	    boolean active = false;;
	    String workingDirectory = null;
	    String buildCommand = null;
	    String cleanCommand = null;
	    String output = null;
	    
	    projectLocation = FileUtil.toFile(helper.getProjectDirectory()).getPath();
	    for (int i = 0; i < confs.length; i++) {
		MakeConfiguration makeConfiguration = (MakeConfiguration)confs[i];
		artifacts.add(new MakeArtifact(projectDescriptor, makeConfiguration));
	    }
	    return (MakeArtifact[]) artifacts.toArray(new MakeArtifact[artifacts.size()]);
        }
    }
    
}
