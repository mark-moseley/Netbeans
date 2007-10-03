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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action;

import java.io.IOException;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.method.MethodCustomizer;
import org.netbeans.modules.j2ee.common.method.MethodModel;
import org.netbeans.modules.j2ee.ejbcore.api.methodcontroller.MethodType;
import org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.shared.MethodsNode;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 * Strategy for visual support for adding various methods into an EJB.
 * 
 * @author Pavel Buzek
 * @author Martin Adamek
 */
public abstract class AbstractAddMethodStrategy {
    
    private final String name;
    
    public AbstractAddMethodStrategy(String name) {
        this.name = name;
    }
    
    protected abstract MethodModel getPrototypeMethod();
    
    /** Describes method type handled by this action. */
    public abstract MethodType.Kind getPrototypeMethodKind();
    
    protected abstract MethodCustomizer createDialog(FileObject fileObject, MethodModel methodModel) throws IOException;

    protected abstract void generateMethod(MethodModel method, boolean isOneReturn, boolean publishToLocal, boolean publishToRemote,
            String ejbql, FileObject ejbClassFO, String ejbClass) throws IOException;
    
    public abstract boolean supportsEjb(FileObject fileObject, String className);

    public String getTitle() {
        return name;
    }
    
    public void addMethod(FileObject fileObject, String className) throws IOException {
        if (className == null) {
            return;
        }
        MethodModel methodModel = getPrototypeMethod();
        MethodCustomizer methodCustomizer = createDialog(fileObject, methodModel);
        if (methodCustomizer.customizeMethod()) {
            try {
                MethodModel method = methodCustomizer.getMethodModel();
                boolean isOneReturn = methodCustomizer.finderReturnIsSingle();
                boolean publishToLocal = methodCustomizer.publishToLocal();
                boolean publishToRemote = methodCustomizer.publishToRemote();
                String ejbql = methodCustomizer.getEjbQL();
                generateMethod(method, isOneReturn, publishToLocal, publishToRemote, ejbql, fileObject, className);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
    }
    
    protected EjbJar getEjbModule(FileObject fileObject) {
        return org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(fileObject);
    }
    
    protected static MethodsNode getMethodsNode() {
        Node[] nodes = Utilities.actionsGlobalContext().lookup(new Lookup.Template<Node>(Node.class)).allInstances().toArray(new Node[0]);
        if (nodes.length != 1) {
            return null;
        }
        return nodes[0].getLookup().lookup(MethodsNode.class);
    }
    
}
