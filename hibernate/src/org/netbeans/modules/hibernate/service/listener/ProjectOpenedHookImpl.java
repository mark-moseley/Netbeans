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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hibernate.service.listener;

import java.util.ArrayList;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.cfg.model.HibernateConfiguration;
import org.netbeans.modules.hibernate.service.HibernateEnvironment;
import org.netbeans.modules.hibernate.util.HibernateUtil;
import org.netbeans.spi.project.ui.ProjectOpenedHook;

/**
 * Class that listens for (global) project open/close operations and 
 * registeres or un-registeres Hibernate specific artifacts.
 * 
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class ProjectOpenedHookImpl extends ProjectOpenedHook{

    private Project project;
    private HibernateEnvironment hibernateEnvironment;
    
    private Logger logger = Logger.getLogger(ProjectOpenedHookImpl.class.getName());
    
    public ProjectOpenedHookImpl(Project project, HibernateEnvironment hibernateEnvironment) {
        this.project = project;
        this.hibernateEnvironment = hibernateEnvironment;
    }

    
    @Override
    protected void projectOpened() {
        // Check for Hibernate files in this project.
        ArrayList<HibernateConfiguration> hibernateConfigurations = HibernateUtil.getAllHibernateConfigurations(project);
        if(hibernateConfigurations.size() != 0) {
            hibernateEnvironment = new HibernateEnvironment(project);
        }
        logger.info("project opened .. " + project);
        logger.info("config : " + project.getLookup().lookup(HibernateEnvironment.class).getAllHibernateConfigurationsFromProject());
        

//        // Three cases exists..
//        //1. This web project do not have hibernate files. fine.. NOP
//        //2. This web project already has hibernate files.. search and find them.
//        //3. The web project already has hibernate files and its lookup has the ojb..
//        // Does this third case occur? I think no.
        // this one I need to take care of it..
    }

    @Override
    protected void projectClosed() {
        //TODO clean up here.
    }

}
