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

package org.netbeans.modules.uml.diagrams.actions.sqd;

import java.awt.Point;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;


/**
 * messages can be moved only vertically, can't be moved above parent bounds but can be moved below
 * @author sp153251
 */
public class MessageMoveStrategy implements MoveStrategy {

    
    public MessageMoveStrategy() {
    }

    /**
     * Called after an user suggests a new location and before the suggested location is stored to a specified widget.
     * This allows to manipulate with a suggested location to perform snap-to-grid, locked-axis on any other movement strategy.
     * @param widget the moved widget
     * @param originalLocation the original location specified by the MoveProvider.getOriginalLocation method
     * @param suggestedLocation the location suggested by an user (usually by a mouse cursor position)
     * @return the new (optional modified) location processed by the strategy
     */
    public Point locationSuggested(Widget widget, Point originalLocation, Point suggestedLocation) {
        suggestedLocation.x=originalLocation.x;//only vertical
        return suggestedLocation;
    }

}
