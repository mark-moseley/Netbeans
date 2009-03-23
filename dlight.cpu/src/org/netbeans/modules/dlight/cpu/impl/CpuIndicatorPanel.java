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
package org.netbeans.modules.dlight.cpu.impl;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collections;
import javax.swing.BorderFactory;
import org.netbeans.modules.dlight.indicators.graph.GraphPanel;
import org.netbeans.modules.dlight.indicators.graph.GraphColors;
import org.netbeans.modules.dlight.indicators.graph.GraphDescriptor;
import org.netbeans.modules.dlight.indicators.graph.Legend;
import org.netbeans.modules.dlight.indicators.graph.PercentageGraph;
import org.openide.util.NbBundle;

/**
 * @author Alexey Vladykin
 */
public class CpuIndicatorPanel {

    private static final Color COLOR_SYS = GraphColors.COLOR_1;
    private static final Color COLOR_USR = GraphColors.COLOR_3;
    private static final GraphDescriptor SYS_DESCRIPTOR = new GraphDescriptor(COLOR_SYS, "System");
    private static final GraphDescriptor USR_DESCRIPTOR = new GraphDescriptor(COLOR_USR, "User");

    private final PercentageGraph graph;
    private final GraphPanel<PercentageGraph, Legend> panel;

    /*package*/ CpuIndicatorPanel(CpuIndicator indicator) {
        graph = createGraph(indicator);
        panel = new GraphPanel(getTitle(), graph, createLegend(), null, graph.getVerticalAxis());
    }

    public GraphPanel getPanel() {
        return panel;
    }

    private static String getTitle() {
        return NbBundle.getMessage(CpuIndicatorPanel.class, "indicator.title"); // NOI18N
    }

    private static PercentageGraph createGraph(final CpuIndicator indicator) {
        PercentageGraph graph = new PercentageGraph(SYS_DESCRIPTOR, USR_DESCRIPTOR);
        graph.setBorder(BorderFactory.createLineBorder(GraphColors.BORDER_COLOR));
        graph.setMinimumSize(new Dimension(80, 60));
        graph.setPreferredSize(new Dimension(80, 60));
        graph.getVerticalAxis().setMinimumSize(new Dimension(30, 60));
        graph.getVerticalAxis().setPreferredSize(new Dimension(30, 60));

        MouseListener ml = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    indicator.fireActionPerformed();
                }
            }
        };
        graph.addMouseListener(ml);
        return graph;
    }

    private static Legend createLegend() {
        return new Legend(Arrays.asList(SYS_DESCRIPTOR, USR_DESCRIPTOR), Collections.<String, String>emptyMap());
    }

    /*package*/ void addData(int sys, int usr) {
        graph.addData(sys, usr);
    }

    /*package*/ void setSysValue(int v) {
        //getLegend().setSysValue(formatValue(v));
    }

    /*package*/ void setUsrValue(int v) {
        //getLegend().setUsrValue(formatValue(v));
    }

}
