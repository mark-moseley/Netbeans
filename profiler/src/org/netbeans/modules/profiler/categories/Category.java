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

package org.netbeans.modules.profiler.categories;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.lib.profiler.marker.Mark;
import org.netbeans.modules.profiler.utilities.Visitable;
import org.netbeans.modules.profiler.utilities.Visitor;

/**
 *
 * @author Jaroslav Bachorik
 */
public abstract class Category implements Visitable<Category> {
    private String id;
    private String label;
    
    private Set<CategoryDefinition> definitions;
    private Mark assignedMark;
    
    public static final Category DEFAULT = new Category("DEFAULT", "Default Category", new Mark((short)0)) {
        public <R, P> R accept(Visitor<Visitable<Category>, R, P> visitor, P parameter) {
            // DO NOTHING
            return null;
        }

        @Override
        public Set<Category> getSubcategories() {
            return Collections.EMPTY_SET;
        }
    };
    
    private Category(String id, String label, Mark mark) {
        this.id = id;
        this.label = label;
        this.definitions = new HashSet<CategoryDefinition>();
        this.assignedMark = mark;
    }
    
    public Category(String id, String label) {
        this(id, label, new Mark());
    }

    public String getLabel() {
        return label;
    }

    public String getId() {
        return id;
    }

    public Mark getAssignedMark() {
        return assignedMark;
    }

    public Set<CategoryDefinition> getDefinitions() {
        return definitions;
    }
        
    public Category getValue() {
        return this;
    }
    
    public abstract Set<Category> getSubcategories();
    
    void processDefinitionsWith(CategoryDefinitionProcessor processor) {
        for(CategoryDefinition def : definitions) {
            def.processWith(processor);
        }
    }
}
