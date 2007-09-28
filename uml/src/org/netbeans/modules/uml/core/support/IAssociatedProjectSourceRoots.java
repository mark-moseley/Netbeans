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
 * ISource.java
 *
 * Created on March 29, 2005, 11:24 AM
 */

package org.netbeans.modules.uml.core.support;

import java.io.File;


/**
 * NetBeans projects are able to have multiple source roots.  The
 * IAssociatedProjectSourceRoots aides in converting a file path that is
 * relative to one of the NetBean project source roots to an absolute
 * file path.  IAssociatedProjectSourceRoots can also convert an absolute
 * file path into a path relative to one of the source roots.
 *
 * Each project source root has a identifier.  To convert an absolute path
 * to a relative path a source root identifier in placed in the relative path.
 *
 * @author Trey Spiva
 */
public interface IAssociatedProjectSourceRoots
{  
   /**
    * Retrieves the source root identifier that matches the specified
    * file name.
    *
    * @param file The file used to retrieve the source root identifier.
    * @return  The source root identifier if one can be mapped to the file name
    *          otherwise an empty string.
    */
   public String getSourceRootId(String file);
   
   /**
    * Creates a path that is relative to one of the projects source roots.  If 
    * the file name is not descendent of one of the source roots an empty string
    * will be returned.
    *
    * @param file The name of the file to convert.
    * @return The relative path unless the file name is not a descendent of one 
    *         of the source roots.
    */
   public String createRelativePath(String file);
   
   /**
    * Creates an absolute file path name.  The file name is only converted if the 
    * file name start with a source root identifier.  If the source file name does not
    * start with a source root identifier then an empty string
    * will be returned.
    *
    * @param file The name of the file to convert.
    * @return The converted path unless the file name does not start with a source
    *         root identifier
    */
   public String createAbsolutePath(String file);
   
   /**
    * Retrieves the source roots that are need to locate all source files that
    * are needed to compile the project.
    */
   public File[] getCompileDependencies();
}
