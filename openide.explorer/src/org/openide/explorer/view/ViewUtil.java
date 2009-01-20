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

package org.openide.explorer.view;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.UIManager;
import javax.swing.plaf.UIResource;

/**
 * Utility class
 *
 * @author S. Aubrecht
 */
class ViewUtil {
    
    public static final boolean isAquaLaF =
            "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N

    private static final boolean useDefaultBackground =
            Boolean.getBoolean("nb.explorerview.aqua.defaultbackground"); //NOI18N

    private ViewUtil() {
    }

    /**
     * Change background of given component to light gray on Mac look and feel
     * when the component is in a tabbed container and its background hasn't been
     * already changed (is instance of UIResource).
     * @param c
     */
    static void adjustBackground( JComponent c ) {
        if( !isAquaLaF || useDefaultBackground )
            return;

        if( !isInTabbedContainer(c) )
            return;

        Color currentBackground = c.getBackground();
        if( currentBackground instanceof UIResource ) {
            c.setBackground(UIManager.getColor("NbExplorerView.background"));
        }
    }


    private static boolean isInTabbedContainer( Component c ) {
        Component parent = c.getParent();
        while( null != parent ) {
            if( parent instanceof JComponent
                    && "TabbedContainerUI".equals( ((JComponent)parent).getUIClassID() ) ) //NOI18N
                return true;
            parent = parent.getParent();
        }
        return false;
    }
}
