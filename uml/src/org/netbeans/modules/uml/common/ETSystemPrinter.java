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


package org.netbeans.modules.uml.common;

import org.netbeans.modules.uml.core.support.Debug;

/**
 * @author KevinM
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ETSystemPrinter implements IETSystemPrinter
{
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#flush()
    */
   public void flush()
   {
      Debug.out.flush();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#close()
    */
   public void close()
   {
	   //Do not close
		//Debug.out.close();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#checkError()
    */
   public boolean checkError()
   {
       return Debug.out.checkError();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#write(int)
    */
   public void write(int b)
   {
		Debug.out.write(b);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#write(byte[], int, int)
    */
   public void write(byte[] buf, int off, int len)
   {
		Debug.out.write(new String(buf), off, len);

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#print(boolean)
    */
   public void print(boolean b)
   {
		Debug.out.print(b);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#print(char)
    */
   public void print(char c)
   {
 		Debug.out.print(c);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#print(int)
    */
   public void print(int i)
   {
		Debug.out.print(i);

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#print(long)
    */
   public void print(long l)
   {
		Debug.out.print(l);

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#print(float)
    */
   public void print(float f)
   {
		Debug.out.print(f);

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#print(double)
    */
   public void print(double d)
   {
		Debug.out.print(d);

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#print(char[])
    */
   public void print(char[] s)
   {
		Debug.out.print(s);

   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#print(java.lang.String)
    */
   public void print(String s)
   {
		Debug.out.print(s);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#print(java.lang.Object)
    */
   public void print(Object obj)
   {
		Debug.out.print(obj);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#println()
    */
   public void println()
   {
		Debug.out.println();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#println(boolean)
    */
   public void println(boolean x)
   {
		Debug.out.println(x);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#println(char)
    */
   public void println(char x)
   {
		Debug.out.println(x);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#println(int)
    */
   public void println(int x)
   {
		Debug.out.println(x);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#println(long)
    */
   public void println(long x)
   {
		Debug.out.println(x);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#println(float)
    */
   public void println(float x)
   {
		Debug.out.println(x);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#println(double)
    */
   public void println(double x)
   {
		Debug.out.println(x);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#println(char[])
    */
   public void println(char[] x)
   {
		Debug.out.println(x);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#println(java.lang.String)
    */
   public void println(String x)
   {
		Debug.out.println(x);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.common.IETSystemPrinter#println(java.lang.Object)
    */
   public void println(Object x)
   {
		Debug.out.println(x);
   }

}
