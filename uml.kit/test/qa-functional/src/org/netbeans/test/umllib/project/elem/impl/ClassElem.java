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
 * ClassElem.java
 *
 * Created on January 23, 2007, 2:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.umllib.project.elem.impl;

import java.util.LinkedList;
import java.util.List;
import org.netbeans.test.umllib.project.elem.ElemType;
import org.netbeans.test.umllib.project.elem.IAttributeElem;
import org.netbeans.test.umllib.project.elem.IClassElem;
import org.netbeans.test.umllib.project.elem.IInterfaceElem;
import org.netbeans.test.umllib.project.elem.IJavaElem;
import org.netbeans.test.umllib.project.elem.IOperationElem;
import org.netbeans.test.umllib.project.elem.IPackageElem;


/**
 *
 * @author andromeda
 */

public class ClassElem extends JavaElem implements IClassElem {
    
    IClassElem superClass;
    

    public ClassElem(String name, IPackageElem pack) {
	this(name, pack, PredefinedType.INSTANCE.getClassElem(PredefinedType.JAVA_LANG_OBJECT));
    }
    
    public ClassElem(String name, IPackageElem pack, IClassElem superClass) {
	super(name, pack);
	this.superClass = superClass;
    }

    
    public ElemType getType() {
	return ElemType.CLASS;
    }

    public IClassElem getSuperClass() {
	return superClass;
    }
    
    public void setSuperClass(IClassElem classElem) {
	this.superClass = classElem;
    }
    
    
    public String toString() {
	String str = "";
	
	//str += getPackage() + "\n";
	str += "class " + getFullName() + " "; 
	
	if(getSuperClass() != null){
	    str += "extends " + getSuperClass().getFullName() + " ";
	}
	
	if(getSuperInterfaceList().size() != 0 ){
	    str += "implements ";
	    
	    for(IInterfaceElem interfaceElem: getSuperInterfaceList()){
		str += interfaceElem.getFullName() + " ";
	    }
	}
	
	str += "{\n";

	for(IAttributeElem operationElem: getAttributeList()){
	    str += operationElem.toString() + ";\n";
	}
	
	
	for(IOperationElem operationElem: getOperationList()){
	    str += operationElem.toString() + "{ /* operation body */ }\n";
	}
	
	
	for(IJavaElem elem: getNestedElemList()){
	    str += elem.toString() + "\n";
	}
	
	str += "}";

	return str;
    }

    
    
}
