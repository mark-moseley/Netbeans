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

package org.netbeans.modules.xml.xam.ui.customizer;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.xml.xam.Model;

/**
 * An abstract implementation of ExternalReferenceDecorator that provides
 * some common functionality for all concrete implementations to share.
 *
 * @author Nathan Fiedler
 */
public abstract class AbstractReferenceDecorator implements
        ExternalReferenceDecorator {
    /** Set of namespace prefixes, keyed by Model instances. */
    private Map<Model, String> prefixMap;

    /**
     * Creates a new instance of AbstractReferenceDecorator.
     */
    public AbstractReferenceDecorator() {
        prefixMap = new HashMap<Model, String>();
    }

    /**
     * Generate a unique namespace prefix. The model is provided as a
     * means of possibly creating a prefix that reflects the model in
     * some fashion (e.g. using its namespace).
     *
     * @param  model  XAM model, which may be used to generate the prefix.
     * @return  unique prefix value (e.g. "ns1"); must not be null.
     */
    protected abstract String generatePrefix(Model model);

    public String generatePrefix(ExternalReferenceNode node) {
        // It only makes sense to generate a prefix for nodes that have a
        // model, otherwise folders and non-XML files would have them.
        if (node.hasModel()) {
            // Use the model as the key, rather than the node itself, since
            // there could be multiple nodes representing a single model.
            Model model = node.getModel();
            String prefix = prefixMap.get(model);
            if (prefix == null) {
                prefix = generatePrefix(model);
                prefixMap.put(model, prefix);
            }
            return prefix;
        }
        return "";
    }
}
