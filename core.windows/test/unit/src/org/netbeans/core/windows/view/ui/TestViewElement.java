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

package org.netbeans.core.windows.view.ui;

import javax.swing.JComponent;
import javax.swing.JSplitPane;
import org.netbeans.core.windows.*;
import org.netbeans.core.windows.view.*;
import org.netbeans.core.windows.view.ui.*;

import java.awt.Component;
import java.awt.Dimension;

/**
 * A dummy ViewElement implementation for MultiSplitPane testing.
 *
 * @author Stanislav Aubrecht
 */
class TestViewElement extends ViewElement {

    SplitTestComponent myComponent;
    int orientation;

    /** Creates a new instance of TestViewElement */
    public TestViewElement( int orientation, double resizeWeight ) {
        super( null, resizeWeight );
        this.myComponent = new SplitTestComponent();
        this.orientation = orientation;
    }
    
    public Component getComponent() {
        return myComponent;
    }
    
    /**
     * lets the visual components adjust to the current state.
     * @returns true if a change was performed.
     */
    public boolean updateAWTHierarchy(Dimension availableSpace) {
        return true;
    }
    
    int getSizeInSplit() {
        return orientation == JSplitPane.HORIZONTAL_SPLIT ? getComponent().getWidth() : getComponent().getHeight();
    }
    
    int getNonSplitSize() {
        return orientation != JSplitPane.HORIZONTAL_SPLIT ? getComponent().getWidth() : getComponent().getHeight();
    }

    void setMinSize( int minSize ) {
        myComponent.setMinimumSize( new Dimension( minSize, minSize ) );
    }
    
    private static class SplitTestComponent extends JComponent {
        public SplitTestComponent() {
            setMinimumSize( new Dimension( 0, 0 ) );
        }
    }
}
