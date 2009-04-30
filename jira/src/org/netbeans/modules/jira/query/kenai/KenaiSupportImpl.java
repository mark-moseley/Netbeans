/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.jira.query.kenai;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.modules.bugtracking.spi.KenaiSupport;
import org.netbeans.modules.bugtracking.spi.Repository;
import org.netbeans.modules.bugtracking.util.KenaiUtil;
import org.netbeans.modules.jira.Jira;
import org.netbeans.modules.kenai.api.Kenai;
import org.netbeans.modules.kenai.api.KenaiException;
import org.netbeans.modules.kenai.api.KenaiFeature;
import org.netbeans.modules.kenai.api.KenaiProject;
import org.netbeans.modules.kenai.api.KenaiService;
import org.netbeans.modules.kenai.api.KenaiService.Type;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Stupka
 */
public class KenaiSupportImpl extends KenaiSupport implements PropertyChangeListener {

    private final Set<KenaiRepository> repositories = new HashSet<KenaiRepository>();

    public KenaiSupportImpl() {
        Kenai.getDefault().addPropertyChangeListener(this);
    }

    @Override
    public Repository createRepository(KenaiProject project) {
        if(project == null) {
            return null;
        }
        try {
            KenaiFeature[] features = project.getFeatures(Type.ISSUES);
            for (KenaiFeature f : features) {
                if (!KenaiService.Names.JIRA.equals(f.getService())) {
                    return null;
                }
                final URL loc;
                try {
                    loc = new URL(f.getLocation());
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }

                String host = loc.getHost();
                String location = f.getLocation();
                int idx = location.indexOf("/browse/");
                if (idx <= 0) {
                    Jira.LOG.warning("can't get issue tracker url from [" + project.getName() + ", " + location + "]"); // NOI18N
                    return null;
                }
                String url = location.substring(0, idx);
                if (url.startsWith("http:")) { // XXX hack???                   // NOI18N
                    url = "https" + url.substring(4);                           // NOI18N
                }

                String product = location.substring(idx + "/browse/".length());

                KenaiRepository repo = new KenaiRepository(project.getDisplayName(), url, host, product);
                synchronized (repositories) {
                    repositories.add(repo);
                }

                return repo;
            }
        } catch (KenaiException kenaiException) {
            Jira.LOG.log(Level.SEVERE, kenaiException.getMessage(), kenaiException);
        }
        return null;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Kenai.PROP_LOGIN)) {
            // XXX move to spi

            // get all kenai repositories
            KenaiRepository[] repos;
            synchronized(repositories) {
                if(repositories.size() == 0) {
                    return;
                }
                repos = repositories.toArray(new KenaiRepository[repositories.size()]);
            }

            // get kenai credentials
            String user;
            String psswd;
            PasswordAuthentication pa = KenaiUtil.getPasswordAuthentication(false);
            if(pa != null) {
                user = pa.getUserName();
                psswd = new String(pa.getPassword());
            } else {
                user = "";                                                      // NOI18N
                psswd = "";                                                     // NOI18N
            }

            for (KenaiRepository kr : repos) {
                // refresh their taskdata with the relevant username/password
                kr.setCredentials(user, psswd);
            }
        }
    }
}

