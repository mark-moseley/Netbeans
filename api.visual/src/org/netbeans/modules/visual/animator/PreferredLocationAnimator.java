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
package org.netbeans.modules.visual.animator;

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.animator.Animator;
import org.netbeans.api.visual.animator.SceneAnimator;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author David Kaspar
 */
public final class PreferredLocationAnimator extends Animator {

    private HashMap<Widget, Point> sourceLocations = new HashMap<Widget, Point> ();
    private HashMap<Widget, Point> targetLocations = new HashMap<Widget, Point> ();

    public PreferredLocationAnimator (SceneAnimator sceneAnimator) {
        super (sceneAnimator);
    }

    public void setPreferredLocation (Widget widget, Point preferredLocation) {
        assert widget != null;
        assert preferredLocation != null;
        sourceLocations.clear ();
        targetLocations.put (widget, preferredLocation);
        start ();
    }

    protected void tick (double progress) {
        for (Map.Entry<Widget, Point> entry : targetLocations.entrySet ()) {
            Widget widget = entry.getKey ();
            Point sourceLocation = sourceLocations.get (widget);
            if (sourceLocation == null) {
                sourceLocation = widget.getPreferredLocation ();
                if (sourceLocation == null) {
                    sourceLocation = widget.getLocation ();
                    if (sourceLocation == null) {
                        sourceLocation = new Point ();
                    }
                }
                sourceLocations.put (widget, sourceLocation);
            }
            Point targetLocation = entry.getValue ();
            if (targetLocation == null)
                continue;
            Point point;
            if (progress >= 1.0)
                point = targetLocation;
            else
                point = new Point (
                        (int) (sourceLocation.x + progress * (targetLocation.x - sourceLocation.x)),
                        (int) (sourceLocation.y + progress * (targetLocation.y - sourceLocation.y)));
            widget.setPreferredLocation (point);
        }
        if (progress >= 1.0) {
            sourceLocations.clear ();
            targetLocations.clear ();
        }
    }

}
