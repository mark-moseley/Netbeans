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

package org.netbeans.modules.bugtracking.spi;

import java.awt.event.ActionEvent;
import org.openide.nodes.*;
import org.openide.util.lookup.Lookups;
import javax.swing.*;

/**
 * The node that is rendered in the SyncTable view. It gets values to display from the
 * CvsFileNode which serves as the 'data' node for this 'visual' node.
 * 
 * @author Maros Sandor
 */
public abstract class IssueNode extends AbstractNode {
    
    private Issue issue;

    private String htmlDisplayName;

    public IssueNode(Issue issue) {
        this(Children.LEAF, issue);
    }

    private IssueNode(Children children, Issue issue) {
        super(children, Lookups.fixed());
        this.issue = issue;
        initProperties();
        refreshHtmlDisplayName();
    }

    protected abstract Node.Property<?>[] getProperties();

    @Override
    public Action getPreferredAction() {
        // XXX
        return new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                issue.open();
            }
        };
    }

    /**
     * Provide cookies to actions.
     * If a node represents primary file of a DataObject
     * it has respective DataObject cookies.
     */
    @Override
    public Cookie getCookie(Class klass) {
        return super.getCookie(klass);
    }

    public void setSeen(boolean seen) {
        issue.setSeen(seen);
    }

    public boolean wasSeen() {
        return issue.wasSeen();
    }

    private void initProperties() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet();

        Node.Property<?>[] properties = getProperties();
        for (Property<?> property : properties) {
            ps.put(property);
        }
        
        sheet.put(ps);
        setSheet(sheet);    
    }

    private void refreshHtmlDisplayName() {
        htmlDisplayName = issue.getDisplayName();
    }

    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    public void refresh() {
        refreshHtmlDisplayName();
    }

    void fireSeenValueChanged(boolean oldValue, boolean newValue) {
        if(oldValue != newValue) firePropertyChange(Issue.LABEL_NAME_SEEN, oldValue, newValue);
    }
    
}
