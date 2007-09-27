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

package org.netbeans.modules.cnd.modelimpl.csm;

import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;

/**
 * Implements 
 * @author Vladimir Kvashin
 */
public class ClassImplSpecialization extends ClassImpl implements CsmTemplate {
    
    private String qualifiedNameSuffix = "";
    
    private ClassImplSpecialization(AST ast, CsmFile file) { 
	super(ast, file);
    }
    
    protected void init(CsmScope scope, AST ast) {
	AST qIdToken = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
	assert qIdToken != null;
	qualifiedNameSuffix = TemplateUtils.getSpecializationSuffix(qIdToken);
	super.init(scope, ast);
	// super.register(); // super.init() has already registered me
    }
    
    public static ClassImplSpecialization create(AST ast, CsmScope scope, CsmFile file) {
	ClassImplSpecialization impl = new ClassImplSpecialization(ast, file);
	impl.init(scope, ast);
	return impl;
    }
    
    public boolean isTemplate() {
	return true;
    }
    
    
    public boolean isSpecialization() {
	return true;
    }

//    public String getTemplateSignature() {
//	return qualifiedNameSuffix;
//    }

    public List<CsmTemplateParameter> getTemplateParameters() {
	return Collections.EMPTY_LIST;
    }

  
// This does not work since the method is called from base class' constructor    
//    protected String getQualifiedNamePostfix() {
//	String qName = super.getQualifiedNamePostfix();
//	if( isSpecialization() ) {
//	    qName += qualifiedNameSuffix;
//	}
//	return qName;
//    }

    protected String getQualifiedNamePostfix() {
	return super.getQualifiedNamePostfix() + qualifiedNameSuffix;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    public void write(DataOutput output) throws IOException {
        super.write(output);
	output.writeUTF(qualifiedNameSuffix);
    }
    
    public ClassImplSpecialization(DataInput input) throws IOException {
	super(input);
	qualifiedNameSuffix = input.readUTF();
    }
    
    public String getDisplayName() {
	return getName() + qualifiedNameSuffix;
    }
    
}
