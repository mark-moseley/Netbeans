/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.core.startup;

import java.io.IOException;
import java.text.Collator;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.Util;
import org.openide.modules.Dependency;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;

/**
 * Utility class to provide localized messages explaining problems
 * that modules had during attempted installation.
 * Used by both {@link org.netbeans.core.startup.NbEvents} and autoupdate's ModuleBean.
 * @author Jesse Glick
 * @see "#16636"
 */
public final class NbProblemDisplayer {
    
    private NbProblemDisplayer() {}
    
    /**
     * Provide a localized explanation of some installation problem.
     * Problem may be either an InvalidException or a Dependency.
     * Structure of message can assume that the module failing its
     * dependencies is already being displayed, and concentrate
     * on the problem.
     * @param m the module which cannot be installed
     * @param problem either an {@link InvalidException} or {@link Dependency} as returned from {@link Module#getProblems}
     * @return an explanation of the problem in the most human-friendly format available
     */
    public static String messageForProblem(Module m, Object problem) {
        if (problem instanceof InvalidException) {
            return Util.findLocalizedMessage((InvalidException)problem, true);
        } else {
            Dependency dep = (Dependency)problem;
            switch (dep.getType()) {
            case Dependency.TYPE_MODULE:
                String polite = (String)m.getLocalizedAttribute("OpenIDE-Module-Module-Dependency-Message"); // NOI18N
                if (polite != null) {
                    return polite;
                } else {
                    String name = dep.getName();
                    // Find code name base:
                    int idx = name.lastIndexOf('/');
                    if (idx != -1) {
                        name = name.substring(0, idx);
                    }
                    Module other = m.getManager().get(name);
                    if (other != null && other.getCodeName().equals(dep.getName())) {
                        switch (dep.getComparison()) {
                        case Dependency.COMPARE_ANY:
                            // Just disabled (probably had its own problems).
                            return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_other_disabled", other.getDisplayName());
                        case Dependency.COMPARE_IMPL:
                            String requestedI = dep.getVersion();
                            String actualI = (other.getImplementationVersion() != null) ?
                                other.getImplementationVersion() :
                                NbBundle.getMessage(NbProblemDisplayer.class, "LBL_no_impl_version");
                            if (requestedI.equals(actualI)) {
                                // Just disabled (probably had its own problems).
                                return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_other_disabled", other.getDisplayName());
                            } else {
                                // Wrong version.
                                return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_other_wrong_version", other.getDisplayName(), requestedI, actualI);
                            }
                        case Dependency.COMPARE_SPEC:
                            SpecificationVersion requestedS = new SpecificationVersion(dep.getVersion());
                            SpecificationVersion actualS = (other.getSpecificationVersion() != null) ?
                                other.getSpecificationVersion() :
                                new SpecificationVersion("0"); // NOI18N
                            if (actualS.compareTo(requestedS) >= 0) {
                                // Just disabled (probably had its own problems).
                                return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_other_disabled", other.getDisplayName());
                            } else {
                                // Too old.
                                return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_other_too_old", other.getDisplayName(), requestedS, actualS);
                            }
                        default:
                            throw new IllegalStateException();
                        }
                    } else {
                        // Keep the release version info in this case.
                        // XXX would be nice to have a special message for mismatched major release
                        // version - i.e. other != null
                        return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_other_needed_not_found", dep.getName());
                    }
                }
            case Dependency.TYPE_REQUIRES:
            case Dependency.TYPE_NEEDS:
                polite = (String)m.getLocalizedAttribute("OpenIDE-Module-Requires-Message"); // NOI18N
                if (polite != null) {
                    return polite;
                } else {
                    Set others = m.getManager().getModules();
                    Iterator it = others.iterator();
                    while (it.hasNext()) {
                        Module other = (Module)it.next();
                        if (other.provides(dep.getName())) {
                            return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_require_disabled", dep.getName());
                        }
                    }
                    return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_require_not_found", dep.getName());
                }
            case Dependency.TYPE_PACKAGE:
                polite = (String)m.getLocalizedAttribute("OpenIDE-Module-Package-Dependency-Message"); // NOI18N
                if (polite != null) {
                    return polite;
                } else {
                    String name = dep.getName();
                    // Find package name or qualified name of probe class:
                    int idx = name.lastIndexOf('[');
                    if (idx == 0) {
                        // Probed class. [javax.television.Antenna]
                        return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_class_not_loaded", name.substring(1, name.length() - 1));
                    } else if (idx != -1) {
                        // Package plus sample class. javax.television[Antenna]
                        return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_package_not_loaded_or_old", name.substring(0, idx));
                    } else {
                        return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_package_not_loaded_or_old", name);
                    }
                }
            case Dependency.TYPE_JAVA:
                // XXX would OpenIDE-Module-Java-Dependency-Message be useful?
                if (dep.getName().equals(Dependency.JAVA_NAME) && dep.getComparison() == Dependency.COMPARE_SPEC) {
                    return NbBundle.getMessage(NbProblemDisplayer.class, "MSG_problem_java_too_old", dep.getVersion(), Dependency.JAVA_SPEC);
                } else {
                    // All other usages unlikely, don't bother making pretty.
                    return dep.toString();
                }
            default:
                throw new IllegalArgumentException(dep.toString());
            }
        }
    }

    static void problemMessagesForModules(Appendable writeTo, Collection<? extends Module> modules, boolean justRootCause) {
        try {
            HashSet<String> names = new HashSet<String>();
            for (Module m : modules) {
                names.add(m.getCodeName());
            }

            HashSet<String> dependantModules = new HashSet<String>();
            for (Module m : modules) {
                SortedSet<String> problemTexts = new TreeSet<String>(Collator.getInstance());
                Iterator pit = m.getProblems().iterator();
                if (pit.hasNext()) {
                    while (pit.hasNext()) {
                        Object problem = pit.next();
                        if (problem instanceof Dependency && justRootCause) {
                            Dependency d = (Dependency)problem;
                            if (
                                d.getType() == Dependency.TYPE_MODULE &&
                                names.contains(d.getName())
                            ) {
                                dependantModules.add(m.getCodeName());
                                continue;
                            }
                        }

                        problemTexts.add(m.getDisplayName() + " - " + // NOI18N
                                         NbProblemDisplayer.messageForProblem(m, problem));
                    }
                } else {
                    throw new IllegalStateException("Module " + m + " could not be installed but had no problems"); // NOI18N
                }
                for (String s: problemTexts) {
                    writeTo.append("\n\t").append(s); // NOI18N
                }
            }
            if (!dependantModules.isEmpty()) {
                writeTo.append("\n\t").append(NbBundle.getMessage(NbProblemDisplayer.class, "MSG_also_dep_modules", dependantModules.size()));
            }
        } catch (IOException ex) {
            throw (IllegalStateException)new IllegalStateException().initCause(ex);
        }
    }
    
}
