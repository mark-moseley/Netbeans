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

package org.netbeans.modules.java.navigation;

import javax.lang.model.element.Element;
import javax.swing.JComponent;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Zezula
 */
public class ClassMemberPanel implements NavigatorPanel {

    private ClassMemberPanelUI component;

    private static volatile ClassMemberPanel INSTANCE;   //Apparently not accessed in event dispatch thread in CaretListeningTask
    
    public ClassMemberPanel() {
    }

    public void panelActivated(Lookup context) {
        assert context != null;
        INSTANCE = this;
        getClassMemberPanelUI().showWaitNode();
        ClassMemberNavigatorJavaSourceFactory.getInstance().setLookup(context, getClassMemberPanelUI());
        
    }

    public void panelDeactivated() {
        getClassMemberPanelUI().showWaitNode(); // To clear the ui
        INSTANCE = null;
        
        ClassMemberNavigatorJavaSourceFactory.getInstance().setLookup(Lookup.EMPTY, null);
        
    }

    public Lookup getLookup() {
        return this.getClassMemberPanelUI().getLookup();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(ClassMemberPanel.class,"LBL_members");
    }

    public String getDisplayHint() {
        return NbBundle.getMessage(ClassMemberPanel.class,"HINT_members");
    }

    public JComponent getComponent() {
        return getClassMemberPanelUI();
    }

    public void selectElement(ElementHandle<Element> eh) {
        getClassMemberPanelUI().selectElementNode(eh);
    }
    
    private synchronized ClassMemberPanelUI getClassMemberPanelUI() {
        if (this.component == null) {
            this.component = new ClassMemberPanelUI();
        }
        return this.component;
    }
    
    public static ClassMemberPanel getInstance() {
        return INSTANCE;
    }    
}
