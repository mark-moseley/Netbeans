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

package org.netbeans.modules.projectapi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.LookupMerger;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Template;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Factory methods for lazy {@link LookupProvider} registration.
 */
public class LazyLookupProviders {

    private LazyLookupProviders() {}

    /**
     * @see ProjectServiceProvider
     */
    public static LookupProvider forProjectServiceProvider(final Map<String,Object> attrs) throws ClassNotFoundException {
        return new LookupProvider() {
            public Lookup createAdditionalLookup(final Lookup lkp) {
                return new ProxyLookup() {
                    Collection<String> serviceNames = Arrays.asList(((String) attrs.get("service")).split(",")); // NOI18N
                    protected @Override void beforeLookup(Template<?> template) {
                        if (serviceNames != null && serviceNames.contains(template.getType().getName())) { // NOI18N
                            Class<?> service = template.getType();
                            try {
                                Object instance = loadPSPInstance((String) attrs.get("class"), (String) attrs.get("method"), lkp); // NOI18N
                                if (!service.isInstance(instance)) {
                                    // JRE #6456938: Class.cast currently throws an exception without details.
                                    throw new ClassCastException("Instance of " + instance.getClass() + " unassignable to " + service);
                                }
                                setLookups(Lookups.singleton(instance));
                                serviceNames = null;
                            } catch (Exception x) {
                                Exceptions.attachMessage(x, "while loading from " + attrs);
                                Exceptions.printStackTrace(x);
                            }
                        }
                    }
                };
            }
        };
    }
    private static Object loadPSPInstance(String implName, String methodName, Lookup lkp) throws Exception {
        Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(implName);
        if (methodName == null) {
            for (Constructor c : clazz.getConstructors()) {
                Object[] vals = valuesFor(c.getParameterTypes(), lkp);
                if (vals != null) {
                    return c.newInstance(vals);
                }
            }
        } else {
            for (Method m : clazz.getMethods()) {
                if (!m.getName().equals(methodName)) {
                    continue;
                }
                Object[] vals = valuesFor(m.getParameterTypes(), lkp);
                if (vals != null) {
                    return m.invoke(null, vals);
                }
            }
        }
        throw new RuntimeException(implName + "." + methodName); // NOI18N
    }
    private static Object[] valuesFor(Class[] params, Lookup lkp) {
        if (params.length > 2) {
            return null;
        }
        List<Object> values = new ArrayList<Object>();
        for (Class param : params) {
            if (param == Lookup.class) {
                values.add(lkp);
            } else if (param == Project.class) {
                Project project = lkp.lookup(Project.class);
                if (project == null) {
                    throw new IllegalArgumentException("Lookup " + lkp + " did not contain any Project instance");
                }
                values.add(project);
            } else {
                return null;
            }
        }
        return values.toArray();
    }

    /**
     * @see org.netbeans.spi.project.LookupMerger.Registration
     */
    public static MetaLookupMerger forLookupMerger(final Map<String,Object> attrs) throws ClassNotFoundException {
        return new MetaLookupMerger() {
            private LookupMerger<?> delegate;
            public boolean canNowMerge(Class<?> service) {
                if (delegate == null && service.getName().equals((String) attrs.get("service"))) { // NOI18N
                    try {
                        LookupMerger<?> m = (LookupMerger<?>) attrs.get("lookupMergerInstance"); // NOI18N
                        if (service != m.getMergeableClass()) {
                            throw new ClassCastException(service + " vs. " + m.getMergeableClass()); // NOI18N
                        }
                        delegate = m;
                        return true;
                    } catch (Exception x) {
                        Exceptions.printStackTrace(x);
                    }
                }
                return false;
            }
            public LookupMerger merger() {
                return delegate;
            }
        };
    }

}
