/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.html;

import java.util.Arrays;
import java.util.Collection;

/**
 *
 * @author marekfukala
 */
public enum HtmlVersion {

    UNKNOWN(new String[]{}),

    HTML32(new String[]{"-//W3C//DTD HTML 3.2 Final//EN"}),

    HTML40(new String[]{"-//W3C//DTD HTML 4.0//EN",
                        "-//W3C//DTD HTML 4.0 Transitional//EN",
                        "-//W3C//DTD HTML 4.0 Frameset//EN"}),

    HTML41(new String[]{"-//W3C//DTD HTML 4.01//EN",
                        "-//W3C//DTD HTML 4.01 Transitional//EN",
                        "-//W3C//DTD HTML 4.01 Frameset//EN"}),

    XHTML10(new String[]{"-//W3C//DTD XHTML 1.0 Strict//EN",
                        "-//W3C//DTD XHTML 1.0 Transitional//EN",
                        "-//W3C//DTD XHTML 1.0 Frameset//EN"}, "http://www.w3.org/1999/xhtml", true);

    //TODO Add XHTML1.1, XHTML 2.0 and HTML 5 support

    public static HtmlVersion findHtmlVersion(String publicId) {
        for(HtmlVersion version : HtmlVersion.values()) {
            if(version.getPublicIDs().contains(publicId)) {
                return version;
            }
        }
        return UNKNOWN;
    }

    private final Collection<String> publicIDs;
    private final String defaultNamespace;
    private boolean isXhtml;

    private HtmlVersion(String[] publicIDs) {
        this(publicIDs, null, false);
    }

    private HtmlVersion(String[] publicIDs, String defaultNamespace, boolean isXhtml) {
        this.publicIDs = Arrays.asList(publicIDs);
        this.defaultNamespace = defaultNamespace;
        this.isXhtml = isXhtml;
    }

    public Collection<String> getPublicIDs() {
        return this.publicIDs;
    }

    public String getDefaultNamespace() {
        return this.defaultNamespace;
    }

    public boolean isXhtml() {
        return this.isXhtml;
    }

}
