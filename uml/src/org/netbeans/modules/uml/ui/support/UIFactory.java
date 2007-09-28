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
 * Created on Jun 30, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;

import org.netbeans.modules.uml.ui.controls.filter.IFilterDialog;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel;
import org.netbeans.modules.uml.ui.support.commondialogs.IErrorDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.INavigationDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingErrorDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingNavigationDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;
import org.netbeans.modules.uml.ui.swing.projecttree.JFilterDialog;


/**
 * The UIFactory is used to retrieve the implemenation of common dialogs.  Since
 * we must support both SWT and Swing the UIFactory is used to retrieve the
 * correct implementation for the platform that is running.
 * 
 * @author Trey Spiva
 */
public class UIFactory
{

   /**
    * Retrieves the question dialog implementation.
    * 
    * @return The platforms implementation of the IQuestionDialog interface. 
    */
   public static IQuestionDialog createQuestionDialog()
   {
      return new SwingQuestionDialogImpl();
   }

   /**
    * Retrieves the navigation dialog.
    * 
    * @return The platforms implementation of the INavigationDialog interface.
    */
   public static INavigationDialog createNavigationDialog()
   {
      return new SwingNavigationDialog();
   }   
   
   /**
    * Retrieves the question dialog implementation.
    * 
    * @return The platforms implementation of the IQuestionDialog interface. 
    */
   //public static IPreferenceQuestionDialog createPreferenceQuestionDialog(Component parent)
   public static IPreferenceQuestionDialog createPreferenceQuestionDialog()
   {
      return  new SwingPreferenceQuestionDialog();
   }
   
   /**
    * Retrieves the question dialog implementation.
    * 
    * @return The platforms implementation of the IQuestionDialog interface. 
    */
   //public static IPreferenceQuestionDialog createPreferenceQuestionDialog(Component parent)
   public static IErrorDialog createErrorDialog()
   {
      return  new SwingErrorDialog();
   }
   
   /**
    * Creates a new ProjectTreeFilterDialog.
    *   
    * @param parent The owner window.
    * @return The project trees IFilterDialog implementation.
    */
   public static IFilterDialog createProjectTreeFilterDialog(Component parent,
                                                             IProjectTreeModel model)
   {
      IFilterDialog retVal = null;
      
      Object frame = getParentFrame(parent);
      
      if (frame instanceof Frame)
      {
         Frame parentFrame = (Frame)frame;
         retVal = new JFilterDialog(parentFrame, model);
      }
      else if (frame instanceof Dialog)
      {
         Dialog parentDialog = (Dialog)frame;
         retVal = new JFilterDialog(parentDialog, model);
      }
      
      return  retVal;
   }
   
   public static Object getParentFrame(Component comp)
   {
      Object retVal = null;
      
      if (comp instanceof Frame)
      {
         retVal = comp;
      }
      else if (comp instanceof Dialog)
      {
         retVal = comp;
      }
      else
      {
         retVal = getParentFrame(comp.getParent());
      }
      
      return retVal;
   }
}
