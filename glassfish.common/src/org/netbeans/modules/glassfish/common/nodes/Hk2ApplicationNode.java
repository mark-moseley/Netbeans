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

package org.netbeans.modules.glassfish.common.nodes;

import java.util.Map;
import org.netbeans.modules.glassfish.common.nodes.actions.OpenURLActionCookie;
import org.netbeans.modules.glassfish.spi.AppDesc;
import org.netbeans.modules.glassfish.spi.Decorator;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 * @author Peter Williams
 */
public class Hk2ApplicationNode extends Hk2ItemNode {

    public Hk2ApplicationNode(final Lookup lookup, final AppDesc app, final Decorator decorator) {
        super(Children.LEAF, lookup, app.getName(), decorator);
        setDisplayName(app.getName());
        setShortDescription("<html>name: " + app.getName() + "<br>path: " + app.getPath() + "</html>");

        // !PW FIXME should method of retrieving context root be controlled by decorator?
        if(decorator.canShowBrowser()) {
            getCookieSet().add(new OpenURLActionCookie() {
                public String getWebURL() {
                    String result = null;
                    GlassfishModule commonModule = lookup.lookup(GlassfishModule.class);
                    if(commonModule != null) {
                        Map<String, String> ip = commonModule.getInstanceProperties();
                        String host = ip.get(GlassfishModule.HOSTNAME_ATTR);
                        String httpPort = ip.get(GlassfishModule.HTTPPORT_ATTR);
                        result = HTTP_HEADER + host + ":" + httpPort + "/" + app.getContextRoot();
                        if(result.endsWith("//")) {
                            result = result.substring(0, result.length()-1);
                        }
                    }
                    return result;
                }
            });
        }
    }

}
