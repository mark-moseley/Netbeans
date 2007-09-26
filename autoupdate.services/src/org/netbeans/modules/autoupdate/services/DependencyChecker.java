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

package org.netbeans.modules.autoupdate.services;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;

class DependencyChecker extends Object {

    private static final Logger err = Logger.getLogger(DependencyChecker.class.getName ()); // NOI18N
    
    public static Set<Dependency> findBrokenDependencies (Set<Dependency> deps, Collection<ModuleInfo> modules) {
        Set<Dependency> res = new HashSet<Dependency> ();
        for (Dependency dep : deps) {
            err.log(Level.FINE, "Dependency[" + dep.getType () + "]: " + dep);
            switch (dep.getType ()) {
                case (Dependency.TYPE_REQUIRES) :
                    if (findModuleMatchesDependencyRequires (dep, modules) != null) {
                        // ok
                    } else {
                        // bad, report missing module
                        res.add (dep);
                    }
                    break;
                case (Dependency.TYPE_NEEDS) :
                    if (findModuleMatchesDependencyRequires (dep, modules) != null) {
                        // ok
                    } else {
                        // bad, report missing module
                        res.add (dep);
                    }
                    break;
                case (Dependency.TYPE_RECOMMENDS) :
                    if (findModuleMatchesDependencyRequires (dep, modules) != null) {
                        // ok
                    } else {
                        // bad, report missing module
                        res.add (dep);
                    }
                    break;
                case (Dependency.TYPE_MODULE) :
                    if (matchDependencyModule (dep, modules)) {
                        // ok
                    } else {
                        // bad, report missing module
                        res.add (dep);
                    }
                    break;
                case (Dependency.TYPE_JAVA) :
                    err.log(Level.FINE, "Check dependency on Java platform. Dependency: " + dep);
                    break;
                default:
                    //assert false : "Unknown type of Dependency, was " + dep.getType ();
                    err.log(Level.FINE, "Uncovered Dependency " + dep);                    
            }
        }
        return res;
    }
    
    static ModuleInfo findModuleMatchesDependencyRequires (Dependency dep, Collection<ModuleInfo> modules) {
        for (ModuleInfo info : modules) {
            if (Arrays.asList (info.getProvides ()).contains (dep.getName ())) {
                return info;
            }
        }
        return null;
    }
    
    private static boolean matchDependencyModule (Dependency dep, Collection<ModuleInfo> modules) {
        for (ModuleInfo module : modules) {
            if (checkDependencyModule (dep, module)) {
                return true;
            }
        }
        
        return false;
    }
    
    static boolean checkDependencyModuleAllowEqual (Dependency dep, ModuleInfo module) {
        return checkDependencyModule (dep, module, true);
    }
    
    static boolean checkDependencyModule (Dependency dep, ModuleInfo module) {
        return checkDependencyModule (dep, module, false);
    }
    
    private static boolean checkDependencyModule (Dependency dep, ModuleInfo module, boolean allowEqual) {

        boolean ok = false;
        
        if (dep.getName ().equals (module.getCodeNameBase ()) || dep.getName ().equals (module.getCodeName ())) {
            if (dep.getComparison () == Dependency.COMPARE_ANY) {
                ok = true;
            } else if (dep.getComparison () == Dependency.COMPARE_SPEC) {
                    if (module.getSpecificationVersion () == null) {
                        ok = false;
                    } else if (new SpecificationVersion (dep.getVersion ()).compareTo (module.getSpecificationVersion ()) > 0) {
                        ok = false;
                    } else if (allowEqual && new SpecificationVersion (dep.getVersion ()).compareTo (module.getSpecificationVersion ()) == 0) {
                        ok = true;
                    } else {
                        ok = true;
                    }
            } else {
                // COMPARE_IMPL
                if (module.getImplementationVersion () == null) {
                    ok = false;
                } else if (! module.getImplementationVersion ().equals (dep.getVersion ())) {
                    ok = false;
                } else {
                    ok = true;
                }
            }
            
        } else {

            int dash = dep.getName ().indexOf ('-'); // NOI18N
            if (dash != -1) {
                // Ranged major release version, cf. #19714.
                int slash = dep.getName ().indexOf ('/'); // NOI18N
                String cnb = dep.getName ().substring (0, slash);
                int relMin = Integer.parseInt (dep.getName ().substring (slash + 1, dash));
                int relMax = Integer.parseInt (dep.getName ().substring (dash + 1));
                if (cnb.equals (module.getCodeNameBase ()) &&
                        relMin <= module.getCodeNameRelease () &&
                        relMax >= module.getCodeNameRelease ()) {
                    if (dep.getComparison () == Dependency.COMPARE_ANY) {
                        ok = true;
                    } else {
                        // COMPARE_SPEC; COMPARE_IMPL not allowed here
                        if (module.getCodeNameRelease () > relMin) {
                            // Great, skip the spec version.
                            ok = true;
                        } else {
                            // As usual.
                            if (module.getSpecificationVersion () == null) {
                                ok = false;
                            } else if (new SpecificationVersion (dep.getVersion ()).compareTo (module.getSpecificationVersion ()) > 0) {
                                ok = false;
                            } else if (allowEqual && new SpecificationVersion (dep.getVersion ()).compareTo (module.getSpecificationVersion ()) > 0) {
                                ok = true;
                            } else {
                                ok = true;
                            }
                        }
                    }
                }
            }
        }

        return ok;
    }

}
