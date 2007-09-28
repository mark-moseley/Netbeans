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

package org.netbeans.modules.uml.core.requirementsframework;

/**
 * @author brettb
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class Satisfier implements ISatisfier
{
   private String m_strName;
   private String m_strType;
   private String m_strXMIID;
   private String m_strProjectName;
   private String m_strProjectID;

   public String getProjectID()
   {
      return m_strProjectID;
   }

   public void setProjectID(String newVal)
   {
      m_strProjectID = newVal;
   }

   public String getProjectName()
   {
      return m_strProjectName;
   }

   public void setProjectName(String newVal)
   {
      m_strProjectName = newVal;
   }

   public String getType()
   {
      return m_strType;
   }

   public void setType(String newVal)
   {
      m_strType = newVal;
   }

   public String getXMIID()
   {
      return m_strXMIID;
   }

   public void setXMIID(String newVal)
   {
      m_strXMIID = newVal;
   }

   public String getName()
   {
      return m_strName;
   }

   public void setName(String newVal)
   {
      m_strName = newVal;
   }
}
