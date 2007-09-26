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

package org.netbeans.spi.jumpto.type;

import java.util.List;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.api.project.Project;

/**
 * A Type Provider participates in the Goto Type dialog by providing TypeDescriptors,
 * one for each matched type, when asked to do so.
 * 
 * The Type Providers are registered in Lookup.
 * 
 * @todo Should we return a Collection rather than a List?
 * 
 * @author Tor Norbye
 */
public interface TypeProvider {
    /** 
     * Describe this provider with an internal name, in case we want to provide
     * some kind of programmatic filtering (e.g. a Java EE dialog wanting to include
     * or omit specific type providers, without relying on class names or 
     * localized display names)
     * 
     * @return An internal String uniquely identifying this type provider, such as
     *   "java"
     */
    String name();

    /** 
     * Describe this provider for the user, in case we want to offer filtering
     * capabilities in the Go To Type dialog
     * 
     * @return A display name describing the types being provided by this TypeProvider,
     *  such as "Java Types", "Ruby Types", etc.
     */
    String getDisplayName();
    
    /** 
     * Provide a list of TypeDescriptors that match the given search text for the given
     * search type. This might be a slow operation, and the infrastructure may end
     * up calling {@link #cancel} on the same type provider during the operation, in which
     * case the method can return incomplete results. If there is a "current project",
     * the Go To Type infrastructure will perform the search in two passes; first it
     * will call {@link #getTypeNames} with the current project, which should be a reasonably
     * fast search, and display those types first. It will then call the method again
     * with a null project, which should return all types.
     * <p>
     * Note that a useful performance optimization is for the TypeProvider to cache
     * a few of its most recent search results, and if the next search (e.g. more user
     * keystrokes) is a simple narrowing of the search, just filter the previous search
     * result. There is an explicit {@link #cleanup} call that the Go To Type dialog
     * will make at the end of the dialog interaction, which can be used to clean up the cache.
     * 
     * @param project If not null, limit the type search to the given project.
     * @param text The text to be used for the search; e.g. when type=SearchType.PREFIX,
     *   text is the prefix that all returned types should start with.
     * @param type A type of search to be performed, such as prefix, regexp or camel case.
     * @return A collection of TypeDescriptors that match the given search criteria
     */
    List<? extends TypeDescriptor> getTypeNames(Project project, String text, SearchType type);

    /**
     * Cancel the current operation, if possible. This might be called if the user
     * has typed something (including the backspace key) which makes the current
     * search obsolete and a new one should be initiated.
     */
    void cancel();


    /**
     * The Go To Type dialog is dismissed for now - free up resources if applicable.
     * (A new "session" will be indicated by a new call to getTypeNames.)
     * 
     * This allows the TypeProvider to cache its most recent search result, and if the next
     * search is simply a narrower search, it can just filter the previous result.
     */
    void cleanup();
}
