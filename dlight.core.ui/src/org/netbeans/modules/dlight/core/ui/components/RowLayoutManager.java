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
package org.netbeans.modules.dlight.core.ui.components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * 
 * @author Maria Tishkova
 */
public class RowLayoutManager implements LayoutManager {

    int xInset = 0;
    int yInset = 0;
    int yGap = 2;
    Container c;
    int prefferedHeight = 0;

    public void addLayoutComponent(String name, Component comp) {
        if (c != null) {
            c.add(name, comp);
        }
    }

    public void removeLayoutComponent(Component comp) {
        if (c != null) {
            c.remove(comp);
        }
    }

    public Dimension preferredLayoutSize(Container parent) {
        return minimumLayoutSize(c);
    }

    public Dimension minimumLayoutSize(Container parent) {
        if (c == null) {
            return new Dimension(100, 100);
        }
        Insets insets = c.getInsets();
        int height = yInset + insets.top;
        int width = 0 + insets.left + insets.right;

        Component[] children = c.getComponents();

        Dimension compSize = null;

        for (int i = 0; i < children.length; i++) {
            compSize = children[i].getPreferredSize();
            height += compSize.height;
            width = Math.max(width, compSize.width + insets.left + insets.right + xInset * 2);
        }

        height += insets.bottom;
        return new Dimension(width, height);
    }

    public void layoutContainer(Container c) {
        this.c = c;
        Insets insets = c.getInsets();
        int height = yInset + insets.top;
        int width = 0 + insets.left + insets.right;
        int myWidth = c.getSize().width;
        Component[] children = c.getComponents();
        Dimension compSize = null;

        for (int i = 0; i < children.length; i++) {
            compSize = children[i].getPreferredSize();
            children[i].setSize(myWidth, compSize.height);
            children[i].setLocation(xInset + insets.left, height);
            height += compSize.height;
        }

        prefferedHeight = height;
    }
}
