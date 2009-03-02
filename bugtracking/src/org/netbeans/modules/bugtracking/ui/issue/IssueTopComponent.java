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

package org.netbeans.modules.bugtracking.ui.issue;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.bugtracking.spi.Issue;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top component that displays information about one issue.
 *
 * @author Jan Stola, Tomas Stupka
 */
public final class IssueTopComponent extends TopComponent {
    private static Set<IssueTopComponent> openIssues = new HashSet<IssueTopComponent>();
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";

    private Issue issue;

    public IssueTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(IssueTopComponent.class, "CTL_IssueTopComponent"));
        setToolTipText(NbBundle.getMessage(IssueTopComponent.class, "HINT_IssueTopComponent"));
//        setIcon(Utilities.loadImage(ICON_PATH, true));
        
    }

    public Issue getIssue() {
        return issue;
    }

    public void setIssue(Issue issue) {
        assert (this.issue == null);
        this.issue = issue;
        add(issue.getControler().getComponent(), BorderLayout.CENTER);

        setName(issue.getDisplayName());
        setToolTipText(issue.getTooltip());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        openIssues.add(this);
    }

    @Override
    public void componentClosed() {
        openIssues.remove(this);
    }

    public static synchronized IssueTopComponent find(Issue issue) {
        for (IssueTopComponent tc : openIssues) {
            if (issue.equals(tc.getIssue())) {
                return tc;
            }
        }
        IssueTopComponent tc = new IssueTopComponent();
        tc.setIssue(issue);
        return tc;
    }

}
