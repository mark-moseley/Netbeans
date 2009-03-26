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

package org.netbeans.modules.bugzilla.kenai;

import java.awt.Image;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.netbeans.modules.bugtracking.spi.Query;
import org.netbeans.modules.bugzilla.repository.BugzillaConfiguration;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.util.BugzillaConstants;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class KenaiRepository extends BugzillaRepository {

    static final String ICON_PATH = "org/netbeans/modules/bugtracking/ui/resources/kenai-small.png"; // NOI18N
    private String urlParam;
    private Image icon;
    private String product;
    private KenaiQuery myIssues;
    private KenaiQuery allIssues;
    private String host;

    public KenaiRepository(String repoName, String url, String host, String urlParam, String product) {
        super(repoName, url, KenaiUtil.getKenaiUser(), KenaiUtil.getKenaiPassword());
        this.urlParam = urlParam;
        icon = ImageUtilities.loadImage(ICON_PATH, true);
        this.product = product;
        this.host = host;
    }

    @Override
    public Image getIcon() {
        return icon;
    }

    @Override
    public Query createQuery() {
        KenaiQuery q = new KenaiQuery(null, this, null, product, false, false);
        return q;
    }

    @Override
    public Issue createIssue() {
        return super.createIssue();
    }

    @Override
    public synchronized Query[] getQueries() {
        Query[] qs = super.getQueries();
        Query[] dq = getDefinedQueries();
        Query[] ret = new Query[qs.length + dq.length];
        System.arraycopy(qs, 0, ret, 0, qs.length);
        System.arraycopy(dq, 0, ret, qs.length, dq.length);
        return ret;
    }

    private Query[] getDefinedQueries() {
        List<Query> queries = new ArrayList<Query>();

        // my issues - only if username provided
        if(KenaiUtil.isLoggedIn()) {
            if(myIssues == null) {
                StringBuffer url = new StringBuffer();
                url.append(urlParam);

                // XXX escape @?
                // XXX what if user already mail address?
                String user = KenaiUtil.getKenaiUser();
                if(user == null) {
                    user = "";
                }
                String userMail = user + "@"+ host; // NOI18N
                url.append(MessageFormat.format(BugzillaConstants.MY_ISSUES_PARAMETERS_FORMAT, product, userMail));

                myIssues =
                    new KenaiQuery(
                        NbBundle.getMessage(KenaiRepository.class, "LBL_MyIssues"), // NOI18N
                        this,
                        url.toString(),
                        product,
                        true,
                        true);
            }
            queries.add(myIssues);
        }

        // all issues
        if(allIssues == null) {
            StringBuffer url = new StringBuffer();
            url = new StringBuffer();
            url.append(urlParam);
            url.append(MessageFormat.format(BugzillaConstants.ALL_ISSUES_PARAMETERS, product));
            allIssues =
                new KenaiQuery(
                    NbBundle.getMessage(KenaiRepository.class, "LBL_AllIssues"), // NOI18N
                    this,
                    url.toString(),
                    product,
                    true,
                    true);
        }
        queries.add(allIssues);
        return queries.toArray(new Query[queries.size()]);
    }

    @Override
    protected BugzillaConfiguration createConfiguration() {
        KenaiConfiguration c = BugzillaConfiguration.create(this, KenaiConfiguration.class);
        c.setProducts(product);
        return c;
    }

    protected void setCredentials(String user, String password) {
        super.setTaskRepository(getDisplayName(), getUrl(), user, password);
    }

}
