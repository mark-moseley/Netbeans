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

package org.netbeans.upgrade.systemoptions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

/**
 * @author Milos Kleint
 */
class URLProcessor extends PropertyProcessor {    
    URLProcessor() {
        super("java.net.URL");//NOI18N
    }
    
    void processPropertyImpl(String propertyName, Object value) {
        StringBuffer sb = new StringBuffer();       
        if ("mainProjectURL".equals(propertyName)) {//NOI18N
            List l = ((SerParser.ObjectWrapper)value).data;
            try {
                URL url = createURL(l);
                addProperty(propertyName, url.toExternalForm());
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }  else {
            throw new IllegalStateException();
        }
    }
    
    public static URL createURL(List l) throws MalformedURLException {
            String protocol = null;
            String host = null;
            int port = -1;
            String file = null;
            String authority = null;
            String ref = null;
            for (Iterator it = l.iterator(); it.hasNext();) {
                Object elem = (Object) it.next();
                if (elem instanceof SerParser.NameValue) {
                    SerParser.NameValue nv = (SerParser.NameValue)elem;
                    if (nv.value != null && nv.name != null) {
                        if (nv.name.name.equals("port")) {//NOI18N
                            port = ((Integer)nv.value).intValue();//NOI18N
                        }
                        else if (nv.name.name.equals("file")) {//NOI18N
                            file = nv.value.toString();//NOI18N
                        }
                        else if (nv.name.name.equals("authority")) {//NOI18N
                            authority = nv.value.toString();//NOI18N
                        }
                        else if (nv.name.name.equals("host")) {//NOI18N
                            host = nv.value.toString();//NOI18N
                        }
                        else if (nv.name.name.equals("protocol")) {//NOI18N
                            protocol = nv.value.toString();//NOI18N
                        }
                        else if (nv.name.name.equals("ref")) {//NOI18N
                            ref = nv.value.toString();//NOI18N
                        }
                    }
                }
            }
            return new URL(protocol, host, port, file);
        
    }
}
