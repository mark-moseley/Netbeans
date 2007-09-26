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

package org.netbeans.modules.openide.explorer;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.util.Lookup;

/**
 * An architectural hack - until PropertySheet is separated and openide
 * split up, openide cannot depend on module code due to classloader
 * restrictions.  So we have an interface which will supply a bridge to
 * the tabcontrol code; an implementation of this interface is provided
 * over org.netbeans.swing.tabcontrol.TabbedContainer (in core/swing/tabcontrol)
 * by the window system which depends on it.
 *
 * @see org.netbeans.core.windows.view.ui.tabcontrol.TabbedContainerBridgeImpl
 * @author  Tim Boudreau
 */
public abstract class TabbedContainerBridge {
    
    protected TabbedContainerBridge(){};
    
    public static TabbedContainerBridge getDefault() {
        TabbedContainerBridge result = Lookup.getDefault().lookup (TabbedContainerBridge.class);
        if (result == null) {
            //unit test or standalone library operation
            return new TrivialTabbedContainerBridgeImpl();
        }
        return result;
    }

    public abstract JComponent createTabbedContainer();

    public abstract void setInnerComponent (JComponent container, JComponent inner);

    public abstract JComponent getInnerComponent(JComponent jc);

    public abstract Object[] getItems(JComponent jc);

    public abstract void setItems (JComponent jc, Object[] objects, String[] titles);

    public abstract void attachSelectionListener (JComponent jc, ChangeListener listener);

    public abstract void detachSelectionListener (JComponent jc, ChangeListener listener);

    public abstract Object getSelectedItem(JComponent jc);

    public abstract void setSelectedItem (JComponent jc, Object selection);

    public abstract boolean setSelectionByName(JComponent jc, String tabname);

    public abstract String getCurrentSelectedTabName(JComponent jc);

}
