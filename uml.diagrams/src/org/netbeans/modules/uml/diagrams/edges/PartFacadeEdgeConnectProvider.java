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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.uml.diagrams.edges;

import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.constructs.IPartFacade;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.infrastructure.IConnectableElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IParameterableElement;
import org.netbeans.modules.uml.core.metamodel.structure.IComment;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.actions.SceneConnectProvider;
import org.netbeans.modules.uml.drawingarea.support.ProxyPresentationElement;
import org.openide.util.Lookup;

/**
 *
 */
public class PartFacadeEdgeConnectProvider extends SceneConnectProvider
{
    
    public PartFacadeEdgeConnectProvider(String defaultType)
    {
        super(defaultType, "");
    }

    @Override
    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget)
    {
        ConnectorState retVal = ConnectorState.REJECT;
        
        if(sourceWidget.getScene() instanceof ObjectScene)
        {
            ObjectScene scene = (ObjectScene)sourceWidget.getScene();
            IPresentationElement source = null;
            IPresentationElement target = null;
            Object srcObj = scene.findObject(sourceWidget);
            Object trgObj = scene.findObject(targetWidget);
            if (srcObj instanceof IPresentationElement) 
            {
                source = (IPresentationElement) srcObj;
            }
            if (trgObj instanceof IPresentationElement) 
            {
                target = (IPresentationElement) trgObj;
            }

            if(target != null)
            {
                if(scene.findWidget(target) != targetWidget)
                {
                    target = null;
                }
            }

            if((source != null) && (target != null))
            {
                IElement sourceElement = source.getFirstSubject();
                IElement targetElement = target.getFirstSubject();
                
                if( (sourceElement instanceof ICollaboration 
                     && targetElement instanceof IPartFacade) 
                    || (sourceElement instanceof IPartFacade
                        && targetElement instanceof ICollaboration))
                {
                        retVal = ConnectorState.ACCEPT;                    
                }
            }
        }
        
        return retVal;
    }
    
    @Override
    public void createConnection(Widget sourceWidget, Widget targetWidget)
    {
        if(sourceWidget.getScene() instanceof ObjectScene)
        {
            GraphScene scene = (GraphScene)sourceWidget.getScene();
            IPresentationElement source = (IPresentationElement) scene.findObject(sourceWidget);
            IPresentationElement target = (IPresentationElement) scene.findObject(targetWidget);

            if(target != null)
            {
                // Sometimes the mouse is over a feature when the mouse is 
                // release.  Since features have an object assocated to them
                // so the can be selected, we have to make sure we have a node.
                if(scene.isNode(target) == false)
                {
                    Widget parent = targetWidget.getParentWidget();
                    if(parent != null)
                    {
                        target = (IPresentationElement) scene.findObject(parent);
                    }
                }
            }

            if((source != null) && (target != null))
            {
                IElement sourceElement = source.getFirstSubject();
                IElement targetElement = target.getFirstSubject();
                
                ICollaboration pattern = null;
                IPartFacade role = null;
                if (sourceElement instanceof ICollaboration 
                    && targetElement instanceof IPartFacade) 
                {
                    pattern = (ICollaboration)sourceElement;
                    role = (IPartFacade)targetElement;
                } 
                else if (sourceElement instanceof IPartFacade
                    && targetElement instanceof ICollaboration) 
                {
                    pattern = (ICollaboration)targetElement;
                    role = (IPartFacade)sourceElement;
                }
                if((pattern != null) && (role != null))
                {
                    IConnectableElement pConnectableElement = (IConnectableElement)role;
                    IParameterableElement pParameterableElement = (IParameterableElement)role;

                    pattern.addTemplateParameter(pParameterableElement);
                    pattern.addRole(pConnectableElement);
                    
                    IPresentationElement edge = createNodePresentationElement(source, "PartFacadeEdge");
                    Widget edgeWidget = scene.addEdge(edge);
                    if((edgeWidget != null) && 
                       (scene.isNode(source) == true) && 
                       (scene.isNode(target) == true))
                    {
                        scene.setEdgeSource(edge, source);
                        scene.setEdgeTarget(edge, target);
                    }
                }
            }
        }
    }
    
    protected IPresentationElement createNodePresentationElement(IPresentationElement element, 
                                                                 String proxyType)
    {
        IPresentationElement retVal = new ProxyPresentationElement(element, proxyType);

        return retVal;
    }
}
