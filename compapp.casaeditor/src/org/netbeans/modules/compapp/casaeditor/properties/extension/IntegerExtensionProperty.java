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
package org.netbeans.modules.compapp.casaeditor.properties.extension;

import java.beans.PropertyEditor;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaComponent;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaExtensibilityElement;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;
import org.netbeans.modules.compapp.casaeditor.properties.spi.ExtensionProperty;
import org.netbeans.modules.compapp.casaeditor.properties.IntegerEditor;

/**
 * Extension poperty of Integer type (empty value allowed).
 *
 * @author jqian
 */
public class IntegerExtensionProperty extends ExtensionProperty<Integer> {

    public IntegerExtensionProperty(
            CasaNode node,
            CasaComponent extensionPointComponent,
            CasaExtensibilityElement firstEE,
            CasaExtensibilityElement lastEE,
            String propertyType,
            String propertyName,
            String displayName,
            String description) {
        super(node, extensionPointComponent, firstEE, lastEE, propertyType,
                Integer.class, propertyName, displayName, description);
    }

    static class Regular extends IntegerExtensionProperty {

        public Regular(
                CasaNode node,
                CasaComponent extensionPointComponent,
                CasaExtensibilityElement firstEE,
                CasaExtensibilityElement lastEE,
                String propertyType,
                String propertyName,
                String displayName,
                String description) {
            super(node, extensionPointComponent, firstEE, lastEE, propertyType,
                    propertyName, displayName, description);
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new IntegerEditor();
        }
    }
    
    static class Positive extends IntegerExtensionProperty {

        public Positive(
                CasaNode node,
                CasaComponent extensionPointComponent,
                CasaExtensibilityElement firstEE,
                CasaExtensibilityElement lastEE,
                String propertyType,
                String propertyName,
                String displayName,
                String description) {
            super(node, extensionPointComponent, firstEE, lastEE, propertyType,
                    propertyName, displayName, description);
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new IntegerEditor(1, Integer.MAX_VALUE);
        }
    }

    static class Negative extends IntegerExtensionProperty {

        public Negative(
                CasaNode node,
                CasaComponent extensionPointComponent,
                CasaExtensibilityElement firstEE,
                CasaExtensibilityElement lastEE,
                String propertyType,
                String propertyName,
                String displayName,
                String description) {
            super(node, extensionPointComponent, firstEE, lastEE, propertyType,
                    propertyName, displayName, description);
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new IntegerEditor(Integer.MIN_VALUE, -1);
        }
    }

    static class NonPositive extends IntegerExtensionProperty {

        public NonPositive(
                CasaNode node,
                CasaComponent extensionPointComponent,
                CasaExtensibilityElement firstEE,
                CasaExtensibilityElement lastEE,
                String propertyType,
                String propertyName,
                String displayName,
                String description) {
            super(node, extensionPointComponent, firstEE, lastEE, propertyType,
                    propertyName, displayName, description);
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new IntegerEditor(Integer.MIN_VALUE, 0);
        }
    }

    static class NonNegative extends IntegerExtensionProperty {

        public NonNegative(
                CasaNode node,
                CasaComponent extensionPointComponent,
                CasaExtensibilityElement firstEE,
                CasaExtensibilityElement lastEE,
                String propertyType,
                String propertyName,
                String displayName,
                String description) {
            super(node, extensionPointComponent, firstEE, lastEE, propertyType,
                    propertyName, displayName, description);
        }

        @Override
        public PropertyEditor getPropertyEditor() {
            return new IntegerEditor(0, Integer.MAX_VALUE);
        }
    }
}


