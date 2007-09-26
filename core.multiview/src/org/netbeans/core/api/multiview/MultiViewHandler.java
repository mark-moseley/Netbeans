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

package org.netbeans.core.api.multiview;

import org.netbeans.core.multiview.MultiViewHandlerDelegate;

/**
 * A handler for the  multiview's {@link org.openide.windows.TopComponent}, obtainable via
 * {@link org.netbeans.core.spi.multiview.MultiViewFactory}, that allows
 * examination of Component's content and programatic changes in visible/activated elements.
 * @author  mkleint
 */
public final class MultiViewHandler {

    static {
        AccessorImpl.createAccesor();
    }

    private MultiViewHandlerDelegate del;

    MultiViewHandler(MultiViewHandlerDelegate delegate) {
        del = delegate;
    }
    /**
     * Returns the array of <code>MultiViewPerspective</code>s that the {@link org.openide.windows.TopComponent} is composed of.
     * @return array of defined perspectives.
     */
    public MultiViewPerspective[] getPerspectives() {
        return del.getDescriptions();
    }
    
    /**
     * Returns the currently selected <code>MultiViewPerspective</code> in the {@link org.openide.windows.TopComponent}.
     * It's element can be either visible or activated.
     * @return selected perspective
     */
    public MultiViewPerspective getSelectedPerspective() {
        return del.getSelectedDescription();
    }
    
    /**
     * returns the MultiViewElement for the given Description if previously created,
     * otherwise null.
     */
// SHOULD NOT BE USED, ONLY IN EMERGENCY CASE!    
//    public MultiViewPerspectiveComponent getElementForPerspective(MultiViewPerspective desc) {
//        return del.getElementForDescription(desc);
//    }
    
    /**
     * Requests focus for the <code>MultiViewPerspective</code> passed as parameter, if necessary
     * will switch from previously selected <code>MultiViewPerspective</code>
     * @param desc the new active selection
     */
    public void requestActive(MultiViewPerspective desc) {
        del.requestActive(desc);
    }
    
    /**
     * Changes the visible <code>MultiViewPerspective</code> to the one passed as parameter.
     * @param desc the new selection
     *
     */
    
    public void requestVisible(MultiViewPerspective desc) {
        del.requestVisible(desc);
    }
    
    
 
}
