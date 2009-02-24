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

package org.netbeans.modules.bugzilla;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaAttribute;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaClient;
import org.eclipse.mylyn.internal.bugzilla.core.BugzillaRepositoryConnector;
import org.eclipse.mylyn.tasks.core.RepositoryResponse;
import org.eclipse.mylyn.tasks.core.RepositoryStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;

/**
 *
 * @author tomas
 */
public class TestUtil implements TestConstants {
    public static void handleException(Exception exception) throws Throwable {
        if (exception instanceof CoreException) {
            CoreException e = (CoreException) exception;
            IStatus status = e.getStatus();
            if (status instanceof RepositoryStatus) {
                RepositoryStatus rs = (RepositoryStatus) status;
                String html = rs.getHtmlMessage();
                if(html != null && !html.trim().equals("")) {
//                    HtmlBrowser.URLDisplayer displayer = HtmlBrowser.URLDisplayer.getDefault ();
//                    if (displayer != null) {
//                        displayer.showURL (url);
//                    } else {
//                        //LOG.info("No URLDisplayer found.");
//                    }

                    final HtmlPanel p = new HtmlPanel();
                    p.setHtml(html);
                    BugzillaUtil.show(p, "html", "ok");
                }
                throw new Exception(rs.getHtmlMessage());
            }
            if (e.getStatus().getException() != null) {
                throw e.getStatus().getException();
            }
            if (e.getCause() != null) {
                throw e.getCause();
            }
            throw e;
        }
        exception.printStackTrace();
        throw exception;
    }

    public static TaskData createTaskData(BugzillaRepositoryConnector brc, TaskRepository repository, String summary, String desc, String typeName) throws MalformedURLException, CoreException {
        TaskAttributeMapper attributeMapper = brc.getTaskDataHandler().getAttributeMapper(repository);
        TaskData data = new TaskData(attributeMapper, repository.getConnectorKind(), repository.getRepositoryUrl(), "");
        TaskAttribute rta = data.getRoot();
        TaskAttribute ta = rta.getMappedAttribute(TaskAttribute.USER_ASSIGNED);
        ta = rta.createMappedAttribute(TaskAttribute.SUMMARY);
        ta.setValue(summary);
        ta = rta.createMappedAttribute(TaskAttribute.DESCRIPTION);
        ta.setValue(desc);

        BugzillaClient client = brc.getClientManager().getClient(repository, NULL_PROGRESS_MONITOR);
        String os = client.getRepositoryConfiguration().getOSs().get(0);
        ta = rta.createMappedAttribute(BugzillaAttribute.OP_SYS.getKey());
        ta.setValue(os);

        ta = rta.createMappedAttribute(BugzillaAttribute.PRODUCT.getKey());
        ta.setValue(TEST_PROJECT);

        String platform = client.getRepositoryConfiguration().getPlatforms().get(0);
        ta = rta.createMappedAttribute(BugzillaAttribute.REP_PLATFORM.getKey());
        ta.setValue(platform);

        String version = client.getRepositoryConfiguration().getVersions(TEST_PROJECT).get(0);
        ta = rta.createMappedAttribute(BugzillaAttribute.VERSION.getKey());
        ta.setValue(version);

        String component = client.getRepositoryConfiguration().getComponents(TEST_PROJECT).get(0);
        ta = rta.createMappedAttribute(BugzillaAttribute.COMPONENT.getKey());
        ta.setValue(component);

        return data;
    }

    public static RepositoryResponse postTaskData(BugzillaRepositoryConnector brc, TaskRepository repository, TaskData data) throws CoreException {
        Set<TaskAttribute> attrs = new HashSet<TaskAttribute>(); // XXX what is this for
        return  brc.getTaskDataHandler().postTaskData(repository, data, attrs, NULL_PROGRESS_MONITOR);
    }

    public static String createIssue(BugzillaRepository repo, String summary) throws MalformedURLException, CoreException {
        BugzillaRepositoryConnector brc = Bugzilla.getInstance().getRepositoryConnector();
        TaskRepository tr = repo.getTaskRepository();
        TaskData data = TestUtil.createTaskData(brc, tr, summary, "desc", "bug");
        RepositoryResponse rr = TestUtil.postTaskData(brc, tr, data);
        return rr.getTaskId();
    }
    
    public static BugzillaRepository getRepository(String name, String url, String user, String psswd) {
        return new BugzillaRepository(name, url, user, psswd);
    }

    static void validate(BugzillaRepositoryConnector brc, TaskRepository repository) throws Throwable {
        try {
            brc.getClientManager().getClient(repository, NULL_PROGRESS_MONITOR).validate(NULL_PROGRESS_MONITOR);
        } catch (Exception ex) {
            handleException(ex);
        }
    }

}
