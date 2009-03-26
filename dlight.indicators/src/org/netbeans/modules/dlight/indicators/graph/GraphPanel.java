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
package org.netbeans.modules.dlight.indicators.graph;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Convenient base class for indicator components.
 *
 * @author Alexey Vladykin
 */
public class GraphPanel<G extends JComponent, L extends JComponent> extends JPanel {

    private static final int PADDING = 12;
    private final String title;
    private final G graph;
    private final L legend;
    private final JComponent hAxis;
    private final JComponent vAxis;

    public GraphPanel(String title, G graph, L legend, JComponent hAxis, JComponent vAxis) {
        this.title = title;
        this.graph = graph;
        this.legend = legend;
        this.hAxis = hAxis;
        this.vAxis = vAxis;
        initComponents();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints c;

        JLabel label = new JLabel(title);
        Font labelFont = label.getFont();
        label.setFont(labelFont.deriveFont(labelFont.getStyle() | Font.BOLD));
        label.setForeground(GraphConfig.TEXT_COLOR);
        c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(PADDING, PADDING, 0, PADDING);
        add(label, c);

        if (vAxis != null) {
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.VERTICAL;
            c.weighty = 1.0;
            c.insets = new Insets(PADDING / 2, PADDING, hAxis == null? PADDING : 0, 0);
            add(vAxis, c);
        }

        graph.setBorder(BorderFactory.createLineBorder(GraphConfig.BORDER_COLOR));

        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.insets = new Insets(PADDING / 2, vAxis == null ? PADDING : 0, hAxis == null ? PADDING : 0, 0);
        add(graph, c);

        legend.setBackground(Color.WHITE);
        legend.setBorder(BorderFactory.createLineBorder(GraphConfig.BORDER_COLOR));
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weighty = 1.0;
        c.insets = new Insets(PADDING / 2, -1, hAxis == null ? PADDING : 0, PADDING);
        add(legend, c);

        if (hAxis != null) {
            c = new GridBagConstraints();
            c.gridx = vAxis == null ? 0 : 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
            c.insets = new Insets(0, vAxis == null ? PADDING : 0, PADDING, 0);
            add(hAxis, c);
        }
    }

    protected final G getGraph() {
        return graph;
    }

    protected final L getLegend() {
        return legend;
    }

    protected final JComponent getHorizontalAxis() {
        return hAxis;
    }

    protected final JComponent getVerticalAxis() {
        return vAxis;
    }
}
