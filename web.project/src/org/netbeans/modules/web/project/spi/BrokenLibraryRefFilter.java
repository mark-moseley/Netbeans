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

/**
 * Allows removing broken library references from a project while the project
 * is being open.
 *
 * <p>Implementations of this interface are returned by {@link BrokenLibraryRefFilterProvider}.
 * When a web project is opened the {@link #removeLibraryReference} method is called
 * for all broken library references. If at least one implementation returns
 * <code>true</code>, the library reference is removed.</p>
 *
 * @author Andrei Badea
 */
public interface BrokenLibraryRefFilter {

    /**
     * Return <code>true</code> from this method to remove the
     * reference to the given library.
     * @param  libraryName the name of a library to which a broken
     *         reference exists; never null.
     * @return true to remove this reference from the project; false otherwise
     */
    boolean removeLibraryReference(String libraryName);
}
