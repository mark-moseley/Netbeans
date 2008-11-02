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
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.model.profile.impl;

import org.netbeans.modules.maven.model.profile.Activation;
import org.netbeans.modules.maven.model.profile.ModelList;
import org.netbeans.modules.maven.model.profile.Profile;
import org.netbeans.modules.maven.model.profile.ProfilesComponent;
import org.netbeans.modules.maven.model.profile.ProfilesComponentVisitor;
import org.netbeans.modules.maven.model.profile.ProfilesModel;
import org.netbeans.modules.maven.model.profile.Properties;
import org.netbeans.modules.maven.model.profile.Repository;
import org.w3c.dom.Element;

/**
 *
 * @author mkleint
 */
public class ProfileImpl extends ProfilesComponentImpl implements Profile {

    private static final Class<? extends ProfilesComponent>[] ORDER = new Class[] {
        Activation.class,
        RepositoryImpl.RepoList.class,
        RepositoryImpl.PluginRepoList.class,
        Properties.class
    };

    public ProfileImpl(ProfilesModel model, Element element) {
        super(model, element);
    }
    
    public ProfileImpl(ProfilesModel model) {
        this(model, createElementNS(model, model.getProfilesQNames().PROFILE));
    }

    // attributes

    // child elements
    public Activation getActivation() {
        return getChild(Activation.class);
    }

    public void setActivation(Activation activation) {
        setChild(Activation.class, getModel().getProfilesQNames().ACTIVATION.getName(), activation,
                getClassesBefore(ORDER, Activation.class));
    }

    public String getId() {
        return getChildElementText(getModel().getProfilesQNames().ID.getQName());
    }

    public void setId(String id) {
        setChildElementText(getModel().getProfilesQNames().ID.getName(), id,
                getModel().getProfilesQNames().ID.getQName());
    }

    public java.util.List<Repository> getRepositories() {
        ModelList<Repository> childs = getChild(RepositoryImpl.RepoList.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    public void addRepository(Repository repo) {
        ModelList<Repository> childs = getChild(RepositoryImpl.RepoList.class);
        if (childs == null) {
            setChild(RepositoryImpl.RepoList.class,
                    getModel().getProfilesQNames().REPOSITORIES.getName(),
                    getModel().getFactory().create(this, getModel().getProfilesQNames().REPOSITORIES.getQName()),
                    getClassesBefore(ORDER, RepositoryImpl.RepoList.class));
            childs = getChild(RepositoryImpl.RepoList.class);
            assert childs != null;
        }
        childs.addListChild(repo);
    }

    public void removeRepository(Repository repo) {
        ModelList<Repository> childs = getChild(RepositoryImpl.RepoList.class);
        if (childs != null) {
            childs.removeListChild(repo);
        }
    }

    public java.util.List<Repository> getPluginRepositories() {
        ModelList<Repository> childs = getChild(RepositoryImpl.PluginRepoList.class);
        if (childs != null) {
            return childs.getListChildren();
        }
        return null;
    }

    public void addPluginRepository(Repository repo) {
        ModelList<Repository> childs = getChild(RepositoryImpl.PluginRepoList.class);
        if (childs == null) {
            setChild(RepositoryImpl.PluginRepoList.class,
                    getModel().getProfilesQNames().PLUGINREPOSITORIES.getName(),
                    getModel().getFactory().create(this, getModel().getProfilesQNames().PLUGINREPOSITORIES.getQName()),
                    getClassesBefore(ORDER, RepositoryImpl.PluginRepoList.class));
            childs = getChild(RepositoryImpl.PluginRepoList.class);
            assert childs != null;
        }
        childs.addListChild(repo);
    }

    public void removePluginRepository(Repository repo) {
        ModelList<Repository> childs = getChild(RepositoryImpl.PluginRepoList.class);
        if (childs != null) {
            childs.removeListChild(repo);
        }
    }



    public Properties getProperties() {
        return getChild(Properties.class);
    }

    public void setProperties(Properties props) {
        setChild(Properties.class, getModel().getProfilesQNames().PROPERTIES.getName(), props,
                getClassesBefore(ORDER, Properties.class));
    }


    public void accept(ProfilesComponentVisitor visitor) {
        visitor.visit(this);
    }


    public static class List extends ListImpl<Profile> {
        public List(ProfilesModel model, Element element) {
            super(model, element, model.getProfilesQNames().PROFILE, Profile.class);
        }

        public List(ProfilesModel model) {
            this(model, createElementNS(model, model.getProfilesQNames().PROFILES));
        }
    }


}