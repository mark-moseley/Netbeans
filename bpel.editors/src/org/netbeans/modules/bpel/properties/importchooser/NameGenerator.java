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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bpel.properties.importchooser;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.ExNamespaceContext;

/**
 * Utility class to generate uniqiue names for BpelModel
 * @author Vitaly Bychkov
 */
public class NameGenerator {

    // Default Prefix for the namespace 
    private static final String PREFIX_PREFIX = "ns"; // NOI18N

    private static NameGenerator INSTANCE = new NameGenerator();
    
    private NameGenerator() {
    }
    
    public static NameGenerator getInstance() {
        return INSTANCE;
    }
    
    /**
     * Generate a unique namespace prefix for the given model. This is
     * the same as generateNamespacePrefix(String, BpelModel, int) with
     * a value of zero for the counter parameter.
     *
     * @param  prefix  the desired prefix for the namespace prefix;
     *                 if null, a default of "ns" will be used.
     * @param  model   model in which to find unique prefix.
     * @return  the unique namespace prefix (e.g. "ns0").
     */
    public String generateNamespacePrefix(String prefix,
            BpelModel model) {
        // WSDL uses zero for the namespace prefix, so let's do the same.
        return generateNamespacePrefix(prefix, model, 0);
    }

    /**
     * Generate a unique namespace prefix for the given model.
     *
     * @param  prefix   the desired prefix for the namespace prefix;
     *                  if null, a default of "ns" will be used.
     * @param  model    model in which to find unique prefix.
     * @param  counter  minimum number to use as suffix (results in a
     *                  prefix such as "ns" plus the value of counter).
     * @return  the unique namespace prefix (e.g. "ns0").
     */
    public String generateNamespacePrefix(String prefix,
            BpelModel model, int counter) {
        String prefixStr = prefix == null ? PREFIX_PREFIX : prefix;
        String generated = prefixStr + counter;
        while (isPrefixExist(generated, model)) {
            counter++;
            generated = prefixStr + counter;
        }
        return generated;
    }

    /**
     * Determine if the given namespace prefix is used in the model.
     *
     * @param  prefix  namespace prefix to look up.
     * @param  model   the model in which to look.
     * @return  true if exists, false otherwise.
     */
    public static boolean isPrefixExist(String prefix, BpelModel model) {
        return getNamespaceURI(prefix, model) != null ? true : false;
    }

    /**
     * Get the prefix for the given namespace, for this given element.
     *
     * @param  namespace  the namespace to lookup.
     * @param  model   the model in which to look.
     * @return  the prefix, or null if none.
     */
    public static String getNamespacePrefix(String namespace,
            BpelModel model) 
    {
        if (model != null && namespace != null) {
            return getNamespacePrefix(namespace, model.getProcess());
        }
        return null;
    }

    /**
     * Get the prefix for the given namespace, for this given element.
     *
     * @param  namespace  the namespace to lookup.
     * @param  element    the element to look at.
     * @return  the prefix, or null if none.
     */
    public static String getNamespacePrefix(String namespace,
            BpelEntity entity) {
        if (entity != null && namespace != null) {
            ExNamespaceContext nsContext = entity.getNamespaceContext();
            if (nsContext != null) {
                String oldPrefix = nsContext.getPrefix(namespace);
                if (oldPrefix != null && oldPrefix.length() > 0) {
                    return oldPrefix;
                }
            }
        }
        return null;
    }

    /**
     * Retrieve the namespace for the given prefix, if any.
     *
     * @param  prefix  namespace prefix to look up.
     * @param  model   the model in which to look.
     * @return  the namespace for the prefix, or null if none.
     */
    public static String getNamespaceURI(String prefix, BpelModel model) {
        if (model != null && prefix != null) {
            BpelEntity entity = model.getProcess();
            ExNamespaceContext nsContext = entity != null 
                        ? entity.getNamespaceContext() : null;
            if (nsContext != null) {
                return nsContext.getNamespaceURI(prefix);
            }
        }
        return null;
    }
}
