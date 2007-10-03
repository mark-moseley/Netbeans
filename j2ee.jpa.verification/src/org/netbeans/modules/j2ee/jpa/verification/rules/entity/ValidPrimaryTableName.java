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

package org.netbeans.modules.j2ee.jpa.verification.rules.entity;

import java.util.Collections;
import javax.lang.model.element.TypeElement;
import org.netbeans.modules.db.api.sql.SQLKeywords;
import org.netbeans.modules.j2ee.jpa.model.JPAHelper;
import org.netbeans.modules.j2ee.jpa.verification.JPAClassRule;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.j2ee.persistence.dd.JavaPersistenceQLKeywords;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbBundle;

/**
 * Table mapped to the entity class name must not be a reserved JPA-QL keyword
 * 
 * @author Tomasz.Slota@Sun.COM
 */
public class ValidPrimaryTableName extends JPAClassRule {
    
    /** Creates a new instance of LegalName */
    public ValidPrimaryTableName() {
        setClassContraints(Collections.singleton(ClassConstraints.ENTITY));
    }
    
    @Override public ErrorDescription[] apply(TypeElement subject, ProblemContext ctx){
        String entityName = JPAHelper.getPrimaryTableName((Entity)ctx.getModelElement());
        
        if (entityName.length() == 0){
            return new ErrorDescription[]{createProblem(subject, ctx,
                    NbBundle.getMessage(IdDefinedInHierarchy.class, "MSG_InvalidPersistenceQLIdentifier"))};
        }
        
        if (JavaPersistenceQLKeywords.isKeyword(entityName)){
            return new ErrorDescription[]{createProblem(subject, ctx,
                    NbBundle.getMessage(IdDefinedInHierarchy.class, "MSG_ClassNamedWithJavaPersistenceQLKeyword"))};
        }
        
        if (SQLKeywords.isSQL99ReservedKeyword(entityName)){
            return new ErrorDescription[]{createProblem(subject, ctx,
                    NbBundle.getMessage(IdDefinedInHierarchy.class, "MSG_ClassNamedWithReservedSQLKeyword"),
                    Severity.WARNING)};
        }
        
        return null;
    }
}
