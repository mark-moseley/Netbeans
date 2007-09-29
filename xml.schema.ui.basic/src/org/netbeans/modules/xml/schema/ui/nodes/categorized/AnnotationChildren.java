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

package org.netbeans.modules.xml.schema.ui.nodes.categorized;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.ui.basic.spi.AppInfoProvider;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.schema.ui.nodes.ReferencingNodeProvider;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.ui.nodes.SchemaUIContext;

/**
 *
 * @author  Ajit Bhate
 */
public class AnnotationChildren extends CategorizedChildren<Annotation> {
    public AnnotationChildren(SchemaUIContext context,
            SchemaComponentReference<Annotation> reference) {
        super(context,reference);
    }
    
    
    /**
     *
     *
     */
    protected List<Node> createKeys() {
        List<Node> keys=super.createKeys();
        Lookup.Result providerLookups =
                Lookup.getDefault().lookup(new Lookup.Template(AppInfoProvider.class));
        Collection providers = providerLookups.allInstances();
        if (providers != null && !providers.isEmpty()) {
            ArrayList<Node> customNodes = new ArrayList<Node>();
            ArrayList<AppInfo> customAppInfos = new ArrayList<AppInfo>();
            Node parent = getNode();
            if(parent!=null) {
                SchemaComponentNode scn = (SchemaComponentNode)parent.getCookie
                        (SchemaComponentNode.class);
                if(scn!=null) {
                ArrayList<Node> path = new ArrayList<Node>();
                    path.add(parent);
                    while(true) {
                        parent = parent.getParentNode();
                        if(parent == null) {
                            ReferencingNodeProvider refProvider =
                                    (ReferencingNodeProvider)path.get(0).getLookup().
                                    lookup(ReferencingNodeProvider.class);
                            if(refProvider!=null) parent = refProvider.getNode();
                        }
                        if (parent == null) break;
                        path.add(0,parent);
                    }
                    for(Object provider:providers) {
                        Node customNode = null;
                        AppInfoProvider aiProvider = (AppInfoProvider)provider;
                        if(aiProvider.isActive(getReference().get().getModel()) &&
                                (customNode = aiProvider.getNode(path))!=null) {
                            customNodes.add(customNode);
                            AppInfo customAppInfo = (AppInfo)customNode.
                                    getLookup().lookup(AppInfo.class);
                            if(customAppInfo!=null)
                                customAppInfos.add(customAppInfo);
                        }
                    }
                }
            }
            for(int idx = keys.size(); idx>0; idx--) {
                Node n = keys.get(idx-1);
                SchemaComponentNode scn = (SchemaComponentNode)n.getCookie
                        (SchemaComponentNode.class);
                if(scn!=null && scn.getReference().get() instanceof AppInfo) {
                    AppInfo appInfo = (AppInfo)scn.getReference().get();
                    if(customAppInfos.contains(appInfo)) keys.remove(n);
                }
            }
            keys.addAll(customNodes);
        }
        
        return keys;
    }
}
