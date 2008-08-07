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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry;

/**
 *
 * @author david
 */
public class EclipseProjectTestUtils {

    public static EclipseProject createEclipseProject(File proj, DotClassPath cp) throws IOException {
        return createEclipseProject(proj, cp, null, null);
    }
    
    public static EclipseProject createEclipseProject(File proj, DotClassPath cp, Workspace w, String name) throws IOException {
        return createEclipseProject(proj, cp, w, name, new ArrayList<Link>());
    }
    
    public static EclipseProject createEclipseProject(File proj, DotClassPath cp, Workspace w, String name, List<Link> links) throws IOException {
        EclipseProject ep = new EclipseProject(proj);
        if (w != null) {
            ep.setName(name);
            ep.setWorkspace(w);
            ep.setLinks(links);
            w.addProject(ep);
        }
        ep.setClassPath(cp);
        ep.resolveContainers(new ArrayList<String>(), false);
        return ep;
    }
    
    public static DotClassPathEntry createDotClassPathEntry(String ... keyvalue) {
        Map<String, String> map = new HashMap<String, String>();
        for (int i=0; i<keyvalue.length; i = i +2) {
            map.put(keyvalue[i], keyvalue[i+1]);
        }
        return new DotClassPathEntry(map, null);
    }
    
    public static Workspace createWorkspace(File workspace, Workspace.Variable ... variables) {
        Workspace w = new Workspace(workspace);
        for (Workspace.Variable v : variables) {
            w.addVariable(v);
        }
        return w;
    }
}
