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
package org.netbeans.modules.hibernate.hqleditor;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.hibernate.util.CustomClassLoader;
import org.netbeans.modules.hibernate.hqleditor.ui.HQLEditorTopComponent;
import org.netbeans.modules.hibernate.service.api.HibernateEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * HQL Editor controller. Controls overall HQL query execution.
 *
 * @author Vadiraj Deshpande (Vadiraj.Deshpande@Sun.COM)
 */
public class HQLEditorController {

    private Logger logger = Logger.getLogger(HQLEditorController.class.getName());
    HQLEditorTopComponent editorTopComponent = null;

    public void executeHQLQuery(final String hql,
            final FileObject configFileObject,
            final int maxRowCount,
            final ProgressHandle ph) {
        final List<URL> localResourcesURLList = new ArrayList<URL>();

        try {
            ph.progress(
                    NbBundle.getMessage(HQLEditorTopComponent.class, "queryExecutionPrepare"), 10);
            Project project = FileOwnerQuery.getOwner(configFileObject);
            // Parse POJOs from HQL
            // Check and if required compile POJO files mentioned in HQL
            parseAndCompilePOJOs(hql, configFileObject, project);
            // Construct custom classpath here.
            HibernateEnvironment env = project.getLookup().lookup(HibernateEnvironment.class);
            localResourcesURLList.addAll(env.getProjectClassPath(configFileObject));

        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }

        final ClassLoader customClassLoader = new CustomClassLoader(localResourcesURLList.toArray(new URL[]{}),
                this.getClass().getClassLoader());

        Thread t = new Thread() {

            @Override
            public void run() {
                Thread.currentThread().setContextClassLoader(customClassLoader);
                HQLExecutor queryExecutor = new HQLExecutor();
                try {
                    ph.progress(50);
                    ph.setDisplayName(NbBundle.getMessage(HQLEditorTopComponent.class, "queryExecutionPassControlToHibernate"));
                    HQLResult r = queryExecutor.execute(hql, configFileObject, maxRowCount, ph);
                    ph.progress(80);
                    ph.setDisplayName(NbBundle.getMessage(HQLEditorTopComponent.class, "queryExecutionProcessResults"));
                    editorTopComponent.setResult(r);
                } catch (Exception e) {
                    Exceptions.printStackTrace(e);
                }

            }
        };
        t.setContextClassLoader(customClassLoader);
        try {
            t.start();
        } catch (Exception e) {
        }
    }

    public void init(Node[] activatedNodes) {
        editorTopComponent = new HQLEditorTopComponent(this);
        editorTopComponent.open();
        editorTopComponent.requestActive();

        editorTopComponent.fillHibernateConfigurations(activatedNodes);
    }

    private void parseAndCompilePOJOs(String hql, FileObject configFileObject, Project project) {
//        ClassPathProvider cpProvider = Lookup.getDefault().lookup(ClassPathProvider.class);
//        System.out.println("class path provider = " + cpProvider);
//        if(cpProvider != null) {
//            ClassPath cp = cpProvider.findClassPath(configFileObject, ClassPath.SOURCE);
//            System.out.println("classpath " + cp);
//            if(cp != null) {
//                StringTokenizer tokenizer = new StringTokenizer(hql);
//                while(tokenizer.hasMoreTokens()) {
//                    String token = tokenizer.nextToken();
//                    System.out.println("Token = " + token);
//                    FileObject file = cp.findResource(token);
//                    System.out.println("Found file " + file);
//                }
//            }
//        }
        StringTokenizer tokenizer = new StringTokenizer(hql);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            System.out.println("Token = " + token);
//            FileObject javaSource = findJavaSource(token, project);
//            if(javaSource != null) {
//                // Check for class file..
//
//            }
        }
    }
//    private FileObject findJavaSource(String text, Project project) {
//
//        return false;
//    }
}
