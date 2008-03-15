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

package org.netbeans.modules.javascript.editing;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.NameKind;

/**
 * Cache which performs type lookup etc. for functions
 * 
 * @author Tor Norbye
 */
public class FunctionCache {
    static FunctionCache instance = new FunctionCache();
    static final IndexedElement NONE = new IndexedProperty(null, null, null, null, null, 0, ElementKind.OTHER);
    
    Map<String,IndexedElement> cache = new HashMap<String,IndexedElement>(500);
    
    public String getType(String fqn, JsIndex index) {
        IndexedElement element = cache.get(fqn);
        if (element == NONE) {
            return null;
        } else if (element == null) {
            Set<IndexedElement> elements = index.getElements(fqn, null, NameKind.EXACT_NAME, JsIndex.ALL_SCOPE, null);
            if (elements.size() > 0) {
                IndexedElement firstElement = elements.iterator().next();
                cache.put(fqn, firstElement);
                return firstElement.getType();
            } else {
                cache.put(fqn, NONE);
            }
            return null;
        } else {
            return element.getType();
        }
    }
    
    public void wipe(String fqn) {
        cache.remove(fqn);
    }
    
    public static FunctionCache getInstance() {
        return instance;
    }

    boolean isEmpty() {
        return cache.size() == 0;
    }
}
