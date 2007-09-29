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

package org.netbeans.modules.xml.xam.ui.actions;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.netbeans.modules.xml.xam.ui.cookies.ViewComponentCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Interface GotoType defines a place where an editor can be opened
 * for a particular component.
 *
 * @author  Nathan Fiedler
 */
public abstract class GotoType {

    /**
     * Finds the component that the given node represents. Subclasses may
     * wish to override this to retrieve the component in different manner.
     *
     * @param  node  the Node from which to get the component.
     * @return  the component, or null if none.
     */
    protected Component getComponent(Node node) {
        return XAMUtils.getComponent(node);
    }

    /**
     * Help context for the goto type.
     *
     * @return  the help context
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Returns the name of this type, to be displayed in a menu. Ideally
     * this should be the name of the view or editor that this type will
     * open the component in (e.g. "Design").
     *
     * @return  name of this type.
     */
    public String getName() {
        return NbBundle.getMessage(GotoType.class, "LBL_GoTo");
    }

    /**
     * Return the view in which this type will show the component.
     *
     * @return  component view.
     */
    protected abstract ViewComponentCookie.View getView();

    /**
     * Show the given node in the view this type represents.
     *
     * @param  node  the Node to be shown.
     */
    public void show(Node node) {
        Component comp = getComponent(node);
        ViewComponentCookie.View view = getView();
        ViewComponentCookie cookie = XAMUtils.getViewCookie(comp, view);
        if (cookie != null) {
            cookie.view(view, comp);
        }
    }
}
