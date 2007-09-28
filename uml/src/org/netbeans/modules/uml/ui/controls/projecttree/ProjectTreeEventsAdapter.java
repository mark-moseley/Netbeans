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
 *
 * Created on Jun 10, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.controls.projecttree;

import org.netbeans.modules.uml.ui.products.ad.projecttreedefaultengine.FilteredItemManager;
import java.awt.datatransfer.Transferable;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;


/**
 *
 * @author Trey Spiva
 */
public class ProjectTreeEventsAdapter implements IProjectTreeEventsSink
{

   /* (non-Javadoc)
    * @see com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEventsSink#onBeforeEdit(com.embarcadero.describe.gui.axcontrols.axprojecttree.IAxProjectTreeControl, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeItem, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEditVerify, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onBeforeEdit(IProjectTreeControl pParentControl,
                            IProjectTreeItem pItem,
                            IProjectTreeEditVerify pVerify,
                            IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEventsSink#onAfterEdit(com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeControl, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeItem, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEditVerify, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onAfterEdit(IProjectTreeControl pParentControl,
                           IProjectTreeItem pItem,
                           IProjectTreeEditVerify pVerify,
                           IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEventsSink#onDoubleClick(com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeControl, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeItem, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onDoubleClick(IProjectTreeControl pParentControl,
                             IProjectTreeItem    pItem, 
                             boolean             isControl, 
                             boolean             isShift, 
                             boolean             isAlt, 
                             boolean             isMeta,
                             IResultCell         cell)
   {
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEventsSink#onSelChanged(com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeControl, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeItems, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onSelChanged(IProjectTreeControl pParentControl,
	                         IProjectTreeItem[] pItem,
                            IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEventsSink#onRightButtonDown(com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeControl, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeItem, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeHandled, int, int, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onRightButtonDown(IProjectTreeControl pParentControl,
                                 IProjectTreeItem pItem,
                                 IProjectTreeHandled pHandled,
                                 int nScreenLocX,
                                 int nScreenLocY,
                                 IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEventsSink#onBeginDrag(com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeControl, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeItems, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeDragVerify, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onBeginDrag(IProjectTreeControl pParentControl,
	                        IProjectTreeItem[] pItem,
                           IProjectTreeDragVerify pVerify,
                           IResultCell cell)
   {
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEventsSink#onMoveDrag(com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeControl, com.embarcadero.describe.gui.axcontrols.axprojecttree.IDataObject, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeDragVerify, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onMoveDrag(IProjectTreeControl    pParentControl,
                          Transferable           pItem,
                          IProjectTreeDragVerify pVerify,
                          IResultCell            cell)
   {
   }

   /* (non-Javadoc)
    * @see com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeEventsSink#onEndDrag(com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeControl, com.embarcadero.describe.gui.axcontrols.axprojecttree.IDataObject, com.embarcadero.describe.gui.axcontrols.axprojecttree.IProjectTreeDragVerify, com.embarcadero.describe.umlsupport.IResultCell)
    */
   public void onEndDrag(IProjectTreeControl    pParentControl,
                         Transferable           pItem,
                         int                    action,
                         IProjectTreeDragVerify pVerify,
                         IResultCell            cell)
   {
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeEventsSink#onItemExpanding(org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl, org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeExpandingContext, org.netbeans.modules.uml.core.support.umlsupport.IResultCell)
    */
   public void onItemExpanding(IProjectTreeControl pParentControl, 
                               IProjectTreeExpandingContext pContext,
                               IResultCell cell)
   {
   }
   
   public void onItemExpandingWithFilter(IProjectTreeControl pParentControl,
            IProjectTreeExpandingContext pContext,
            FilteredItemManager manager, IResultCell cell)
   {
   }

}
