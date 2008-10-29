/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.api;

import java.io.IOException;
import java.util.List;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.api.customizer.ModelHandle;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.Dependency;
import org.netbeans.modules.maven.model.pom.DependencyManagement;
import org.netbeans.modules.maven.model.pom.POMComponentFactory;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.POMModelFactory;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.model.pom.Project;
import org.netbeans.modules.maven.model.pom.Properties;
import org.netbeans.modules.maven.model.pom.Repository;
import org.netbeans.modules.maven.options.MavenVersionSettings;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;

/**
 * Various maven model related utilities.
 * @author mkleint
 * @author Anuradha G
 */
public final class ModelUtils {

    /**
     * 
     * @param pom       FileObject that represents POM
     * @param group     
     * @param artifact
     * @param version
     * @param type
     * @param scope
     * @param classifier
     * @param acceptNull accept null values to scope,type and classifier.
     *                   If true null values will remove corresponding tag.
     */
    public static void addDependency(FileObject pom,
            String group,
            String artifact,
            String version,
            String type,
            String scope,
            String classifier, boolean acceptNull) {
        ModelSource source = Utilities.createModelSource(pom, true);
        POMModel model = POMModelFactory.getDefault().getModel(source);
        model.startTransaction();
        try {
            Dependency dep = checkModelDependency(model, group, artifact, true);
            dep.setVersion(version);
            if (acceptNull || scope != null) {
                dep.setScope(scope);
            }
            if (acceptNull || type != null) {
                dep.setType(type);
            }
            if (acceptNull || classifier != null) {
                dep.setClassifier(classifier);
            }
            model.endTransaction();
        } finally {
            if (model.isIntransaction()) {
                model.rollbackTransaction();
            }
        }
        DataObject dobj = model.getModelSource().getLookup().lookup(DataObject.class);
        SaveCookie sc = dobj.getLookup().lookup(SaveCookie.class);
        if (sc != null) {
            try {
                sc.save();
            } catch (IOException ex) {
                //TODO report properly
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public static Dependency checkModelDependency(POMModel pom, String groupId, String artifactId, boolean add) {
        Project mdl = pom.getProject();
        List<Dependency> deps = mdl.getDependencies();
        Dependency ret = null;
        Dependency managed = null;
        if (deps != null) {
            for (Dependency d : deps) {
                if (groupId.equalsIgnoreCase(d.getGroupId()) && artifactId.equalsIgnoreCase(d.getArtifactId())) {
                    ret = d;
                    break;
                }
            }
        }
        if (ret == null || ret.getVersion() == null) {
            //check dependency management section as well..
            DependencyManagement mng = mdl.getDependencyManagement();
            if (mng != null) {
                deps = mng.getDependencies();
                if (deps != null) {
                    for (Dependency d : deps) {
                        if (groupId.equalsIgnoreCase(d.getGroupId()) && artifactId.equalsIgnoreCase(d.getArtifactId())) {
                            managed = d;
                            break;
                        }
                    }
                }
            }
        }
        if (add && ret == null) {
            ret = mdl.getModel().getFactory().createDependency();
            ret.setGroupId(groupId);
            ret.setArtifactId(artifactId);
            mdl.addDependency(ret);
        }
        // if managed dependency section is present, return that one for editing..
        return managed == null ? ret : managed;
    }


    public static boolean hasModelDependency(POMModel mdl, String groupid, String artifactid) {
        return checkModelDependency(mdl, groupid, artifactid, false) != null;
    }

    /**
     *
     * @param mdl
     * @param url of the repository
     * @param add true == add to model, will not add if the repo is in project but not in model (eg. central repo)
     * @return null
     */
    public static Repository addModelRepository(MavenProject project, POMModel mdl, String url) {
        if (url.contains("http://repo1.maven.org/maven2")) { //NOI18N
            return null;
        }
        List<Repository> repos = mdl.getProject().getRepositories();
        if (repos != null) {
            for (Repository r : repos) {
                if (url.equals(r.getUrl())) {
                    //already in model..either in pom.xml or added in this session.
                    return null;
                }
            }
        }
        
        @SuppressWarnings("unchecked")
        List<org.apache.maven.model.Repository> reps = project.getRepositories();
        org.apache.maven.model.Repository prjret = null;
        Repository ret = null;
        if (reps != null) {
            for (org.apache.maven.model.Repository re : reps) {
                if (url.equals(re.getUrl())) {
                    prjret = re;
                    break;
                }
            }
        }
        if (prjret == null) {
            ret = mdl.getFactory().createRepository();
            ret.setUrl(url);
            ret.setId(url);
            mdl.getProject().addRepository(ret);
        }
        return ret;
    }

    private static final String CONFIGURATION_EL = "configuration";//NOI18N

    /**
     * update the source level of project to given value.
     *
     * @param handle handle which models are to be updated
     * @param sourceLevel the sourcelevel to set
     */
    public static void checkSourceLevel(ModelHandle handle, String sourceLevel) {
        String source = PluginPropertyUtils.getPluginProperty(handle.getProject(),
                Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER, Constants.SOURCE_PARAM,
                "compile"); //NOI18N
        if (source != null && source.contains(sourceLevel)) {
            return;
        }
        POMModel mdl = handle.getPOMModel();
        Plugin old = null;
        Plugin plugin;
        Build bld = mdl.getProject().getBuild();
        if (bld != null) {
            old = bld.findPluginById(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
        } else {
            mdl.getProject().setBuild(mdl.getFactory().createBuild());
        }
        if (old != null) {
            plugin = old;
        } else {
            plugin = mdl.getFactory().createPlugin();
            plugin.setGroupId(Constants.GROUP_APACHE_PLUGINS);
            plugin.setArtifactId(Constants.PLUGIN_COMPILER);
            plugin.setVersion(MavenVersionSettings.getDefault().getVersion(MavenVersionSettings.VERSION_COMPILER));
            mdl.getProject().getBuild().addPlugin(plugin);
        }
        Configuration conf = plugin.getConfiguration();
        if (conf == null) {
            conf = mdl.getFactory().createConfiguration();
            plugin.setConfiguration(conf);
        }
        conf.setSimpleParameter(Constants.SOURCE_PARAM, sourceLevel);
        conf.setSimpleParameter(Constants.TARGET_PARAM, sourceLevel);
        handle.markAsModified(handle.getPOMModel());
    }

    /**
     * update the encoding of project to given value.
     *
     * @param handle handle which models are to be updated
     * @param enc encoding to use
     */
    public static void checkEncoding(ModelHandle handle, String enc) {
        boolean wasProperty = false;
        String source = handle.getProject().getProperties().getProperty(Constants.ENCODING_PROP);
        if (source == null) {
            source = PluginPropertyUtils.getPluginProperty(handle.getProject(),
                    Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER,
                    Constants.ENCODING_PARAM, null);
        } else {
            wasProperty = true;
        }
        if (source != null && source.contains(enc)) {
            return;
        }
        if (wasProperty) {
            //new approach, assume all plugins conform to the new setting.
            Properties props = handle.getPOMModel().getProject().getProperties();
            if (props == null) {
                props = handle.getPOMModel().getFactory().createProperties();
                handle.getPOMModel().getProject().setProperties(props);
            }
            props.setProperty(Constants.ENCODING_PROP, enc);
            //do not bother configuring the plugins in this case.
        } else {
            POMModel model = handle.getPOMModel();
            POMComponentFactory fact = model.getFactory();
            Plugin plugin;
            Plugin plugin2;
            Plugin old = null;
            Plugin old2 = null;
            Build bld = handle.getPOMModel().getProject().getBuild();
            if (bld != null) {
                old = bld.findPluginById(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_COMPILER);
                old2 = bld.findPluginById(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_RESOURCES);
            } else {
                model.getProject().setBuild(fact.createBuild());
            }
            if (old != null) {
                plugin = old;
            } else {
                plugin = fact.createPlugin();
                plugin.setGroupId(Constants.GROUP_APACHE_PLUGINS);
                plugin.setArtifactId(Constants.PLUGIN_COMPILER);
                plugin.setVersion(MavenVersionSettings.getDefault().getVersion(MavenVersionSettings.VERSION_COMPILER));
                model.getProject().getBuild().addPlugin(plugin);
            }
            if (old2 != null) {
                plugin2 = old2;
            } else {
                plugin2 = fact.createPlugin();
                plugin2.setGroupId(Constants.GROUP_APACHE_PLUGINS);
                plugin2.setArtifactId(Constants.PLUGIN_RESOURCES);
                plugin2.setVersion(MavenVersionSettings.getDefault().getVersion(MavenVersionSettings.VERSION_RESOURCES));
                model.getProject().getBuild().addPlugin(plugin2);
            }
            Configuration conf = plugin.getConfiguration();
            if (conf == null) {
                conf = fact.createConfiguration();
                plugin.setConfiguration(conf);
            }
            conf.setSimpleParameter(Constants.ENCODING_PARAM, enc);

            conf = plugin2.getConfiguration();
            if (conf == null) {
                conf = fact.createConfiguration();
                plugin2.setConfiguration(conf);
            }
            conf.setSimpleParameter(Constants.ENCODING_PARAM, enc);
        }
        handle.markAsModified(handle.getPOMModel());
    }


}
