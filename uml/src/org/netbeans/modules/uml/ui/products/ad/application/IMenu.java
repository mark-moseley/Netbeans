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


package org.netbeans.modules.uml.ui.products.ad.application;

//import org.netbeans.modules.uml.ui.products.ad.application.action.PluginAction;

/**
 *
 * @author Trey Spiva
 */
public interface IMenu
{
   public final static int BAR       = 0x0;
   public final static int DROP_DOWN = 0x1;
   public final static int MENU      = 0x2;
   public final static int POP_UP    = 0x4;
   public final static int CASCADE   = 0x8;

   /**
    * @param index
    */
   public void insertSeperatorAt(int index);

   /**
    *
    */
   public void appendSeperator();

   /**
    * 
    */
   public IMenu getParentMenu();

   /**
    * @return
    */
   public int getStyle();

   /**
    * @param m_Action
    * @param index
    * @return
    */
//   public IMenuItem createMenuItem(PluginAction m_Action, int index);
   
//   public IMenuItem createMenuItem(String name, int index);

   /**
    * @param m_Action
    * @return
    */
//   public IMenuItem createMenuItem(PluginAction m_Action);
   
//   public IMenuItem createMenuItem(String name);

   /**
    * @return
    */
   public IMenu createSubMenu();

   /**
    * @return
    */
   public boolean isDisposed();

   /**
    * @return
    */
   public IMenuItem[] getItems();

   /**
    * @return
    */
   public int getItemCount();

   /**
    * @param i
    * @return
    */
   public IMenuItem getMenuItem(int i);

   /**
    * @param text
    */
   public void setText(String text);

}
