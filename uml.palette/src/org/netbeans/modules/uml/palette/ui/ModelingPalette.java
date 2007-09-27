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
 * ModelingPalette.java
 *
 * Created on March 2, 2005, 3:13 PM
 */

package org.netbeans.modules.uml.palette.ui;

import org.openide.nodes.Node;
import org.openide.nodes.FilterNode;
import org.openide.cookies.InstanceCookie;

/**
 *
 * @author Praveen Savur
 */
public class ModelingPalette extends FilterNode
{
   private static final int DELEGATE = DELEGATE_SET_NAME |
         DELEGATE_GET_NAME |
         DELEGATE_SET_DISPLAY_NAME |
         DELEGATE_GET_DISPLAY_NAME |
         DELEGATE_SET_SHORT_DESCRIPTION |
         DELEGATE_GET_SHORT_DESCRIPTION |
         DELEGATE_DESTROY |
         DELEGATE_GET_ACTIONS |
         DELEGATE_GET_CONTEXT_ACTIONS |
         DELEGATE_SET_VALUE |
         DELEGATE_GET_VALUE;
   
   /** Creates a new instance of ModelingPalette */
   public ModelingPalette(Node root)
   {
      super(root, new PaletteCategoryFilterNode(root));
      enableDelegation(DELEGATE);
   }
   
   //    public SystemAction[] getActions() {
   //        if (staticActions == null)
   //            staticActions = new SystemAction [] {
   //                SystemAction.get(ReorderAction.class)
   //            };
   //            return staticActions;
   //    }
}

class PaletteCategoryFilterNode extends FilterNode.Children
{
   
   public PaletteCategoryFilterNode(Node node)
   {
      super(node);
   }
   
   
   protected Node copyNode(Node node)
   {
      return new PaletteCategoryNode(node);
   }
}

class PaletteCategoryNode extends FilterNode
{
   
   private static final int DELEGATE = DELEGATE_SET_NAME |
         DELEGATE_GET_NAME |
         DELEGATE_SET_DISPLAY_NAME |
         DELEGATE_GET_DISPLAY_NAME |
         DELEGATE_SET_SHORT_DESCRIPTION |
         DELEGATE_GET_SHORT_DESCRIPTION |
         DELEGATE_DESTROY |
         DELEGATE_GET_ACTIONS |
         DELEGATE_GET_CONTEXT_ACTIONS |
         DELEGATE_SET_VALUE |
         DELEGATE_GET_VALUE;
   
   public PaletteCategoryNode(Node node)
   {
      super(node, new PaletteElementFilterNode(node));
      enableDelegation(DELEGATE);
   }
}


class PaletteElementFilterNode extends FilterNode.Children
{
   public PaletteElementFilterNode(Node node)
   {
      super(node);
   }
   
   public Node copyNode(Node node)
   {
      InstanceCookie ic = (InstanceCookie) node.getCookie(InstanceCookie.class);
      if (ic != null)
         return new PaletteElementNode(ic, node);
      
      return node;
   }
}
