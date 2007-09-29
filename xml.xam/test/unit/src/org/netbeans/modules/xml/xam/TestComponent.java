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

package org.netbeans.modules.xml.xam;

import java.util.List;

/**
 *
 * @author Nam Nguyen
 */
public class TestComponent extends AbstractComponent<TestComponent> implements NamedReferenceable<TestComponent>, Cloneable {
    int index;
    String value;
    
    public TestComponent(TestModel model, int index) {
        super(model);
        this.index = index;
    }
    
    public String toString() { return getName(); }
    public String getName() { return "test"; }
    
    protected void populateChildren(List<TestComponent> children) {
        children.add(new A(getModel(), 1));
        children.add(new A(getModel(), 2));
        children.add(new A(getModel(), 3));
    }
    
    public void setValue(String v) { 
        String old = value;
        this.value = v;
        super.firePropertyChange("value", old, value);
        super.fireValueChanged();
    }
    public String getValue() { 
        return value;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    public int getIndex() {
        return index;
    }

    public static class A extends TestComponent {
        public A(TestModel model, int i) {
            super(model, i);
            this.index = i;
        }
        public String getName() { return "a"+index; }
    }
    
    public static class B extends TestComponent {
        public B(TestModel model, int i) {
            super(model, i);
            this.index = i;
        }
        public String getName() { return "b"+index; }
    }

    public static class C extends TestComponent {
        public C(TestModel model, int i) {
            super(model, i);
            this.index = i;
        }
        public String getName() { return "c"+index; }
    }

    public TestModel getModel() {
        return (TestModel) super.getModel();
    }

    protected void insertAtIndexQuietly(TestComponent newComponent, List<TestComponent> children, int index) {
        children.add(index, newComponent);
    }

    public Component copy(TestComponent parent) {
        try {
            return (Component) this.clone();
        } catch(CloneNotSupportedException ex) {
            return null;
        }
    }

    protected void removeChildQuietly(TestComponent component, List<TestComponent> children) {
        children.remove(component);
    }

    protected void appendChildQuietly(TestComponent component, List<TestComponent> children) {
        children.add(component);
    }
    
}
