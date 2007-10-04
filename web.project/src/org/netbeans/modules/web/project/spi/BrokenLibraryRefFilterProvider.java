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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.project.spi;

import org.netbeans.api.project.Project;

/**
 * Allows removing broken library references from a project while the project
 * is being open.
 *
 * <p>Implementations of this interface are registered in the
 * <code>Projects/org-netbeans-modules-web-project/BrokenLibraryRefFilterProviders</code>
 * folder in the default file system. When a web project is opened,
 * the {@link #createFilter} method of all implementations is called
 * to create {@link BrokenLibraryRefFilter a filter for broken references}. 
 * This filter is then queried for all broken library references in the project.
 * If at least one filter returns <code>true</code>, the library reference is removed.</p>
 *
 * @author Andrei Badea
 */
public interface BrokenLibraryRefFilterProvider {

    /**
     * Creates a filter for the broken library references in the given
     * project.
     * 
     * @param  project the project being opened; never null.
     * @return a filter for the broken library references in <code>project</code>
     *         or null if this project does not need to be filtered.
     */
    public BrokenLibraryRefFilter createFilter(Project project);
}
