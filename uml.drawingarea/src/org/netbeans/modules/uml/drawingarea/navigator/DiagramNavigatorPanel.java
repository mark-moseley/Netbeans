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
package org.netbeans.modules.uml.drawingarea.navigator;

import java.beans.PropertyChangeEvent;
import java.util.Collection;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jyothi
 */
public class DiagramNavigatorPanel implements NavigatorPanel, LookupListener
{

    private DiagramNavigatorContent navigator = null;
    private Lookup.Template < DesignerScene > template = 
            new Lookup.Template < DesignerScene >(DesignerScene.class);
    private Lookup.Result<DesignerScene> result;

    public DiagramNavigatorPanel()
    {
//        System.err.println(" #### DiagNAvi Panel constr...");
    }

    public String getDisplayName()
    {
        return NbBundle.getMessage(DiagramNavigatorPanel.class, "Navigator_DisplayName");
    }

    public String getDisplayHint()
    {
        return NbBundle.getMessage(DiagramNavigatorPanel.class, "Navigator_Hint");
    }

    public DiagramNavigatorContent getComponent()
    {
//        System.err.println(" getComponent() ");
        if (navigator == null)
        {
            navigator = new DiagramNavigatorContent();
        }
        return navigator;
    }

    private TopComponent getTopComponent()
    {
        TopComponent tc = WindowManager.getDefault().findTopComponent("UMLDiagramTopComponent");
        return tc;
    }

    public void panelActivated(Lookup context) 
    {
        getComponent();
        TopComponent.getRegistry().addPropertyChangeListener(navigator);
        TopComponent tc = getTopComponent();
        if (tc != null) 
        {
            result = tc.getLookup().lookup(template);
            result.addLookupListener(this);
            Collection c = result.allInstances();
            resultChanged(null);
            navigator.propertyChange(new PropertyChangeEvent(this,
                TopComponent.getRegistry().PROP_ACTIVATED_NODES,false,true));
            
        } 
    }

    public void panelDeactivated() {
        TopComponent.getRegistry().removePropertyChangeListener(navigator);
        
        if(result != null)
        {
            result.removeLookupListener(this);
            result = null;
        }
    }

    public Lookup getLookup()
    {
        return null;
    }

    public void resultChanged(LookupEvent ev)
    {
        Collection selected = result.allInstances();
//        System.err.println(" instances =  "+selected.size());
        if (selected.size() == 1)
        {
            DesignerScene dv = (DesignerScene) selected.iterator().next();
            navigator.navigate(dv.getSatelliteView());
        }
    }
}
