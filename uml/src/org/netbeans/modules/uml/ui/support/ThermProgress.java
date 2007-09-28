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



package org.netbeans.modules.uml.ui.support;

import java.awt.Component;

import javax.swing.ProgressMonitor;

import org.netbeans.modules.uml.ui.support.applicationmanager.IProgressCtrl;

/**
 * @author sumitabhk
 *
 *
 */
public class ThermProgress
{
	/// The progress ctrl
	private IProgressCtrl m_ProgressCtrl = null;

	/// The current message
	private String m_Message = "";

   private Component m_parent = null;
   private ProgressMonitor m_progressMonitor = null;

   /**
    *
    */
   public ThermProgress()
   {
      this( ProductHelper.getWindowHandle() );
   }
   
   /**
    * 
    */
   public ThermProgress( Component parent )
   {
      super();
      m_parent = parent;
   }

	/// Begins the progress control
   public void beginProgress( String message, int nLower, int nUpper, int nInitialPos )
   {
      // Fix J2739:  Disable the progress monitor, since it is not working as desired
      // UPDATE m_progressMonitor = new ProgressMonitor( m_parent, message, "", nLower, nUpper );
      setPos( nInitialPos );
   }
   
	/// Sets the current position - leaves the text the same. 
   public void setPos( int nPos )
   {
      if( m_progressMonitor != null )
      {
         m_progressMonitor.setProgress( nPos );
      }
   }

	/// Sets the current position and text 
   public void setPos( String message, int nPos )
   {
      if( m_progressMonitor != null )
      {
         m_progressMonitor.setNote( message );
         m_progressMonitor.setProgress( nPos );
      }
   }

   /// initializes and updates the progress control
   public void updateProgressControl( IProgressCtrl hInstance, int nID, int nCurPos, int nTotalPos )
   {
      // TODO
   }

   /// initializes and updates the progress control
   public void updateProgressControl( String sMessage, int nCurPos, int nTotalPos )
   {
      if( m_progressMonitor != null )
      {
         m_progressMonitor.setNote( sMessage );
         m_progressMonitor.setProgress( nCurPos );
         m_progressMonitor.setMaximum( nTotalPos );
      }
   }

   /// Ends the progress
   public void endProgress()
   {
      if( m_progressMonitor != null )
      {
         m_progressMonitor.close();
      }
   }
}


