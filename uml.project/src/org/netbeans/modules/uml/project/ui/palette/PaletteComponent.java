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

package org.netbeans.modules.uml.project.ui.palette;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.netbeans.modules.uml.resources.images.ImageUtil;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;

public class PaletteComponent extends JPanel
{
   private IDrawingAreaControl m_Control = null;

   public PaletteComponent()
   {
      super();
   }

   public PaletteComponent(IDrawingAreaControl control)
   {
      super();
      m_Control = control;
   }

   public void initComponent(IDrawingAreaControl control)
   {
      m_Control = control;

      if (m_Control != null)
         //initialize the toolbars according to the control
         add(new JButton(ImageUtil.instance().getIcon("actor.png"))); // NOI18N
      
      else //assume its a class diagram
         add(new JButton(ImageUtil.instance().getIcon("class.png"))); // NOI18N
   }
}
