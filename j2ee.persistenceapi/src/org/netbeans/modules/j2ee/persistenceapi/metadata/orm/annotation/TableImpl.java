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

package org.netbeans.modules.j2ee.persistenceapi.metadata.orm.annotation;

import javax.lang.model.element.AnnotationMirror;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.*;

public class TableImpl implements Table {

    private final ParseResult parseResult;

    public TableImpl(AnnotationModelHelper helper, AnnotationMirror annotation, String defaultName) {
        AnnotationParser parser = AnnotationParser.create(helper);
        parser.expectString("name", parser.defaultValue(defaultName)); // NOI18N
        parser.expectString("schema", parser.defaultValue("")); // NOI18N
        parser.expectString("catalog", parser.defaultValue("")); // NOI18N
        parseResult = parser.parse(annotation);
    }

    public void setName(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getName() {
        return parseResult.get("name", String.class); // NOI18N
    }

    public void setCatalog(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getCatalog() {
        return parseResult.get("catalog", String.class); // NOI18N
    }

    public void setSchema(String value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public String getSchema() {
        return parseResult.get("schema", String.class); // NOI18N
    }

    public void setUniqueConstraint(int index, UniqueConstraint value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public UniqueConstraint getUniqueConstraint(int index) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int sizeUniqueConstraint() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public void setUniqueConstraint(UniqueConstraint[] value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public UniqueConstraint[] getUniqueConstraint() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int addUniqueConstraint(UniqueConstraint value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public int removeUniqueConstraint(UniqueConstraint value) {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }

    public UniqueConstraint newUniqueConstraint() {
        throw new UnsupportedOperationException("This operation is not implemented yet."); // NOI18N
    }
}
