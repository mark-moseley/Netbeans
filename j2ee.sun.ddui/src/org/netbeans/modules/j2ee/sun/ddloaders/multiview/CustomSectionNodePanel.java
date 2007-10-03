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
package org.netbeans.modules.j2ee.sun.ddloaders.multiview;

import java.awt.Dimension;
import java.awt.Image;
import javax.swing.ImageIcon;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.SectionNodePanel;

/**
 *
 * @author Peter Williams
 */
public class CustomSectionNodePanel extends SectionNodePanel {

    public static final int MAX_WIDTH = 600;
    
    public CustomSectionNodePanel(final SectionNode node) {
        super(node, false);

        setAlignmentX(LEFT_ALIGNMENT);
//        setBorder(javax.swing.BorderFactory.createTitledBorder("custom section node panel"));
    }
    
    public void setTitleIcon(String iconBase) {
        Image iconImage = org.openide.util.Utilities.loadImage(iconBase, true);
        getTitleButton().setIcon(iconImage != null ? new ImageIcon(iconImage) : null);
    }

//    @Override
//    protected void openInnerPanel() {
//        System.out.println(this.getNode().getClass().getName() + " : SectionNodePanel.openInnerPanel()");
//        super.openInnerPanel();
//    }
//
//    /** Return reasonable maximum size.  Usage of GridBagLayout + stretch fields
//     * in this panel cause the default maximum size behavior to too wide.
//     */
//    @Override
//    public Dimension getMaximumSize() {
//        return new Dimension(MAX_WIDTH, super.getMaximumSize().height);
//    }
    
    /** Return correct preferred size.  The multiline JLabels in this panel cause
     *  the default preferred size behavior to be incorrect (too wide).
     */
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getMinimumSize().width, super.getPreferredSize().height);
    }
    
}
