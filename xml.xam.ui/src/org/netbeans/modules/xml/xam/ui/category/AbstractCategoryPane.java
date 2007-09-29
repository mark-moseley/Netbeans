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

package org.netbeans.modules.xml.xam.ui.category;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Abstract implementation of CategoryPane, used by concrete implementations.
 *
 * @author Nathan Fiedler
 */
public abstract class AbstractCategoryPane implements CategoryPane {
    /** Property change support for firing events to listeners. */
    protected PropertyChangeSupport changeSupport;
    /** The currently selected (visible) Category. */
    private Category currentCategory;

    /**
     * Creates a new instance of AbstractCategoryPane.
     */
    public AbstractCategoryPane() {
        changeSupport = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String name, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(name, listener);
    }

    public Category getCategory() {
        return currentCategory;
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String name, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(name, listener);
    }

    public void setCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("category cannot be null");
        }
        Category oldcat = currentCategory;
        currentCategory = category;
        changeSupport.firePropertyChange(PROP_CATEGORY, oldcat, category);
    }
}
