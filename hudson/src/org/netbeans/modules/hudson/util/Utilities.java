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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.hudson.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.w3c.dom.Element;

/**
 * Helper utility class
 */
public class Utilities {

    private Utilities() {}
    
    public static boolean isSupportedVersion(HudsonVersion version) {
        // Check for null
        if (null == version)
            return false;
        
        // Version check
        if (version.compareTo(HudsonVersion.SUPPORTED_VERSION) < 0)
            return false;
        
        return true;
    }

    /**
     * Encode a path segment of a URI (e.g. a job name) suitably for a URL.
     * @param segment a path segment
     * @return the same with spaces and unsafe characters escaped
     * @throws IllegalArgumentException if there is some other URI problem, or a slash
     */
    public static String uriEncode(String segment) {
        if (segment.contains("/")) {
            throw new IllegalArgumentException("cannot contain a slash: " + segment);
        }
        try {
            return new URI(null, segment, null).toASCIIString();
        } catch (URISyntaxException x) {
            throw (IllegalArgumentException) new IllegalArgumentException(x.toString()).initCause(x);
        }
    }

    /**
     * Inverse of {@link #uriEncode}.
     */
    public static String uriDecode(String string) {
        String d = URI.create(string).getPath();
        if (d.contains("/")) {
            throw new IllegalArgumentException(d);
        }
        return d;
    }

        /**
         * Evaluate an XPath expression.
         * @param expr an XPath expression
         * @param xml a DOM context
         * @return the string value, or null
         */
        public static synchronized String xpath(String expr, Element xml) {
            try {
                return xpath.evaluate(expr, xml);
            } catch (XPathExpressionException x) {
                Logger.getLogger(Utilities.class.getName()).log(Level.FINE, "cannot evaluate '" + expr + "'", x);
                return null;
            }
        }
        private static final XPath xpath = XPathFactory.newInstance().newXPath();

}
