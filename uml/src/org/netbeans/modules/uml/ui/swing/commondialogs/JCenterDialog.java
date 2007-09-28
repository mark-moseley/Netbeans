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



package org.netbeans.modules.uml.ui.swing.commondialogs;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import org.netbeans.modules.uml.core.support.umlsupport.Log;
import org.openide.windows.WindowManager;

/**
 *
 * @author Trey Spiva
 */
public class JCenterDialog extends JDialog
{

   /**
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog() throws HeadlessException
   {
      super();
      setLocationRelativeTo(null);
   }

   /**
    * @param owner
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Frame owner) throws HeadlessException
   {
      super(owner);
      //setLocationRelativeTo(owner);
      center(owner);
   }

   /**
    * @param owner
    * @param modal
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Frame owner, boolean modal) throws HeadlessException
   {
      super(owner, modal);
      //setLocationRelativeTo(owner);
		center(owner);
   }

   /**
    * @param owner
    * @param title
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Frame owner, String title) throws HeadlessException
   {
      super(owner, title);
      //setLocationRelativeTo(owner);
		center(owner);
   }

   /**
    * @param owner
    * @param title
    * @param modal
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Frame owner, String title, boolean modal) throws HeadlessException
   {
      super(owner, title, modal);
      //setLocationRelativeTo(owner);
		center(owner);
   }

   /**
    * @param owner
    * @param title
    * @param modal
    * @param gc
    */
   public JCenterDialog(Frame owner, String title, boolean modal, GraphicsConfiguration gc)
   {
      super(owner, title, modal, gc);
      //setLocationRelativeTo(owner);
		center(owner);
   }

   /**
    * @param owner
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Dialog owner) throws HeadlessException
   {
      super(owner);
      //setLocationRelativeTo(owner);
		center(owner);
   }

   /**
    * @param owner
    * @param modal
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Dialog owner, boolean modal) throws HeadlessException
   {
      super(owner, modal);
      //setLocationRelativeTo(owner);
		center(owner);
   }

   /**
    * @param owner
    * @param title
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Dialog owner, String title) throws HeadlessException
   {
      super(owner, title);
      //setLocationRelativeTo(owner);
		center(owner);
   }

   /**
    * @param owner
    * @param title
    * @param modal
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Dialog owner, String title, boolean modal) throws HeadlessException
   {
      super(owner, title, modal);
      //setLocationRelativeTo(owner);
		center(owner);
   }

   /**
    * @param owner
    * @param title
    * @param modal
    * @param gc
    * @throws java.awt.HeadlessException
    */
   public JCenterDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) throws HeadlessException
   {
      super(owner, title, modal, gc);
      //setLocationRelativeTo(owner);
		center(owner);
   }
   
   public void center(Frame frame)
   {
//       if (frame != null)
//       {
//           Point p = frame.getLocation();
//           if (p != null)
//           {
//               int centerX = (p.x + frame.getWidth()) / 2;
//               int centerY = (p.y + frame.getHeight()) / 2;
//               int dialogHalfWidth = getWidth() / 2;
//               int dialogHalfHeight = getHeight() / 2;
//               setLocation(centerX - dialogHalfWidth, centerY - dialogHalfHeight);
//           }
//       }
       center((Component)frame);
   }
   
   public void center(Dialog dia)
   {
//       if (dia != null)
//       {
//           Point p = dia.getLocation();
//           if (p != null)
//           {
//               int centerX = (p.x + dia.getWidth()) / 2;
//               int centerY = (p.y + dia.getHeight()) / 2;
//               int dialogHalfWidth = getWidth() / 2;
//               int dialogHalfHeight = getHeight() / 2;
//               setLocation(centerX - dialogHalfWidth, centerY - dialogHalfHeight);
//           }
//       }
       center((Component)dia);
   }
        
	public void center(Component comp)
        {
            if(comp == null)
            {
                comp = WindowManager.getDefault().getMainWindow();
            }
            
            if (comp != null)
            {
                Point p = comp.getLocation();
                if (p != null)
                {
                    // This does not seem to work well on two monitor systems
                    // The reason is that the location is in the neg numbers.
                    int centerX = p.x + (comp.getWidth() / 2);
                    int centerY = p.y + (comp.getHeight() / 2);
                    int dialogHalfWidth = getWidth() / 2;
                    int dialogHalfHeight = getHeight() / 2;
                    setLocation(centerX - dialogHalfWidth, centerY - dialogHalfHeight);
                }
            }
        }
   
	protected JRootPane createRootPane() {
	  ActionListener actionListener = new ActionListener() {
		 public void actionPerformed(ActionEvent actionEvent) {
          try
          {
             setVisible(false);
          }
          catch(Exception e)
          {
             Log.stackTrace(e);
          }
		 }
	  };
	  JRootPane rootPane = new JRootPane();
	  KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	  rootPane.registerKeyboardAction(actionListener, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
	  return rootPane;
	}

}
