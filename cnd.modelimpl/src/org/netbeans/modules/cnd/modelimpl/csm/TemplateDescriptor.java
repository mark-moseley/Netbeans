/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm;

import antlr.collections.AST;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;

/**
 *
 * @author eu155513
 */
public final class TemplateDescriptor {
    private final Collection<CsmUID<CsmTemplateParameter>> templateParams;
    private final CharSequence templateSuffix;
    private final int inheritedTemplateParametersNumber;

    public TemplateDescriptor(List<CsmTemplateParameter> templateParams, CharSequence templateSuffix, boolean global) {
        register(templateParams, global);
        this.templateParams = UIDCsmConverter.objectsToUIDs(templateParams);
        this.templateSuffix = templateSuffix;
        inheritedTemplateParametersNumber = 0;
    }

    public TemplateDescriptor(List<CsmTemplateParameter> templateParams, CharSequence templateSuffix, int inheritedTemplateParametersNumber,boolean global) {
        register(templateParams, global);
        this.templateParams = UIDCsmConverter.objectsToUIDs(templateParams);
        this.templateSuffix = templateSuffix;
        this.inheritedTemplateParametersNumber = inheritedTemplateParametersNumber;
    }

    private void register(List<CsmTemplateParameter> templateParams, boolean global){
        for (CsmTemplateParameter par : templateParams){
            if (global) {
                RepositoryUtils.put(par);
            } else {
                Utils.setSelfUID((CsmDeclaration)par);
            }
        }
    }

    public List<CsmTemplateParameter> getTemplateParameters() {
        if (templateParams != null) {
            List<CsmTemplateParameter> res = new ArrayList<CsmTemplateParameter>();
            for(CsmTemplateParameter par : UIDCsmConverter.UIDsToCsmObjects(templateParams)){
                res.add(par);
            }
            return res;
        }
    	return Collections.<CsmTemplateParameter>emptyList();
    }
    
    public CharSequence getTemplateSuffix() {
        return templateSuffix;
    }
    
    public int getInheritedTemplateParametersNumber() {
        return inheritedTemplateParametersNumber;
    }

    public static TemplateDescriptor createIfNeeded(AST ast, CsmFile file, CsmScope scope, boolean global) {
        if (ast == null) {
            return null;
        }
        AST start = TemplateUtils.getTemplateStart(ast.getFirstChild());
        for( AST token = start; token != null; token = token.getNextSibling() ) {
            if (token.getType() == CPPTokenTypes.LITERAL_template) {
                    return new TemplateDescriptor(TemplateUtils.getTemplateParameters(token, file, scope, global),
                            '<' + TemplateUtils.getClassSpecializationSuffix(token, null) + '>', global);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return getTemplateSuffix().toString();
    }

    public TemplateDescriptor(DataInput input) throws IOException {
        this.templateParams = UIDObjectFactory.getDefaultFactory().readUIDCollection(new ArrayList<CsmUID<CsmTemplateParameter>>(), input);
        this.templateSuffix = PersistentUtils.readUTF(input, NameCache.getManager());
        this.inheritedTemplateParametersNumber = input.readInt();
    }

    public void write(DataOutput output) throws IOException {
        UIDObjectFactory.getDefaultFactory().writeUIDCollection(templateParams, output, false);
        PersistentUtils.writeUTF(templateSuffix, output);
        output.writeInt(this.inheritedTemplateParametersNumber);
    }
}
