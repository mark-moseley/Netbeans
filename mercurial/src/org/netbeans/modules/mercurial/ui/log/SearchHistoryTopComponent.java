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
package org.netbeans.modules.mercurial.ui.log;

import org.openide.windows.TopComponent;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

import java.util.*;
import java.io.File;
import java.awt.BorderLayout;
import org.netbeans.modules.mercurial.ui.diff.DiffSetupSource;
import org.netbeans.modules.mercurial.util.HgUtils;
import org.netbeans.modules.versioning.spi.VCSContext;

/**
 * @author Maros Sandor
 */
public class SearchHistoryTopComponent extends TopComponent implements DiffSetupSource {

    private SearchHistoryPanel shp;
    private SearchCriteriaPanel scp;

    public SearchHistoryTopComponent() {
        getAccessibleContext().setAccessibleName(NbBundle.getMessage(SearchHistoryTopComponent.class, "ACSN_SearchHistoryT_Top_Component")); // NOI18N
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SearchHistoryTopComponent.class, "ACSD_SearchHistoryT_Top_Component")); // NOI18N
    }
    
    public SearchHistoryTopComponent(VCSContext context) {
        this(context, null, null, null, null);
    }

    public SearchHistoryTopComponent(VCSContext context, String commitMessage, String username, Date from, Date to) {
        this();
        File[] roots = context.getRootFiles().toArray(new File[0]);
        initComponents(roots, commitMessage, username, from, to);
    }

    public SearchHistoryTopComponent(String repositoryUrl, File localRoot, long revision) {
        this();
        initComponents(repositoryUrl, localRoot, revision);
    }

    /**
     * Support for openning file history with a specific DiffResultsView
     * @param file it's history shall be shown
     * @param fac factory creating a specific DiffResultsView - just override its createDiffResultsView method
     */
    SearchHistoryTopComponent(File file, DiffResultsViewFactory fac) {
        this();
        initComponents(new File[] {file}, null, null, null, null);
        shp.setDiffResultsViewFactory(fac);
        // showing only one file - so disable the show all changepaths options
        shp.disableFileChangesOption(false);
    }

    public void search() {        
        shp.executeSearch();
        shp.setSearchCriteria(false);
    }
    
    public void searchOut() {  
        shp.setOutSearch();
        scp.setTo("");
    }

    public void searchIncoming() {  
        shp.setIncomingSearch();
        scp.setTo("");
    }

    private void initComponents(String repositoryUrl, File localRoot, long revision) {
        setLayout(new BorderLayout());
        scp = new SearchCriteriaPanel(repositoryUrl);
        scp.setFrom(Long.toString(revision));
        scp.setTo(Long.toString(revision));
        shp = new SearchHistoryPanel(repositoryUrl, localRoot, scp);
        add(shp);
        }

    private void initComponents(File[] roots, String commitMessage, String username, Date from, Date to) {
        setLayout(new BorderLayout());
        scp = new SearchCriteriaPanel(roots);
        scp.setCommitMessage(commitMessage);
        scp.setUsername(username);
        if (from != null){ 
            scp.setFrom(SearchExecutor.simpleDateFormat.format(from));
        }
        if (to != null){
            scp.setTo(SearchExecutor.simpleDateFormat.format(to));
        }
        shp = new SearchHistoryPanel(roots, scp);
        add(shp);
        }

    public int getPersistenceType(){
       return TopComponent.PERSISTENCE_NEVER;
    }
    
    protected void componentClosed() {
       //((DiffMainPanel) getComponent(0)).componentClosed();
       super.componentClosed();
    }
    
    protected String preferredID(){
        if (shp.isIncomingSearch()) {
            return "Hg.IncomingSearchHistoryTopComponent";    // NOI18N
        } else if (shp.isOutSearch()) {
            return "Hg.OutSearchHistoryTopComponent";    // NOI18N
        }
        return "Hg.SearchHistoryTopComponent";    // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(getClass());
    }

    public Collection getSetups() {
        return shp.getSetups();
    }

    public String getSetupDisplayName() {
        return getDisplayName();
    }
    
    /**
     * Provides an initial diff view. To display a specific one, override createDiffResultsView.
     */
    public static class DiffResultsViewFactory {
        DiffResultsView createDiffResultsView(SearchHistoryPanel panel, List<RepositoryRevision> results) {
            return new DiffResultsView(panel, results);
        }
    }
}
