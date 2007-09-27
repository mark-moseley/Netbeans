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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.sql.framework.ui.view.validation;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextArea;

import org.netbeans.modules.sql.framework.ui.graph.IGraphView;


/**
 * @author Ritesh Adval
 */
public class SQLValidationView extends JPanel {

    private static final String TABLE_VIEW = "table_view";

    private static final String TEXT_VIEW = "text_view";

    private ValidationTableView vTableView;

    private JTextArea textArea;

    private JPanel cardPanel;

    private IGraphView graphView;

    public SQLValidationView(IGraphView gView) {
        this.graphView = gView;
        initGui();
    }

    private void initGui() {
        this.setLayout(new BorderLayout());

        cardPanel = new JPanel();
        cardPanel.setLayout(new CardLayout());
        this.add(cardPanel, BorderLayout.CENTER);

        //add table panel
        vTableView = new ValidationTableView(this.graphView);
        cardPanel.add(vTableView, TABLE_VIEW);

        //add text panel
        textArea = new JTextArea();
        cardPanel.add(textArea, TEXT_VIEW);

    }

    public void setValidationInfos(List vInfos) {
        this.graphView.clearSelection();
        this.graphView.resetSelectionColors();
        vTableView.setValidationInfos(vInfos);
        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, TABLE_VIEW);
    }

    public void clearView() {
        vTableView.clearView();
        textArea.setText("");
    }

    public void appendToView(String msg) {
        textArea.append(msg);
        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, TEXT_VIEW);
    }
}

