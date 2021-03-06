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

package org.netbeans.modules.xml.schema.ui.nodes.categorized.customizer;

import java.io.IOException;
import java.util.Map;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceCustomizer;
import org.netbeans.modules.xml.xam.ui.customizer.ExternalReferenceDecorator;
import org.openide.util.HelpCtx;

/**
 * A redefine customizer.
 *
 * @author  Administrator
 * @author  Nathan Fiedler
 */
public class RedefineCustomizer extends ExternalReferenceCustomizer<Redefine> {
    private static final long serialVersionUID = 1L;
    private ExternalReferenceDecorator decorator;

    /**
     * Creates a new instance of RedefineCustomizer.
     *
     * @param  redefine  the component to customize.
     */
    public RedefineCustomizer(Redefine redefine) {
        super(redefine, null);
    }

    @Override
    public void applyChanges() throws IOException {
        super.applyChanges();
        if (isLocationChanged()) {
            Redefine redefine = getModelComponent();
            redefine.setSchemaLocation(getEditedLocation());
        }
    }

    public boolean mustNamespaceDiffer() {
        return false;
    }

    protected String getReferenceLocation() {
        Redefine redefine = getModelComponent();
        return redefine.getSchemaLocation();
    }

    protected String getNamespace() {
        return null;
    }

    protected String getPrefix() {
        return null;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(RedefineCustomizer.class);
    }

    protected String getTargetNamespace(Model model) {
        if (model instanceof SchemaModel) {
            SchemaModel sm = (SchemaModel) model;
            Schema schema = sm.getSchema();
            if (schema != null) {
                return schema.getTargetNamespace();
            }
        }
        return null;
    }

    protected Map<String, String> getPrefixes(Model model) {
        if (model instanceof SchemaModel) {
            SchemaModel sm = (SchemaModel) model;
            Schema schema = sm.getSchema();
            if (schema != null) {
                return schema.getPrefixes();
            }
        }
        return null;
    }

    protected ExternalReferenceDecorator getNodeDecorator() {
        if (decorator == null) {
            decorator = new SchemaReferenceDecorator(this);
        }
        return decorator;
    }

    protected String generatePrefix() {
        return "";
    }
}
