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

/*
 * ComplexTypeDerivationsView.java
 *
 * Created on October 25, 2005, 2:09 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.refactoring.query.views;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;
import org.netbeans.modules.xml.nbprefuse.AnalysisConstants;
import org.netbeans.modules.xml.nbprefuse.AnalysisViewer;
import org.netbeans.modules.xml.nbprefuse.EdgeFillColorAction;
import org.netbeans.modules.xml.nbprefuse.EdgeStrokeColorAction;
import org.netbeans.modules.xml.nbprefuse.NodeExpansionMouseControl;
import org.netbeans.modules.xml.nbprefuse.NodeFillColorAction;
import org.netbeans.modules.xml.nbprefuse.NodeStrokeColorAction;
import org.netbeans.modules.xml.nbprefuse.NodeTextColorAction;
import org.netbeans.modules.xml.nbprefuse.PopupMouseControl;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.nbprefuse.View;
import org.netbeans.modules.xml.nbprefuse.layout.NbFruchtermanReingoldLayout;
import org.netbeans.modules.xml.nbprefuse.render.CompositionEdgeRenderer;
import org.netbeans.modules.xml.nbprefuse.render.FindUsagesRendererFactory;
import org.netbeans.modules.xml.nbprefuse.render.GeneralizationEdgeRenderer;
import org.netbeans.modules.xml.nbprefuse.render.NbLabelRenderer;
import org.netbeans.modules.xml.nbprefuse.render.ReferenceEdgeRenderer;
import org.netbeans.modules.xml.schema.refactoring.query.readers.ComplexTypeDerivationsReader;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.animate.ColorAnimator;
import prefuse.action.animate.QualityControlAnimator;
import prefuse.action.animate.VisibilityAnimator;
import prefuse.activity.SlowInSlowOutPacer;
import prefuse.controls.FocusControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.SubtreeDragControl;
import prefuse.controls.ToolTipControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.render.EdgeRenderer;

/**
 *
 * @author Jeri Lockhart
 */
public class ComplexTypeDerivationsView implements View, PropertyChangeListener {
    
    private GlobalComplexType baseCT;
    private Display display = null;
    private JPanel displayPanel;
    private Graph graph;
    private boolean usePacer = true;    // slow in slow out pacer for initial graph animation
    private int resizeCounter = 0;
    
    /**
     * Creates a new instance of ComplexTypeDerivationsView
     *  using a previously created Preview from
     * FindUsageVisitor
     */
    public ComplexTypeDerivationsView(
            SchemaComponent baseCT
            ) {
        this.baseCT = (GlobalComplexType)baseCT;
    }
    
    /**
     *  Implement View
     *
     */
    
//    public JPanel getDisplayPanel() {
//        return displayPanel;
//    }
    
    
    public Object[] createModels( ) {    // not cancellable
        resizeCounter = 0;
        usePacer = true;
        graph = null;
        ComplexTypeDerivationsReader reader = new ComplexTypeDerivationsReader();    
        // Load the prefuse graph
        
        graph = reader.loadComplexTypeDerivationsGraph(baseCT);
        return new Object[] {graph};
    }
     
//    public DefaultTreeModel getTreeModel() {
//        assert true:"This view only supports graph output.  Use createGraph() instead.";
//        throw new UnsupportedOperationException("This view only supports graph output.  Use createGraph() instead.");//NOI18N
//        
//    }
//     
//    public Graph createGraphAndTreeModel(CancelSignal interruptProcess) {
//        assert true:"This view only supports graph output.  Use createGraph() instead.";    //NOI18N
//        throw new UnsupportedOperationException("This view only supports graph output.  Use createGraph() instead.");//NOI18N
//    }
    
    public boolean showView(AnalysisViewer viewer) {
        boolean wasShown = false;
        viewer.setCurrentView(this);
        // resizing will call showView() from AnalysisViewer
        // prevent showing the view twice the first time
        if (resizeCounter == 0){
            resizeCounter++;
        } else if (resizeCounter == 1 || resizeCounter == 2){
            resizeCounter++;
            return wasShown;
        }
        
        Visualization viz = null;
        if (graph == null){
            ErrorManager.getDefault().log(ErrorManager.ERROR,
                    NbBundle.getMessage(WhereUsedView.class,
                    "LBL_Graph_Not_Created_Error"));
            return false;
        }
        
        try {
            // initialize display
            this.display = new Display();
            display.setBackground(Color.WHITE);
            viz = new Visualization();
            viz.addGraph(AnalysisConstants.GRAPH_GROUP, graph);
            display.setVisualization(viz);
            
            // size the AnalysisViewer to the available space in the main parent panel
            Dimension dim = viewer.getPanel().getBounds().getSize();
//            System.out.println("AnalysisView dimensions:" + dim.toString());
            Dimension displayDim = display.getBounds().getSize();
            if (!dim.equals(displayDim)) {
                display.setSize(dim.width, dim.height);
            }
            
            this.displayPanel = new JPanel(new BorderLayout());
            displayPanel.add(display, BorderLayout.CENTER);
            viewer.addDisplayPanel(displayPanel);
            
            display.addControlListener(new SubtreeDragControl());
            display.addControlListener(new PanControl());
            display.addControlListener(new ZoomControl());
            display.addControlListener(new WheelZoomControl());
            display.addControlListener(new ToolTipControl(AnalysisConstants.TOOLTIP)); // "tooltip"
            display.addControlListener(new FocusControl());
            display.addControlListener(new ActivatedNodesControlAdapter());
            display.addControlListener(new NeighborHighlightControl(AnalysisConstants.ACTION_UPDATE)); //NOI18N
            display.addControlListener(new PopupMouseControl());
            display.addControlListener(new NodeExpansionMouseControl(viz,
                    AnalysisConstants.ACTION_UPDATE));
            
            
            // initialize renderers
            
            viz.setRendererFactory(new FindUsagesRendererFactory(
                    new NbLabelRenderer(),
                    new NbLabelRenderer(),
                    new GeneralizationEdgeRenderer(),
                    new CompositionEdgeRenderer(),
                    new ReferenceEdgeRenderer(),
                    new EdgeRenderer()
                    ));
            // initialize action lists
//            ActionList filter = new ActionList();
//            System.out.println("layout.getLayoutBounds() " + layout.getLayoutBounds());   // null
//            Rectangle2D rect = layout.getLayoutBounds(viz);
//            layout.setLayoutBounds(new Rectangle(rect.getBounds().width-10, rect.getBounds().height-10));
            ActionList update = new ActionList(viz);
            update.add(new NodeFillColorAction());
            update.add(new NodeTextColorAction());
            update.add(new NodeStrokeColorAction());
            update.add(new EdgeStrokeColorAction());
            update.add(new EdgeFillColorAction());
            update.add(new RepaintAction());
            viz.putAction(AnalysisConstants.ACTION_UPDATE, update);
            
            ActionList draw = new ActionList();
            draw.add(new NodeFillColorAction());
            draw.add(new NodeTextColorAction());
            draw.add(new NodeStrokeColorAction());
            draw.add(new EdgeStrokeColorAction());
            draw.add(new EdgeFillColorAction());
            viz.putAction(AnalysisConstants.ACTION_DRAW, draw);
            
            ActionList layout = new ActionList();
            layout.add(new NbFruchtermanReingoldLayout(AnalysisConstants.GRAPH_GROUP));
            layout.add(new RepaintAction());
            viz.putAction(AnalysisConstants.ACTION_LAYOUT, layout);
            
            
            viz.runAfter(AnalysisConstants.ACTION_DRAW, AnalysisConstants.ACTION_LAYOUT);
            
            if (usePacer) {
                // animated transition
                ActionList animate = new ActionList(1500, 20);
                animate.setPacingFunction(new SlowInSlowOutPacer());
                animate.add(new QualityControlAnimator());
                animate.add(new VisibilityAnimator());
//                animate.add(new PolarLocationAnimator(AnalysisConstants.GRAPH_GROUP));
                animate.add(new ColorAnimator());
                animate.add(new RepaintAction());
                viz.putAction(AnalysisConstants.ACTION_ANIMATE, animate);
                viz.alwaysRunAfter(AnalysisConstants.ACTION_LAYOUT, AnalysisConstants.ACTION_ANIMATE);
            }
            
            viz.run(AnalysisConstants.ACTION_DRAW);
            wasShown = true;
            
            
        } catch ( Exception e ) {
            ErrorManager.getDefault().notify(e);
        }
        return wasShown;
    }
    
    
    public void usePacer(boolean use) {
        this.usePacer = use;
    }
    

    /**
     *  Should the SchemaColumnView make the Column
     *  that the View is shown in as wide as possible?
     *  @return boolean true if View should be shown
     *    in a column as wide as the available horizontal space
     *    in the column view
     */
    public boolean getMaximizeWidth(){
        return true;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
    }
}
