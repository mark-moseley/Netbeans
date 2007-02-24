/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * DiagramStructureNavigator.java
 *
 * Created on December 8, 2005, 2:53 PM
 */

package org.netbeans.modules.uml.drawingarea.navigator;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import javax.swing.JPanel;
import javax.swing.tree.TreeModel;

/**
 *
 * @author  TreySpiva
 */
public class DiagramStructureNavigator extends JPanel
{
   private IDrawingAreaControl drawingArea = null;
   private TreeModel model = null;
    
   /** Creates new form DiagramStructureNavigator */
   public DiagramStructureNavigator(IDrawingAreaControl drawingArea)
   {
      initComponents();
      setDrawingArea(drawingArea);
      navigatorTree.setCellRenderer(new DiagramStrucutureRender());
   }

   public IDrawingAreaControl getDrawingArea()
   {
      return drawingArea;
   }

   public void setDrawingArea(IDrawingAreaControl drawingArea)
   {
      this.drawingArea = drawingArea;
   }
   
   public void refresh()
   {
      // First get all of the model elements that are on the diagram.
      ETList < IElement >  elements = getDrawingArea().getAllItems3();
      model = new NavigatorTreeModel(elements);
      navigatorTree.setModel(model);
      
   }
   
   /** This method is called from within the constructor to
    * initialize the form.
    * WARNING: Do NOT modify this code. The content of this method is
    * always regenerated by the Form Editor.
    */
   // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
   private void initComponents()
   {
      navigatorScrollBar = new javax.swing.JScrollPane();
      navigatorTree = new javax.swing.JTree();

      navigatorScrollBar.setBorder(null);
      navigatorTree.setRootVisible(false);
      navigatorScrollBar.setViewportView(navigatorTree);

      org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
      this.setLayout(layout);
      layout.setHorizontalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(navigatorScrollBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
      );
      layout.setVerticalGroup(
         layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
         .add(navigatorScrollBar, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
      );
   }// </editor-fold>//GEN-END:initComponents
   
   // Variables declaration - do not modify//GEN-BEGIN:variables
   public javax.swing.JScrollPane navigatorScrollBar;
   public javax.swing.JTree navigatorTree;
   // End of variables declaration//GEN-END:variables
   
}
