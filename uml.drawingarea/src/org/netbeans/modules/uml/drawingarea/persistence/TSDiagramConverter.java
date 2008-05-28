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

package org.netbeans.modules.uml.drawingarea.persistence;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.netbeans.api.visual.widget.Widget;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.eventframework.IEventPayload;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.TSDiagramDetails;
import org.netbeans.modules.uml.core.metamodel.dynamics.Lifeline;
import org.netbeans.modules.uml.core.metamodel.dynamics.Message;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.FileExtensions;
import org.netbeans.modules.uml.core.support.umlutils.ElementLocator;
import org.netbeans.modules.uml.core.support.umlutils.IElementLocator;
import org.netbeans.modules.uml.drawingarea.AbstractLabelManager;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.SQDDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.UMLDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.actions.ActionProvider;
import org.netbeans.modules.uml.drawingarea.actions.AfterValidationExecutor;
import org.netbeans.modules.uml.drawingarea.actions.SQDMessageConnectProvider;
import org.netbeans.modules.uml.drawingarea.engines.DiagramEngine;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramEdgeReader;
import org.netbeans.modules.uml.drawingarea.persistence.data.EdgeInfo;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.persistence.data.NodeInfo;

import org.netbeans.modules.uml.drawingarea.support.ProxyPresentationElement;
import org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator.SQDDiagramEngineExtension;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget.UMLWidgetIDString;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerNode;
import org.netbeans.modules.uml.ui.support.DispatchHelper;
import org.netbeans.modules.uml.ui.support.diagramsupport.IDrawingAreaEventDispatcher;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 * @author Craig Conover, craig.conover@sun.com
 */
public class TSDiagramConverter
{
    public static final String ELEMENT = "ELEMENT";
    private static final String PRESENTATIONELEMENT = "PRESENTATION";
    private static final String SHOWMESSAGETYPE = "ShowMessageType";
    private static final String TSLABELTYPE = "TYPE";
    private static final String LIFELINESHIFTKEY = "LIFELINESHIFT";
    private static final String TSLABELPLACEMENT = "PLACEMENT";
    private final String GRAPHPROPERTY="graphicsType";
    private final String GRAPHICONIC="ETGenericNodeLabelUI";
    private final String GRAPHDEFAULT="ETGenericNodeUI";
    private final String ENGINE="engine";
    private final String PATTERNEDGEENGINE="PartFacadeEdgeDrawEngine";
    private final String PATTERNEDGEPRESENTATION="PartFacadeEdge";
    private final String NESTEDLINKENGINE="NestedLinkDrawEngine";
    private final String NESTEDLINKPRESENTATION="NestedLink";
    private final String NODECONNECTORS="NodeConnectors";
    private IProxyDiagram proxyDiagram;
    private TSDiagramDetails diagramDetails;
    
    private XMLInputFactory factory;
    private InputStream fisPres;
    private InputStream fisData;
    private XMLStreamReader readerPres;
    private XMLStreamReader readerData;
    
    // coordinate values for converting from TS graph to Meteora graph
    private int minX = Integer.MAX_VALUE;
    private int maxY = Integer.MIN_VALUE;
    private Integer sqdYShift=null;
    
    // data file node id (integer string) -> PEID
    private Map<String, String> dataNodeIdMap = new HashMap<String, String>(); 
    // data file edge id (integer string) -> PEID
    private Map<String, String> dataEdgeIdMap = new HashMap<String, String>(); 
    private Map<String, ETPairT<String, String>> dataConnIdMap = 
        new HashMap<String, ETPairT<String, String>>(); 
    
    private HashMap<String,HashMap<String,Object>> peidToLabelMap=new HashMap<String,HashMap<String,Object>>();
    
    // PEID -> NodeInfo
    private Map<String, NodeInfo> presIdNodeInfoMap = 
        new HashMap<String, NodeInfo>(); 

    // PEID -> EdgeInfo
    private Map<String, EdgeInfo> presIdEdgeInfoMap = 
        new HashMap<String, EdgeInfo>(); 
    private ArrayList<EdgeInfo> messagesInfo=new ArrayList<EdgeInfo>();

    private DesignerScene scene;
    private UMLDiagramTopComponent topComponent;

    private IProject project;
    private IElementLocator locator = new ElementLocator();
    
    // list of all presentation elements created
    private List<IPresentationElement> presEltList = 
        new ArrayList<IPresentationElement>();
    private ArrayList<Widget> widgetsList = 
        new ArrayList<Widget>();
    //
    private HashMap<String,ConnectorData> connectors=new HashMap<String,ConnectorData>();
    private Boolean ShowAllReturnMessages;
    private Boolean ShowMessageNumbers;

    private FileObject etldFO;

    private FileObject etlpFO;;
    
    public TSDiagramConverter(IProxyDiagram proxyDiagram)
    {
        this.proxyDiagram = proxyDiagram;
        diagramDetails = (TSDiagramDetails)proxyDiagram.getDiagramDetails();
    }

    public UMLDiagramTopComponent convertDiagram()
    {
        initialize();
        readXMLData();
        normilizeNodesForZero();
        readXMLPres();
        releaseResources();
        //
        createNodesPresentationElements();
        findEdgesElements();
        handleLabelsInfo(peidToLabelMap);
        if(diagramDetails.getDiagramType() == IDiagramKind.DK_SEQUENCE_DIAGRAM)
        {
            normalizeSQDDiagram();
        }
        createDiagram();
        return topComponent;
    }







    private void initialize()
    {
        try
        {
            project = getProject(diagramDetails.getDiagramProjectXMIID());
            
            factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);
            etldFO = FileUtil.toFileObject(new File(diagramDetails.getDiagramFileName()));
        
            etlpFO = FileUtil.findBrother(etldFO, FileExtensions.DIAGRAM_TS_PRESENTATION_EXT_NODOT);
            
            fisData = etldFO.getInputStream();
            readerData = factory.createXMLStreamReader(fisData, "UTF-8");

            fisPres = etlpFO.getInputStream();
            readerPres = factory.createXMLStreamReader(fisPres, "UTF-8");
        }
        
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        
        catch (FileNotFoundException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    
    private void createDiagram()
    {
        if(diagramDetails.getDiagramType() == IDiagramKind.DK_SEQUENCE_DIAGRAM)
        {
            topComponent = new SQDDiagramTopComponent(
                (INamespace) getElement(getProject(
                    diagramDetails.getDiagramProjectXMIID()), 
                    diagramDetails.getDiagramNamespaceXMIID()), 
                diagramDetails.getDiagramName(), 
                diagramDetails.getDiagramType());
            //
            
        }
        else
        {
            topComponent = new UMLDiagramTopComponent(
                (INamespace) getElement(getProject(
                    diagramDetails.getDiagramProjectXMIID()), 
                    diagramDetails.getDiagramNamespaceXMIID()), 
                diagramDetails.getDiagramName(), 
                diagramDetails.getDiagramType());
        }

        scene = topComponent.getScene();
        DiagramEngine engine = scene.getEngine();
        if(diagramDetails.getDiagramType() == IDiagramKind.DK_SEQUENCE_DIAGRAM)
        {
            engine.setSettingValue(SQDDiagramEngineExtension.SHOW_MESSAGE_NUMBERS, ShowMessageNumbers);
            engine.setSettingValue(SQDDiagramEngineExtension.SHOW_RETURN_MESSAGES, ShowAllReturnMessages);
        }
        //

        Collection<NodeInfo> ninfos = presIdNodeInfoMap.values();
        for (NodeInfo ninfo : ninfos)
        {
            addNodeToScene(ninfo);
        }
        addEdgestWithValidationWait();
        scene.validate();
   }
    
    private void addEdgestWithValidationWait()
    {
        new AfterValidationExecutor(new ActionProvider() {

            public void perfomeAction() {
                if(diagramDetails.getDiagramType() == IDiagramKind.DK_SEQUENCE_DIAGRAM)
                {
                    addEdgesSequenceDiagram();
                }
                else
                {
                    addEdgesGeneral();
                }
                processContainmentWithValidationWait();
                scene.validate();
            }
        },scene);
    }
    private void processContainmentWithValidationWait()
    {
        new AfterValidationExecutor(new ActionProvider() {

            public void perfomeAction() {
                for(Widget w:widgetsList)
                {
                    if(w instanceof ContainerNode && w instanceof UMLNodeWidget)
                    {
                         UMLNodeWidget cont=(UMLNodeWidget) w;
                         cont.getResizeStrategyProvider().resizingStarted(cont);
                         cont.getResizeStrategyProvider().resizingFinished(cont);
                    }
                }
                scene.validate();
                try {
                    scene.getDiagram().save();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                processArchiveWithValidationWait();
                scene.revalidate();
                scene.validate();
            }
        }, scene);
    }
    
    private void processArchiveWithValidationWait()
    {
        new AfterValidationExecutor(new ActionProvider() {

            public void perfomeAction() {
                archiveTSDiagram();
            }
        }, scene);
    }     
    
    
    private void addEdgesGeneral()
    {
        Collection<EdgeInfo> einfos = presIdEdgeInfoMap.values();
        for (EdgeInfo einfo : einfos)
        {
            Widget connWidget=addEdgeToScene(einfo);
            if (connWidget != null && connWidget instanceof UMLEdgeWidget)
            {
                ((UMLEdgeWidget) connWidget).load(einfo);
            }
        }
    }
    
    private void addEdgesSequenceDiagram()
    {
        Collection<EdgeInfo> einfos = presIdEdgeInfoMap.values();
        for (EdgeInfo einfo : einfos)
        {
            addEdgeToScene(einfo);
        }
        if(messagesInfo.size()>0)addMessagesToSQD(messagesInfo);
    }
    
    private void archiveTSDiagram()
    {
        FileObject projectFO = etldFO.getParent();
        
        try
        {
            FileObject backupFO = 
                FileUtil.createFolder(projectFO, "DiagramBackup");
            FileObject parent=etldFO.getParent();
            String path=etldFO.getPath();
            String path2=etlpFO.getPath();
            String parpath=parent.getPath();
            FileUtil.moveFile(etldFO, backupFO, etldFO.getName());
            FileUtil.moveFile(etlpFO, backupFO, etlpFO.getName());
            IDrawingAreaEventDispatcher dispatcher = getDispatcher();
            if (dispatcher != null) {
                IEventPayload payload = dispatcher.createPayload("DrawingAreaFileRemoved");
                dispatcher.fireDrawingAreaFileRemoved(diagramDetails.getDiagramFileName(), payload);
                dispatcher.fireDrawingAreaFileRemoved(path2, payload);
            }
            //FileUtil.refreshFor(new File(parpath));
        }
        
        catch (DataObjectNotFoundException ex)
        {
            Exceptions.printStackTrace(ex);
            ex.printStackTrace();
        }

        catch (IOException ex)
        {
            Exceptions.printStackTrace(ex);
            ex.printStackTrace();
        }
    }

    
    public IProject getProject(String xmiID)
    {
        IApplication app = ProductHelper.getApplication();
        project = (app != null) ? app.getProjectByID(xmiID) : null;
        return project;
    }

    private IElement getElement(IProject project, String sModelElementID)
    {
        IElement element = null;
        if (project != null)
        {
            element = locator.findElementByID(project, sModelElementID);
        }
        return element;
    }

    private void addNodeToScene(NodeInfo nodeInfo)
    {
        try
        {            
            IPresentationElement presEl = (IPresentationElement) nodeInfo.getProperty(PRESENTATIONELEMENT);
            if(presEl!=null)
            {
                DiagramEngine engine = scene.getEngine();
                Widget widget = null;
                if(presEl!=null && presEl.getFirstSubject() instanceof INamedElement)widget=engine.addWidget(presEl, nodeInfo.getPosition());
                if(widget!=null)
                {
                    postProcessNode(widget,presEl);
                    //add this PE to the presLIst
                    widgetsList.add(widget);
                    if (widget!=null && widget instanceof UMLNodeWidget)
                        ((UMLNodeWidget) widget).load(nodeInfo);
                }
                else
                {
                    //most likely unsupported widgets and it wasn't created
                    //or not named element (for example expression
                    presEl.getFirstSubject().removePresentationElement(presEl);
                    presEltList.remove(presEl);
                    nodeInfo.getProperties().remove(PRESENTATIONELEMENT);
                }
            }
        }

        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private boolean createNodesPresentationElements()
    {
        Collection<NodeInfo> ninfos = presIdNodeInfoMap.values();
        boolean good=true;
        for (NodeInfo ninfo : ninfos)
        {
            good=good&&createPE(ninfo);
        }        
        return good;
    }
    
    private boolean createPE(NodeInfo nodeInfo)
    {
        boolean good=true;
        try
        {            
             IPresentationElement presEl = createPresentationElement(
             nodeInfo.getMEID(), nodeInfo.getPEID());
             good=presEl!=null;
             if(good)
             {
                mapEngineToView(presEl,nodeInfo);
                nodeInfo.setProperty(PRESENTATIONELEMENT, presEl);
                presEltList.add(presEl);
             }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return good;
    }

    private Widget addEdgeToScene(EdgeInfo edgeReader)
    {
        Widget connWidget = null;
        IPresentationElement pE = null;
        IPresentationElement proxyPE = null;
        
        //
        String sourceId=dataConnIdMap.get(edgeReader.getPEID()).getParamOne();
        String targetId=dataConnIdMap.get(edgeReader.getPEID()).getParamTwo();
        IPresentationElement sourcePE=findNode(sourceId);
        IPresentationElement targetPE=findNode(targetId);
        if(sourcePE==null || targetPE==null)return null;//target or source is missed from model
        edgeReader.setSourcePE(sourcePE);
        edgeReader.setTargetPE(targetPE);
        //
        processSemanricPresentation(edgeReader);
        //
        IElement elt = (IElement) edgeReader.getProperty(ELEMENT);
        if (elt == null)
        {
            //there is nothing to add.. so return..
            return null;
        }
        else if(elt instanceof Message)
        {
            messagesInfo.add(edgeReader);
            return null;
        }

            pE = Util.createNodePresentationElement();
    //            pE.setXMIID(PEID);
            pE.addSubject(elt);
            String proxyType = edgeReader.getSemanticModelBridgePresentation();
            if (proxyType.trim().length() > 0 && !proxyType.equalsIgnoreCase(""))
            {
                proxyPE = new ProxyPresentationElement(pE, proxyType);
            }
            if (proxyPE != null)
            {
                connWidget = scene.addEdge(proxyPE);
            } else
            {
                connWidget = scene.addEdge(pE);
            }
        return connWidget;
    }


    /**
     * need to be called after general addEfge(where all source/target widgets are determined)
     * also all elements in collection should be messages
     * @return
     */
    private void addMessagesToSQD(ArrayList<EdgeInfo> einfos)
    {
        Collections.sort(einfos,new Comparator<EdgeInfo>()
        {
            public int compare(EdgeInfo o1, EdgeInfo o2) {
                //first check special null cases
                IPresentationElement sourcePE1=o1.getSourcePE();
                IPresentationElement sourcePE2=o2.getSourcePE();
                if(sourcePE1==null && sourcePE2==null)return 0;
                else if(sourcePE1==null)return -1;
                else if(sourcePE2==null)return 1;
                NodeInfo sourceInfo1=presIdNodeInfoMap.get(sourcePE1.getXMIID());
                ConnectorData sourceConnector1=(ConnectorData) o1.getProperty("SOURCECONNECTOR");
                NodeInfo sourceInfo2=presIdNodeInfoMap.get(sourcePE2.getXMIID());
                ConnectorData sourceConnector2=(ConnectorData) o2.getProperty("SOURCECONNECTOR");
                int y1=0,y2=0;
                if(o1.getSourcePE().getFirstSubject() instanceof Lifeline)
                {
                    boolean actor=((Lifeline) o1.getSourcePE().getFirstSubject()).getIsActorLifeline();
                    int y_level=actor ? SQDDiagramEngineExtension.DEFAULT_ACTORLIFELINE_Y : SQDDiagramEngineExtension.DEFAULT_LIFELINE_Y;
                    y1= y_level+sourceConnector1.getY() - (Integer) sourceInfo1.getProperty(LIFELINESHIFTKEY);//connector is relative to lifeline, also need to count on shift from initial different lvel locations
                }
                else
                {
                    //
                    Widget sourceWidget = scene.findWidget(o1.getSourcePE());
                    y1=sourceWidget.getPreferredLocation().y+sourceConnector1.getY();//consider it may be cf only/interaction and all entire area is used
                }
               if(o2.getSourcePE().getFirstSubject() instanceof Lifeline)
                {
                    boolean actor=((Lifeline) o2.getSourcePE().getFirstSubject()).getIsActorLifeline();
                    int y_level=actor ? SQDDiagramEngineExtension.DEFAULT_ACTORLIFELINE_Y : SQDDiagramEngineExtension.DEFAULT_LIFELINE_Y;
                    y2= y_level+sourceConnector2.getY() - (Integer) sourceInfo2.getProperty(LIFELINESHIFTKEY);//connector is relative to lifeline, also need to count on shift from initial different lvel locations
                }
                else
                {
                    //
                    Widget sourceWidget = scene.findWidget(o2.getSourcePE());
                    y2=sourceWidget.getPreferredLocation().y+sourceConnector2.getY();
                }
                
                
                return y1-y2;
            }
        });
        
        for(int index1=0;index1<einfos.size();index1++)
        {
            EdgeInfo edgeInfo=einfos.get(index1);
            IElement elt = (IElement) edgeInfo.getProperty(ELEMENT);
            if(elt==null)continue;//skip absent model element
            Message message=(Message) elt;
            if(message.getKind()==Message.MK_RESULT)
            {
                continue;//result is created with call message at the same time
            }
            IPresentationElement pE = Util.createNodePresentationElement();
    //            pE.setXMIID(PEID);
            IPresentationElement sourcePE=edgeInfo.getSourcePE();
            IPresentationElement targetPE=edgeInfo.getTargetPE();
            if(sourcePE==null || targetPE==null)continue;//skip if source or target is missed
            Widget sourceWidget = scene.findWidget(sourcePE);
            Widget targetWidget = scene.findWidget(targetPE);
            NodeInfo sourceInfo=presIdNodeInfoMap.get(edgeInfo.getSourcePE().getXMIID());
            NodeInfo targetInfo=presIdNodeInfoMap.get(edgeInfo.getTargetPE().getXMIID());
            pE.addSubject(elt);
            DiagramEngine engine=scene.getEngine();
            SQDDiagramEngineExtension sqdengine=(SQDDiagramEngineExtension) engine;
            SQDMessageConnectProvider provider = null;
            List retVal = new ArrayList();
            Message returnMsg = null;
            EdgeInfo resultInfo=null;
            Point startingPoint=null, endingPoint=null, resultStartingPoint=null, resultEndingPoint=null;
            ConnectorData sourceConnector=(ConnectorData) edgeInfo.getProperty("SOURCECONNECTOR");
            ConnectorData targetConnector=(ConnectorData) edgeInfo.getProperty("TARGETCONNECTOR");
            //
            //
            if(edgeInfo.getSourcePE().getFirstSubject() instanceof Lifeline)
            {
                boolean actor=((Lifeline) edgeInfo.getSourcePE().getFirstSubject()).getIsActorLifeline();
                int y_level=actor ? SQDDiagramEngineExtension.DEFAULT_ACTORLIFELINE_Y : SQDDiagramEngineExtension.DEFAULT_LIFELINE_Y;
                int x=sourceWidget.getPreferredLocation().x+sourceWidget.getMinimumSize().width/2;
                int y= y_level+sourceConnector.getY() - (Integer) sourceInfo.getProperty(LIFELINESHIFTKEY);//connector is relative to lifeline, also need to count on shift from initial different lvel locations
                startingPoint=new Point(x,y);
            }
            else
            {
                //yet unsupported, y may be from preferred location but x need to be archived from data
                int y=sourceWidget.getPreferredLocation().y+sourceConnector.getY();
                int x=sourceWidget.getPreferredLocation().x+(sourceConnector.getProportionalOffetX()>0 ? sourceWidget.getBounds().width : 0);
                startingPoint=new Point(x,y);
            }
            if(edgeInfo.getTargetPE().getFirstSubject() instanceof Lifeline)
            {
                int x=targetWidget.getPreferredLocation().x+targetWidget.getMinimumSize().width/2;
                //int y= targetConnector.getY() - (Integer) targetInfo.getProperty(LIFELINESHIFTKEY);
                int y=startingPoint.y;
                if(sourceWidget==targetWidget)y+=10;
                endingPoint=new Point(x,y);
            }
            else
            {
                //yet unsupported
                int y=startingPoint.y;
                int x=targetWidget.getPreferredLocation().x+(targetConnector.getProportionalOffetX()>0 ? targetWidget.getBounds().width : 0);
                endingPoint=new Point(x,y);
            }
            ArrayList<Point> wayPoints=new ArrayList<Point>();
            wayPoints.add(startingPoint);
            wayPoints.add(endingPoint);
            edgeInfo.setWayPoints(wayPoints);
            //
            if (message.getKind() == Message.MK_SYNCHRONOUS)
            {
                //now find the result message for this call message
                for(EdgeInfo edgeInfo2:einfos)
                {
                    Message resTmp= (Message) edgeInfo2.getProperty(ELEMENT);
                    if(resTmp.getKind()==Message.MK_RESULT)
                    {
                        if(resTmp.getSendingMessage()==message)
                        {
                            returnMsg=resTmp;
                            resultInfo=edgeInfo2;
                            break;
                        }
                    }
                }
                if (returnMsg != null)
                {
                    //create the sync msg and delete it from the edgeInfoList
                    provider = sqdengine.getConnectProvider(message, returnMsg);
                    //get the returnMsg info from the edgeInfoList
                    if (resultInfo != null) {
                        ConnectorData sourceResultConnector=(ConnectorData) resultInfo.getProperty("SOURCECONNECTOR");
                        ConnectorData targetResultConnector=(ConnectorData) resultInfo.getProperty("TARGETCONNECTOR");
                        
                        if(resultInfo.getSourcePE().getFirstSubject() instanceof Lifeline)
                        {
                            boolean actor=((Lifeline) resultInfo.getSourcePE().getFirstSubject()).getIsActorLifeline();
                            int y_level=actor ? SQDDiagramEngineExtension.DEFAULT_ACTORLIFELINE_Y : SQDDiagramEngineExtension.DEFAULT_LIFELINE_Y;
                            int x=targetWidget.getPreferredLocation().x+targetWidget.getMinimumSize().width/2;
                            int y= y_level+sourceResultConnector.getY() - (Integer) targetInfo.getProperty(LIFELINESHIFTKEY);
                            resultStartingPoint=new Point(x,y);
                        }
                        else
                        {
                            //yet unsupported
                            int y=targetWidget.getPreferredLocation().y+sourceResultConnector.getY();
                            int x=targetWidget.getPreferredLocation().x + (sourceResultConnector.getProportionalOffetX()>0 ? targetWidget.getBounds().width : 0);
                            resultStartingPoint=new Point(x,y);
                        }
                        if(resultInfo.getTargetPE().getFirstSubject() instanceof Lifeline)
                        {
                            int x=sourceWidget.getPreferredLocation().x+sourceWidget.getMinimumSize().width/2;
                            //int y= targetResultConnector.getY() - (Integer) targetInfo.getProperty(LIFELINESHIFTKEY);
                            int y=resultStartingPoint.y;
                            if(sourceWidget==targetWidget)y+=10;
                            resultEndingPoint=new Point(x,y);
                        }
                        else
                        {
                            //yet unsupported
                            int y=resultStartingPoint.y;
                            int x=sourceWidget.getPreferredLocation().x + (targetResultConnector.getProportionalOffetX()>0 ? sourceWidget.getBounds().width : 0);
                            resultEndingPoint=new Point(x,y);
                        }
                        
                        ArrayList<Point> resultWayPoints=new ArrayList<Point>();
                        resultWayPoints.add(resultStartingPoint);
                        resultWayPoints.add(resultEndingPoint);
                        resultInfo.setWayPoints(resultWayPoints);
                        
                        retVal = (List)provider.createSynchConnection(sourceWidget, targetWidget, startingPoint, endingPoint, resultStartingPoint, resultEndingPoint);
                    }
                }
            } 
            else if ((message.getKind() == Message.MK_ASYNCHRONOUS) 
                    || (message.getKind() == Message.MK_CREATE))
            {
                provider = sqdengine.getConnectProvider(message, null);
                retVal = provider.createConnection(sourceWidget, targetWidget, startingPoint, endingPoint);                    
            } 
            scene.validate();
            for (int i = 0; i < retVal.size(); i++)
            {
                Object object = retVal.get(i);
                if (object instanceof DiagramEdgeReader)
                {
                    if (object instanceof UMLEdgeWidget && 
                            (((UMLEdgeWidget)object).getWidgetID()).equalsIgnoreCase(UMLWidgetIDString.RESULTMESSAGECONNECTIONWIDGET.toString())) {
                        ((DiagramEdgeReader)object).load(resultInfo);
                    }
                    else {
                        ((DiagramEdgeReader)object).load(edgeInfo);
                    }                        
                }

            }
        }
    }
    
    private void findEdgesElements() {
        Collection<EdgeInfo> einfos = presIdEdgeInfoMap.values();
        for (EdgeInfo einfo : einfos)
        {
            IElement elt = getElement(project, einfo.getMEID());
            if(elt!=null)einfo.setProperty( ELEMENT,elt);
        }
    }
   
    /**
     * make lowest x equal to zero(with some shift), revert y and make lowest y equal to zero(with some shift)
     */
    private void normilizeNodesForZero() {
        Collection<NodeInfo> ninfos = presIdNodeInfoMap.values();
        int margin=60;
        for (NodeInfo ninfo : ninfos)
        {
            Point loc=ninfo.getPosition();
            loc.x-=minX-margin;
            loc.y=maxY+margin-loc.y;
            ninfo.setPosition(loc);
        }
    }
    
    /**
     * this method handle positioning of all lifelines on the same level
     */
    private void normalizeSQDDiagram()
    {
        Collection<NodeInfo> ninfos = presIdNodeInfoMap.values();
        int topLifelineY=Integer.MAX_VALUE;
        for (NodeInfo ninfo : ninfos)//determine top Y
        {
            IPresentationElement pres=(IPresentationElement) ninfo.getProperty(PRESENTATIONELEMENT);
            if(pres!=null && pres.getFirstSubject() instanceof Lifeline)
            {
                Lifeline ll=(Lifeline) pres.getFirstSubject();
                Point loc=new Point(ninfo.getPosition());
                boolean actor=ll.getIsActorLifeline();
                int y_level=actor ? SQDDiagramEngineExtension.DEFAULT_ACTORLIFELINE_Y : SQDDiagramEngineExtension.DEFAULT_LIFELINE_Y;
                int diff=y_level-loc.y;
                loc.y=y_level;
                ninfo.setPosition(loc);
                Dimension size=ninfo.getSize();
                size.height-=diff;//correct length
                ninfo.setSize(size);
                ninfo.setProperty( LIFELINESHIFTKEY,diff);
            }
        }
    }

    private void processSemanricPresentation(EdgeInfo edgeReader) {
        String  engine=(String) edgeReader.getProperty(ENGINE);
        if(PATTERNEDGEENGINE.equals(engine))
        {
            edgeReader.setSemanticModelBridgePresentation(PATTERNEDGEPRESENTATION);
        }
        else if(NESTEDLINKENGINE.equals(engine))
        {
            edgeReader.setSemanticModelBridgePresentation(NESTEDLINKPRESENTATION);
        }
    }

    private void shiftNodesByY(int shift)
    {
        Collection<NodeInfo> ninfos = presIdNodeInfoMap.values();
        for (NodeInfo ninfo : ninfos)
        {
            Point loc=ninfo.getPosition();
            loc.y+=shift;
            ninfo.setPosition(loc);
        }
    }
    
    private void releaseResources()
    {
        try
        {
            readerPres.close();
            fisPres.close();
            readerData.close();
            fisData.close();
        }
        
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        
        catch (IOException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    private void readXMLPres()
    {
        try
        {
            int event = readerPres.getEventType();
            while (true)
            {
                switch (event)
                {
                    case XMLStreamConstants.START_ELEMENT:
                        handlePresStartElement();
                        break;
                        
                    case XMLStreamConstants.END_DOCUMENT:
                        readerPres.close();
                        break;
                }

                if (!readerPres.hasNext())
                    break;
                
                event = readerPres.next();
            }
        }
        
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        
        catch (Exception ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void handlePresStartElement()
    {
        if (readerPres.getName().getLocalPart().startsWith("DCE."))
        {
            String PEID = readerPres.getAttributeValue(null, "PEID");
            NodeInfo ninfo = presIdNodeInfoMap.get(PEID);
            
            if (ninfo != null)
            {
                //node
                ninfo.setMEID(readerPres.getAttributeValue(null, "MEID"));
                //
                getEngineFromPres(readerPres,ninfo);
                //
                presIdNodeInfoMap.put(PEID, ninfo);
            }
            else
            {
                EdgeInfo einfo = presIdEdgeInfoMap.get(PEID);
                
                if (einfo != null)
                {
                    //edge
                    einfo.setMEID(readerPres.getAttributeValue(null, "MEID"));
                    getEngineFromPres(readerPres,einfo);
                    presIdEdgeInfoMap.put(PEID, einfo);
                }
                else if(peidToLabelMap.get(PEID)!=null)
                {
                    //label
                    getLabelInfoFromPres(readerPres, peidToLabelMap.get(PEID));
                }
            }
        }
        else if(readerPres.getName().getLocalPart().startsWith("diagramInfo"))
        {
            ShowAllReturnMessages=(Boolean) Boolean.parseBoolean(readerPres.getAttributeValue(null, "ShowAllReturnMessages"));
            ShowMessageNumbers=(Boolean) Boolean.parseBoolean(readerPres.getAttributeValue(null, "ShowMessageNumbers"));
        }
    }
    
    private void postProcessNode(Widget widget, IPresentationElement presEl) {
        //
    }
    
    private ConnectorData handleConnectorInNode(XMLStreamReader readerData, HashMap<String,ConnectorData> connectors) {
        try {
            String startWith = readerData.getLocalName();
            ConnectorData connectorData=new ConnectorData();
            connectorData.setId( readerData.getAttributeValue(null, "id"));
            //we need to go deeper and exit on the same node, or can exit before on place we found necessary info
            while (readerData.hasNext() && !(readerData.isEndElement() && readerData.getLocalName().equals(startWith))) {
                if(readerData.isStartElement() && readerData.getLocalName().equals("constantOffset"))
                {
                    connectorData.setY((int) -Double.parseDouble(readerData.getAttributeValue(null, "y")));//invert Y because of coordinate system changes
                }
                else if(readerData.isStartElement() && readerData.getLocalName().equals("proportionalOffset"))
                {
                    connectorData.setProportionalOffetX(Double.parseDouble(readerData.getAttributeValue(null, "x")));
                }
                readerData.next();
            }
            connectors.put(connectorData.getId()+"",connectorData);
            return connectorData;
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    private String getEngineFromPres(XMLStreamReader readerPres,NodeInfo ninfo) {
        try {
            String startWith = readerPres.getLocalName();
            //we need to go deeper and exit on the same node, or can exit before on place we found necessary info
            while (readerPres.hasNext() && !(readerPres.isEndElement() && readerPres.getLocalName().equals(startWith))) {
                if(readerPres.isStartElement() && readerPres.getLocalName().equals("engine"))
                {
                    ninfo.setProperty(ENGINE, readerPres.getAttributeValue(null, "name"));
                    return            (String) ninfo.getProperty(ENGINE);
                }
                readerPres.next();
            }
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        //
        return null;
    }
    private String getEngineFromPres(XMLStreamReader readerPres, EdgeInfo einfo) {
        try {
            String startWith = readerPres.getLocalName();
            //we need to go deeper and exit on the same node, or can exit before on place we found necessary info
            while (readerPres.hasNext() && !(readerPres.isEndElement() && readerPres.getLocalName().equals(startWith))) {
                if(readerPres.isStartElement() && readerPres.getLocalName().equals("engine"))
                {
                    einfo.setProperty(ENGINE, readerPres.getAttributeValue(null, "name"));
                    String messageShowType=readerPres.getAttributeValue(null,SHOWMESSAGETYPE);
                    if(messageShowType!=null)einfo.setProperty( SHOWMESSAGETYPE,messageShowType);
                    return            (String) einfo.getProperty(ENGINE);
                }
                readerPres.next();
            }
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        //
        return null;
    }
    
    private void getLabelInfoFromPres(XMLStreamReader readerPres,HashMap<String,Object> labelInfo)
    {
        String startWith = readerPres.getLocalName();
        //we need to go deeper and exit on the same node, or can exit before on place we found necessary info
        String type=readerPres.getAttributeValue(null, "TSLabelKind");
        String placement=readerPres.getAttributeValue(null, "TSLabelPlacementKind");
        labelInfo.put( TSLABELTYPE,type);
        labelInfo.put( TSLABELPLACEMENT,placement);
    }
    
    /**
     * should add all labels info to edges info
     * @param peidToLabelMap
     */
    private void handleLabelsInfo(HashMap<String, HashMap<String, Object>> peidToLabelMap) {
        for(String key:peidToLabelMap.keySet())
        {
            HashMap<String,Object> labelInfo=peidToLabelMap.get(key);
            int tsType=Integer.parseInt((String) labelInfo.get(TSLABELTYPE));
            Dimension size=(Dimension) labelInfo.get("SIZE");
            String typeInfo=null;
            EdgeInfo edgeInfo=(EdgeInfo) labelInfo.get("EDGE");
            boolean endLabel=false;
            LabelManager.LabelType type=null;
            switch(tsType)
            {
                case 1://for names of smth on all diagrams
                case 12://for names of smth on all diagrams
                //case 7://for sqd message
                    typeInfo=AbstractLabelManager.NAME;
                    break;
                case 7://for sqd message operations
                    String msgShowType=(String) edgeInfo.getProperty(SHOWMESSAGETYPE);
                    if("0".equals(msgShowType))
                        typeInfo=AbstractLabelManager.OPERATION;
                    else if("1".equals(msgShowType))
                        typeInfo=AbstractLabelManager.NAME;
                    break;
                case 6:
                    typeInfo=AbstractLabelManager.STEREOTYPE;
                    break;
                case 13:
                    //derivation specification
                    typeInfo=AbstractLabelManager.BINDING;
                    break;
                case 4:
                    //target association name
                    endLabel=true;
                    typeInfo=AbstractLabelManager.NAME;
                    type=LabelManager.LabelType.TARGET;
                    break;
                case 2:
                    //source association name
                    endLabel=true;
                    typeInfo=AbstractLabelManager.NAME;
                    type=LabelManager.LabelType.SOURCE;
                    break;
                case 5:
                    //some end multiplicity
                    endLabel=true;
                    typeInfo=AbstractLabelManager.MULTIPLICITY;
                    type=LabelManager.LabelType.TARGET;
                    break;
                case 3:
                    //other end multiplicity
                    endLabel=true;
                    typeInfo=AbstractLabelManager.MULTIPLICITY;
                    type=LabelManager.LabelType.SOURCE;
                    break;
                case 10:
                    //guard condition(activity)
                    typeInfo=AbstractLabelManager.GUARD_CONDITION;
                    break;
                case 16:
                    //pre consition(state)
                    System.out.println("***WARNING: unsupported precondition label was skipped");
                    break;
                case 17:
                    //post condition(state)
                    System.out.println("***WARNING: unsupported postcondition label was skipped");
                    break;
                default:
                    throw new UnsupportedOperationException("Converter can't handle label kind: "+tsType);
            }
            System.out.println("LABEL: "+typeInfo+":"+type);
            if(typeInfo==null)continue;//unsupported yet
            if(endLabel)
            {
                EdgeInfo.EndDetails endDet = null;
                //need source and target id for association
                IElement elt=(IElement) edgeInfo.getProperty(ELEMENT);
                String sourceID=null;
                String targetID=null;
                if(elt!=null && elt instanceof IAssociation)
                {
                    IAssociation ass=(IAssociation) elt;
                    sourceID=ass.getEndAtIndex(0).getXMIID();
                    targetID=ass.getEndAtIndex(1).getXMIID();
                }
                else
                {
                    continue;//support only associations (now?)
                }
                //
                String requiredID=null;
                if(type.equals(LabelManager.LabelType.TARGET))
                {
                    requiredID=(targetID);
                }
                else if(type.equals(LabelManager.LabelType.SOURCE))
                {
                    requiredID=(sourceID);
                }
                //
                for(EdgeInfo.EndDetails tmp:edgeInfo.getEnds())
                {
                    if(tmp.getID().equals(requiredID))
                    {
                        endDet=tmp;
                        break;
                    }
                }
                if(endDet==null)
                {
                    endDet = edgeInfo.new EndDetails();
                    edgeInfo.getEnds().add(endDet);
                }
                endDet.setID(requiredID);
                edgeInfo.setHasContainedElements(true);
                EdgeInfo.EdgeLabel label=edgeInfo.new EdgeLabel();
                label.setLabel(typeInfo);
                label.setSize(size);
                endDet.getEndEdgeLabels().add(label);
            }
            else
            {
                edgeInfo.setHasContainedElements(true);
                EdgeInfo.EdgeLabel label=edgeInfo.new EdgeLabel();
                label.setLabel(typeInfo);
                label.setSize(size);
                //label.setPosition(null);
                edgeInfo.getLabels().add(label);
            }
        }
    }
    
    private void mapEngineToView(IPresentationElement presEl,NodeInfo ninfo) {
        if(ninfo.getViewName()!=null)return;//only if view wasn't set from node before
        String engine=(String) ninfo.getProperty(ENGINE);
        if(engine!=null)//try from engine
        {
            if(engine.equals("ClassRobustnessDrawEngine"))//iconic viw for robustness classes, now need to find robustness kind
            {
                //let use first stereotype here(looks like approach in 6.1)
                if(presEl.getFirstSubject().getAppliedStereotypes().size()>0)
                {
                    ninfo.setViewName(presEl.getFirstSubject().getAppliedStereotypesAsString().get(0));
                }
            }
            else if(engine.equals("InterfaceDrawEngine"))
            {
                ninfo.setViewName("lollipop");
            }
        }
        if(ninfo.getViewName()!=null)return;//only if view wasn't set from node before
    }

    private IPresentationElement createPresentationElement(
        String meid, String peid)
    {
        IElement element = getElement(project, meid);
        if(element==null)return null;
        IPresentationElement presEl = Util.createNodePresentationElement();
        presEl.setXMIID(peid);
        presEl.addSubject(element);
        return presEl;
    }


    private void readXMLData()
    {
        try
        {
            int event = readerData.getEventType();
            while (true)
            {
                switch (event)
                {
                    case XMLStreamConstants.START_ELEMENT:
                        handleDataStartElement();
                        break;
                        
                    case XMLStreamConstants.END_DOCUMENT:
                        readerData.close();
                        break;
                }

                if (!readerData.hasNext())
                    break;
                
                event = readerData.next();
            }
        }
        
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }

    
    
    private void handleDataStartElement()
    {
        if (readerData.getName().getLocalPart().equalsIgnoreCase("node"))
        {
            handleNode();
        }

        if (readerData.getName().getLocalPart().equalsIgnoreCase("edge"))
        {
                handleEdge();
        }
    }
    
    
    private void handleNode()
    {
        String nodeId = null;
        Point position = null;
        Dimension size = null;
        String PEID = null;
        String graphicsType=null;
        try
        {
            nodeId = readerData.getAttributeValue(null, "id");
            HashMap<String,ConnectorData> nodeConnectors=new HashMap<String,ConnectorData>();
            while (readerData.hasNext())
            {
                if (readerData.next() == XMLStreamConstants.START_ELEMENT)
                {
                    //we are only intersted in data of particular start elements
                    if(readerData.getName().getLocalPart()
                        .equalsIgnoreCase("connector"))
                    {
                        ConnectorData connector=handleConnectorInNode(readerData,connectors);
                        nodeConnectors.put(connector.getId()+"", connector);
                    }
                    else if (readerData.getName().getLocalPart()
                        .equalsIgnoreCase("center") && position==null)//second position may be corresponding label position
                    {
                        int x = Float.valueOf(readerData
                            .getAttributeValue(null, "x")).intValue();
                        
                        int y = Float.valueOf(readerData
                            .getAttributeValue(null, "y")).intValue();
                            
                        
                        // minX and maxY not final yet so keep use coords here;
                        // recalc all points as nodes are added to new diagram
                        position = new Point(x, y);
                    }
                    
                    else if (readerData.getName().getLocalPart()
                        .equalsIgnoreCase("size") && size==null)//second size may be corresponding label size
                    {
                        size = new Dimension(
                            Float.valueOf(readerData
                                .getAttributeValue(null, "width")).intValue(),
                            Float.valueOf(readerData
                                .getAttributeValue(null, "height")).intValue());
                    }

                    else if (readerData.getName().getLocalPart()
                        .equalsIgnoreCase("PEID") && PEID==null)
                    {
                        PEID = readerData.getAttributeValue(null, "value");
                    }
                    else if (readerData.getName().getLocalPart()
                        .equalsIgnoreCase("graphics") && graphicsType==null)
                    {
                        graphicsType = readerData.getAttributeValue(null, "type");
                    }
                }
                
                else if (readerData.isEndElement() && 
                    readerData.getName().getLocalPart().equalsIgnoreCase("node"))
                {
                    //we have reached the end of anchors list                    
                    break;
                }
            } // while
            
            if (nodeId != null)
            {
                if (PEID != null)
                {
                    NodeInfo nodeInfo=new NodeInfo(PEID, "");
                    if(size!=null)nodeInfo.setSize(size);
                    if(position!=null)
                    {
                        if(size!=null)
                        {
                            position.x-=size.width/2;
                            position.y+=size.height/2;
                        }
                        nodeInfo.setPosition(position);
                        minX = Math.min(position.x, minX);
                        maxY = Math.max(position.y, maxY);
                    }
                    
                    if(graphicsType!=null)
                    {
                        nodeInfo.setProperty(GRAPHPROPERTY, graphicsType);
                    }
                    
                    if(nodeConnectors.size()>0)
                    {
                        nodeInfo.setProperty(NODECONNECTORS, nodeConnectors);
                    }
                    
                    dataNodeIdMap.put(nodeId, PEID);
                    
                    presIdNodeInfoMap.put(
                        PEID, nodeInfo);
                }
            }
        }
        
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private IPresentationElement findNode(String PEID)
    {
        if (PEID!=null && !PEID.equals(""))
        {
            for (IPresentationElement pE : presEltList)
            {
                if (PEID.equalsIgnoreCase(pE.getXMIID()))
                {
                    return pE;
                }
            }
        }
        return null;
    }
    
    private void handleEdge()
    {
        String edgeId = null;
        String sourceId = null;
        String targetId = null;
        String PEID = null;
        
        try
        {
            edgeId = readerData.getAttributeValue(null, "id");
            sourceId = readerData.getAttributeValue(null, "source");
            targetId = readerData.getAttributeValue(null, "target");
            //
            EdgeInfo einfo = new EdgeInfo();
            //
            
            while (readerData.hasNext())
            {
                if (readerData.next() == XMLStreamConstants.START_ELEMENT)
                {
                    if (readerData.getName().getLocalPart()
                        .equalsIgnoreCase("PEID"))
                    {
                        PEID = readerData.getAttributeValue(null, "value");
                    }

                    else if (readerData.getName().getLocalPart()
                        .equalsIgnoreCase("edgeLabel"))
                    {
                        
                        handleEdgeLabel(einfo);
                    }
                    else if(readerData.getName().getLocalPart()
                        .equalsIgnoreCase("sourceConnector"))
                    {
                        //edges goes after nodes, so info should be availabel
                        einfo.setProperty("SOURCECONNECTOR", connectors.get(readerData.getAttributeValue(null, "value")));
                    }
                    else if(readerData.getName().getLocalPart()
                        .equalsIgnoreCase("targetConnector"))
                    {
                        //edges goes after nodes, so info should be availabel
                        einfo.setProperty("TARGETCONNECTOR", connectors.get(readerData.getAttributeValue(null, "value")));
                    }
                }
                
                else if (readerData.isEndElement() && 
                    readerData.getName().getLocalPart().equalsIgnoreCase("edge"))
                {
                    //we have reached the end of anchors list                    
                    break;
                }
            } // while
            
            if (edgeId != null)
            {
                if (PEID != null)
                {
                    dataEdgeIdMap.put(edgeId, PEID);
                    einfo.setPEID(PEID);
                    
                    presIdEdgeInfoMap.put(PEID, einfo);

                    dataConnIdMap.put(PEID, new ETPairT(
                        dataNodeIdMap.get(sourceId),
                        dataNodeIdMap.get(targetId)));
                    
                    presIdEdgeInfoMap.put(PEID, einfo);
                }
            }
        }
        
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void handleEdgeLabel(EdgeInfo edgeReader)
    {
        //starts with 'edgeLabel' tag
        //
        //TSLabelPlacementKind="2" - center?
        //TSLabelKind="1" - name for association?  --> typeindo="Name" && xmi.id="Name_EDGE_PEID"?
        //TSLabelKind="12" - name for generalization? --> typeinfo="Name" && xmi.id="Name_EDGE_PEID"?
        //  --> typeinfo="Multiplicity" && xmi.id="Multiplicity_TARGET_PEID" || xmi.id="Multiplicity_SOURCE_PEID"
        //  --> xmi.ixd="End Name_SOURCE_PEID" typeinfo="Name" 
        //
        try
        {
            Point pt = null;
            Dimension d = null;
            
            HashMap<String,Object> labelInfo=new HashMap<String,Object>();
            //peidToLabelMap;
            //
            labelInfo.put("EDGE", edgeReader);
            while (readerData.hasNext())
            {
                if (XMLStreamConstants.START_ELEMENT == readerData.next())
                {
                    //we are only intersted in data of particular start elements
                    if (readerData.getName().getLocalPart().equalsIgnoreCase("constantOffset"))
                    {
                        //about position 1
                        //nodePosition = getPosition("GraphElement.position");
                        //nodeInfo.setPosition(getPosition("GraphElement.position"));
                    }
                    else if(readerData.getName().getLocalPart().equalsIgnoreCase("distanceFromSource"))
                    {
                        //about position 2
                    }
                    else if(readerData.getName().getLocalPart().equalsIgnoreCase("name"))
                    {
                        labelInfo.put("NAME", readerData.getAttributeValue(null, "value"));
                    }
                    else if(readerData.getName().getLocalPart().equalsIgnoreCase("PEID"))
                    {
                        labelInfo.put("PEID", readerData.getAttributeValue(null, "value"));
                    }
                    else if (readerData.getName().getLocalPart().equalsIgnoreCase("size"))
                    {
                        int width=(int) Double.parseDouble(readerData.getAttributeValue(null, "width"));
                        int height=(int) Double.parseDouble(readerData.getAttributeValue(null, "height"));
                        labelInfo.put("SIZE",new Dimension(width,height));
                    }
                    else if (readerData.getName().getLocalPart().equalsIgnoreCase("DiagramElement.property"))
                    {
                    }
                    else if (readerData.getName().getLocalPart().equalsIgnoreCase("SimpleSemanticModelElement"))
                    {
//                        String typeInfo = readerData.getAttributeValue(null, "typeinfo");
//                        if (typeInfo.length() > 0)
//                        {
//
//                            EdgeInfo.EdgeLabel eLabel = edgeReader.new EdgeLabel();
//                            eLabel.setLabel(typeInfo);
//                            eLabel.setPosition(pt);
//                            eLabel.setSize(d);
//                            if (mostRecentEnd == null)
//                            {
//                                edgeReader.getLabels().add(eLabel);
//                            }
//                            else
//                            {
//                                mostRecentEnd.getEndEdgeLabels().add(eLabel);
//                            }
//                        }
                    }
                    else if (readerData.getName().getLocalPart().equalsIgnoreCase("Uml2SemanticModelBridge.element"))
                    {
//                        readerData.nextTag();
//                        //get the  xmi.idref
////                        nodeInfo.setMEID(reader.getAttributeValue(null, "xmi.idref"));
////
////                    }
////                    else if (reader.getName().getLocalPart().equalsIgnoreCase("AssociationEnd"))
////                    {
//                        EdgeInfo.EndDetails assocEnd = edgeReader.new EndDetails();
//                        //get the  xmi.idref
//                        assocEnd.setID(readerData.getAttributeValue(null, "xmi.idref"));
//                        edgeReader.getEnds().add(assocEnd);
//                        mostRecentEnd = assocEnd;
                    }
                    else if (readerData.getName().getLocalPart().equalsIgnoreCase("GraphElement.contained"))
                    {
//                        edgeContainedStack.push(CONTAINED);
                    }
                }
                else if (readerData.isEndElement() && readerData.getName().getLocalPart().equalsIgnoreCase("GraphElement.contained"))
                {
//                    edgeContainedStack.pop();
                }
                else if (readerData.isEndElement() && readerData.getName().getLocalPart().equalsIgnoreCase("edgeLabel"))
                {
                    //EdgeInfo.EdgeLabel eLabel = edgeReader.new EdgeLabel();
                    //edgeReader.getLabels().add(eLabel);
                    peidToLabelMap.put((String) labelInfo.get("PEID"),labelInfo);
                    return;
                }
            }
        }
        catch (XMLStreamException ex)
        {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private class ConnectorData
    {
        private int id;
        private int y;
        private double proportionalX;
        
        public void setId(int id)
        {
            this.id=id;
        }
        public void setId(String id)
        {
            this.id= Integer.parseInt(id);
        }
        public int getId()
        {
            return id;
        }

        public void setY(int y) {
            this.y=y;
        }
        public int getY()
        {
            return y;
        }

        public void setProportionalOffetX(double x) {
            proportionalX=x;
        }
        public double getProportionalOffetX() {
            return proportionalX;
        }
    }
    private IDrawingAreaEventDispatcher getDispatcher()
    {
        IDrawingAreaEventDispatcher retVal = null;
        
        DispatchHelper helper = new DispatchHelper();
        retVal = helper.getDrawingAreaDispatcher();
        
        return retVal;
    }
}
