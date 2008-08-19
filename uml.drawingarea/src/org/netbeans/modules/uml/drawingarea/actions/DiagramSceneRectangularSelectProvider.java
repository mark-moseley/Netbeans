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
 * Version 2 licens<DRAGGEDITEMS><MODELELEMENT TOPLEVELID="DCE.18CC44A7-3B18-9E24-B866-FCEF94A91442" XMIID="DCE.50CB1AE2-17B6-4DC6-DFA9-7F288366AC1C"/></DRAGGEDITEMS>e, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.Rectangle;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.visual.action.RectangularSelectProvider;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;

/**
 *
 * @author Sheryl Su
 */
public class DiagramSceneRectangularSelectProvider implements RectangularSelectProvider {

    private ObjectScene scene;

    public DiagramSceneRectangularSelectProvider (ObjectScene scene) {
        this.scene = scene;
    }

    public void performSelection (Rectangle sceneSelection) {
        
        if (scene instanceof DesignerScene)
        {
            DesignerScene designerScene = (DesignerScene) scene;
            Set < IPresentationElement > set = 
                    designerScene.getGraphObjectInRectangle(sceneSelection, true, true);
            
            Iterator<IPresentationElement> iterator = set.iterator ();
            scene.setFocusedObject (iterator.hasNext () ? iterator.next () : null);
            scene.userSelectionSuggested (set, false);
        }
        
    }


}
